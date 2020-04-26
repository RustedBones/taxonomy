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

import fr.davit.taxonomy.record.{DnsRecordClass, DnsRecordType}
import scodec.bits._
import scodec.codecs._
import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}

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

  val labelSize: Codec[Int] = constantLenient(bin"00") ~> int(6) // first 2 bits are reserved
  val label: Codec[String]  = variableSizeBytes(labelSize, string(ascii)).withToString("label")

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

  val dnsQuestion: Codec[DnsQuestion] =
    (("qname" | qName) ::
      ("qtype" | dnsRecordType) ::
      ("qclass" | dnsRecordClass)).as[DnsQuestion]

}

object DnsCodec extends DnsCodec
