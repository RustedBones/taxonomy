/*
 * Copyright 2020 Michel Davit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.davit.taxonomy

import java.nio.charset.Charset

import fr.davit.taxonomy.DnsMessage.DnsMessageImpl
import fr.davit.taxonomy.record.DnsResourceRecord.DnsResourceRecordImpl
import fr.davit.taxonomy.record.{DnsRecordClass, DnsRecordType, DnsResourceRecord}
import scodec.bits._
import scodec.codecs._
import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}
import shapeless._

import scala.concurrent.duration._

trait DnsCodec {

  val ascii: Charset = Charset.forName("US-ASCII")

  val dnsType: Codec[DnsType]                 = bool.xmap(DnsType.apply, _.code == 1)
  val dnsOpCode: Codec[DnsOpCode]             = uint4.xmap(DnsOpCode.apply, _.code)
  val dnsResponseCode: Codec[DnsResponseCode] = uint4.xmap(DnsResponseCode.apply, _.code)
  val dnsRecordType: Codec[DnsRecordType]     = uint16.xmap(DnsRecordType.apply, _.code)
  val dnsRecordClass: Codec[DnsRecordClass]   = uint16.xmap(DnsRecordClass.apply, _.code)

  val dnsHeaderCodec: Codec[DnsHeader] = fixedSizeBytes(
    12,
    (("id" | uint16) ::
      ("qr" | dnsType) ::
      ("op" | dnsOpCode) ::
      ("aa" | bool) ::
      ("tc" | bool) ::
      ("rd" | bool) ::
      ("ra" | bool) ::
      ("z" | constantLenient(bin"000")) :~>:
      ("rcode" | dnsResponseCode) ::
      ("qdcount" | uint16) ::
      ("ancount" | uint16) ::
      ("nscount" | uint16) ::
      ("arcount" | uint16)).as[DnsHeader]
  )

  val label: Codec[String] = constant(bin"00") ~> variableSizeBytes(int(6), string(ascii))

  val labelPointer: Codec[String] = constant(bin"11") ~> int(14)
    .xmap[String](_.toString, _.toInt)
    .withToString("labelptr")

  val qName: Codec[String] = {
    val labels = filtered(
      list(label),
      new Codec[BitVector] {
        val nul                                                  = BitVector.lowByte
        override def sizeBound: SizeBound                        = SizeBound.unknown
        override def encode(bits: BitVector): Attempt[BitVector] = Attempt.successful(bits ++ nul)
        override def decode(bits: BitVector): Attempt[DecodeResult[BitVector]] =
          bits.bytes.indexOfSlice(nul.bytes) match {
            case -1 => Attempt.failure(Err("Does not contain a 'NUL' termination byte."))
            case i  => Attempt.successful(DecodeResult(bits.take(i * 8L), bits.drop(i * 8L + 8L)))
          }
      }
    )
    labels.xmap(_.mkString("."), _.split('.').toList)
  }

  val name: Codec[String] = Codec(
    encoder = qName, // TODO write pointer
    decoder = discriminated
      .by(peek(bits(2)))
      .typecase(bin"00", qName)
      .typecase(bin"11", labelPointer)
  )

  val dnsQuestionSection: Codec[DnsQuestion] =
    (("qname" | qName) ::
      ("qtype" | dnsRecordType) ::
      ("qclass" | dnsRecordClass)).as[DnsQuestion]

  val ttl: Codec[FiniteDuration] = uint32.xmap(_.seconds, _.toSeconds)
  val rdata: Codec[Vector[Byte]] = variableSizeBytes(uint16, bits).xmap(_.toByteArray.toVector, BitVector.apply)

  val dnsResourceRecord: Codec[DnsResourceRecord] = (("name" | name) ::
    ("type" | dnsRecordType) ::
    ("class" | dnsRecordClass) ::
    ("ttl" | ttl) ::
    ("rdata" | rdata)).xmap(
    Generic[DnsResourceRecordImpl].from(_),
    rr => rr.name :: rr.`type` :: rr.`class` :: rr.ttl :: rr.data.toVector :: HNil
  )

  val dnsMesage: Codec[DnsMessage] = dnsHeaderCodec
    .flatPrepend { header =>
      ("qdsection" | vectorOfN(provide(header.countQuestions), dnsQuestionSection)) ::
        ("ansection" | vectorOfN(provide(header.countAnswerRecords), dnsResourceRecord)) ::
        ("nssection" | vectorOfN(provide(header.countAuthorityRecords), dnsResourceRecord)) ::
        ("arsection" | vectorOfN(provide(header.countAdditionalRecords), dnsResourceRecord))
    }
    .xmap(
      Generic[DnsMessageImpl].from(_),
      message =>
        message.header ::
          message.questions.toVector ::
          message.answers.toVector ::
          message.authorities.toVector ::
          message.additionals.toVector ::
          HNil
    )
}

object DnsCodec extends DnsCodec
