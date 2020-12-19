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

package fr.davit.taxonomy.scodec

import _root_.scodec.bits._
import _root_.scodec.codecs._
import _root_.scodec.{Attempt, Codec, DecodeResult, Decoder, Err, SizeBound}
import fr.davit.taxonomy.model.record._
import fr.davit.taxonomy.model._
import scodec.Attempt.{Failure, Successful}
import shapeless._

import java.net.{Inet4Address, Inet6Address, InetAddress}
import java.nio.charset.Charset
import scala.collection.mutable
import scala.concurrent.duration._

trait DnsCodec {

  lazy val ascii: Charset = Charset.forName("US-ASCII")

  def size16(size: Int): Codec[Unit] = constant(BitVector.fromInt(size, 16))

  lazy val characterString: Codec[String]          = variableSizeBytes(uint8, string(ascii))
  lazy val dnsType: Codec[DnsType]                 = uint(1).xmap(DnsType.withValue, _.value)
  lazy val dnsOpCode: Codec[DnsOpCode]             = uint4.xmap(DnsOpCode.withValue, _.value)
  lazy val dnsResponseCode: Codec[DnsResponseCode] = uint4.xmap(DnsResponseCode.withValue, _.value)
  lazy val dnsRecordType: Codec[DnsRecordType]     = uint16.xmap(DnsRecordType.withValue, _.value)
  lazy val dnsRecordClass: Codec[DnsRecordClass]   = uint(15).xmap(DnsRecordClass.withValue, _.value)

  lazy val dnsHeaderCodec: Codec[DnsHeader] = fixedSizeBytes(
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

  lazy val label: Codec[String]        = constant(bin"00") ~> variableSizeBytes(uint(6), string(ascii))
  lazy val pointer: Codec[Int]         = constant(bin"11") ~> uint(14)
  lazy val labels: Codec[List[String]] = variableSizeDelimited(constant(BitVector.lowByte), list(label), 1)
  lazy val domainName: Codec[String]   = labels.xmap(_.mkString("."), _.split('.').toList)

  lazy val dnsQuestionSection: Codec[DnsQuestion] =
    (("qname" | domainName) ::
      ("qtype" | dnsRecordType) ::
      ("unicast-response" | bool) ::
      ("qclass" | dnsRecordClass)).as[DnsQuestion]

  lazy val ttl: Codec[FiniteDuration] = uint32.xmap(_.seconds, _.toSeconds)

  lazy val ipv4: Codec[Inet4Address] =
    bytesStrict(4)
      .xmap[Inet4Address](
        bytes => InetAddress.getByAddress(bytes.toArray).asInstanceOf[Inet4Address],
        ip => ByteVector(ip.getAddress)
      )

  lazy val ipv6: Codec[Inet6Address] = bytesStrict(16).xmap(
    bytes => InetAddress.getByAddress(bytes.toArray).asInstanceOf[Inet6Address],
    ip => ByteVector(ip.getAddress)
  )

  lazy val dnsARecordData: Codec[DnsARecordData]         = ipv4.as[DnsARecordData]
  lazy val dnsAAAARecordData: Codec[DnsAAAARecordData]   = ipv6.as[DnsAAAARecordData]
  lazy val dnsCNAMERecordData: Codec[DnsCNAMERecordData] = domainName.as[DnsCNAMERecordData]
  lazy val dnsHINFORecordData: Codec[DnsHINFORecordData] = (characterString :: characterString).as[DnsHINFORecordData]
  lazy val dnsMXRecordData: Codec[DnsMXRecordData]       = (uint16 :: domainName).as[DnsMXRecordData]

  lazy val dnsNAPTRRecordData: Codec[DnsNAPTRRecordData] =
    (uint16 :: uint16 :: characterString :: characterString :: characterString :: domainName).as[DnsNAPTRRecordData]
  lazy val dnsNSRecordData: Codec[DnsNSRecordData]   = domainName.as[DnsNSRecordData]
  lazy val dnsPTRRecordData: Codec[DnsPTRRecordData] = domainName.as[DnsPTRRecordData]

  lazy val dnsSOARecordData: Codec[DnsSOARecordData] =
    (domainName :: domainName :: uint32 :: ttl :: ttl :: ttl :: ttl).as[DnsSOARecordData]

  lazy val dnsSRVRecordData: Codec[DnsSRVRecordData] =
    (uint16 :: uint16 :: uint16 :: domainName).as[DnsSRVRecordData]
  lazy val dnsTXTRecordData: Codec[DnsTXTRecordData] = vector(characterString).xmap(DnsTXTRecordData, _.txt.toVector)

  def dnsRawRecordData(recordType: DnsRecordType): Codec[DnsRawRecordData] =
    vector(byte).xmap(DnsRawRecordData(recordType, _), _.data.toVector)

  def dnsRecordData(
      recordType: DnsRecordType
  ): DiscriminatorCodec[DnsRecordData, DnsRecordType] =
    discriminated[DnsRecordData]
      .by(provide(recordType))
      .typecase(DnsRecordType.A, dnsARecordData)
      .typecase(DnsRecordType.AAAA, dnsAAAARecordData)
      .typecase(DnsRecordType.CNAME, dnsCNAMERecordData)
      .typecase(DnsRecordType.HINFO, dnsHINFORecordData)
      .typecase(DnsRecordType.MX, dnsMXRecordData)
      .typecase(DnsRecordType.NAPTR, dnsNAPTRRecordData)
      .typecase(DnsRecordType.NS, dnsNSRecordData)
      .typecase(DnsRecordType.PTR, dnsPTRRecordData)
      .typecase(DnsRecordType.SOA, dnsSOARecordData)
      .typecase(DnsRecordType.SRV, dnsSRVRecordData)
      .typecase(DnsRecordType.TXT, dnsTXTRecordData)

  def rdata(recordType: DnsRecordType): Codec[DnsRecordData] =
    variableSizeBytes(
      uint16,
      discriminatorFallback(dnsRawRecordData(recordType), dnsRecordData(recordType)).xmapc {
        case Left(rr)  => rr
        case Right(rr) => rr
      } {
        case rr: DnsRawRecordData => Left(rr)
        case rr: DnsRecordData    => Right(rr)
      }
    )

  lazy val dnsResourceRecord: Codec[DnsResourceRecord] =
    (("name" | domainName) :: ("type" | dnsRecordType))
      .consume[DnsResourceRecord] {
        case name :: recordType :: HNil =>
          (provide(name) ::
            ("cache-flush" | bool) ::
            ("class" | dnsRecordClass) ::
            ("ttl" | ttl) ::
            ("rdata" | rdata(recordType))).as[DnsResourceRecord]
      } { rr =>
        rr.name :: rr.data.`type` :: HNil
      }

  def questionSection(count: Int): Codec[Vector[DnsQuestion]] =
    "qdsection" | vectorOfN(provide(count), dnsQuestionSection)

  def answerSection(count: Int): Codec[Vector[DnsResourceRecord]] =
    "ansection" | vectorOfN(provide(count), dnsResourceRecord)

  def authoritySection(count: Int): Codec[Vector[DnsResourceRecord]] =
    "nssection" | vectorOfN(provide(count), dnsResourceRecord)

  def additionalSection(count: Int): Codec[Vector[DnsResourceRecord]] =
    "arsection" | vectorOfN(provide(count), dnsResourceRecord)
}

class DnsMessageDecoder(bits: BitVector) extends DnsCodec {

  private var stash: Option[BitVector] = None

  private val seenPtrs: mutable.Set[Int] = mutable.Set.empty

  override lazy val labels: Codec[List[String]] = Codec.lazily(
    Codec(
      variableSizeDelimited(constant(BitVector.lowByte), list(label), 1),
      Decoder(
        data =>
          fallback(pointer, label)
            .decode(data)
            .flatMap { result =>
              result.value match {
                case Right("") =>
                  val remainder = stash.getOrElse(result.remainder)
                  stash = None
                  seenPtrs.clear()
                  Successful(DecodeResult(Nil, remainder))
                case Right(label) =>
                  labels.decode(result.remainder).map(_.map(domain => label :: domain))
                case Left(ptr) if seenPtrs.contains(ptr) =>
                  Failure(Err("Name contains a pointer that loops"))
                case Left(ptr) =>
                  if (stash.isEmpty) stash = Some(result.remainder)
                  seenPtrs += ptr
                  labels.decode(bits.drop(ptr * 8L))
              }
            }
      )
    )
  )
}

object DnsCodec extends DnsCodec {

  // format: off
  val dnsMessage: Codec[DnsMessage] = new Codec[DnsMessage] {

    override def sizeBound: SizeBound = SizeBound.atMost(512 * 8)

    override def decode(bits: BitVector): Attempt[DecodeResult[DnsMessage]] = {
      val decoder = new DnsMessageDecoder(bits)
      for {
        header      <- decoder.dnsHeaderCodec.decode(bits)
        questions   <- decoder.questionSection(header.value.countQuestions).decode(header.remainder)
        answers     <- decoder.answerSection(header.value.countAnswerRecords).decode(questions.remainder)
        authorities <- decoder.authoritySection(header.value.countAuthorityRecords).decode(answers.remainder)
        additionals <- decoder.additionalSection(header.value.countAdditionalRecords).decode(authorities.remainder)
      } yield DecodeResult(
        DnsMessage(header.value, questions.value, answers.value, authorities.value, additionals.value),
        additionals.remainder
      )
    }

    override def encode(message: DnsMessage): Attempt[BitVector] = {
      for {
        header      <- dnsHeaderCodec.encode(message.header)
        questions   <- questionSection(message.header.countQuestions).encode(message.questions.toVector)
        answers     <- answerSection(message.header.countAnswerRecords).encode(message.answers.toVector)
        authorities <- authoritySection(message.header.countAuthorityRecords).encode(message.authorities.toVector)
        additionals <- additionalSection(message.header.countAdditionalRecords).encode(message.additionals.toVector)
      } yield header ++ questions ++ answers ++ authorities ++ additionals
    }
  }
  // format: on

}
