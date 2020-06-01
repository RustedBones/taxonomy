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

import java.net.{Inet4Address, Inet6Address, InetAddress}
import java.nio.charset.Charset

import fr.davit.taxonomy.model.record._
import _root_.scodec.bits._
import _root_.scodec.codecs._
import _root_.scodec.{Attempt, Codec, DecodeResult, Decoder, Err, SizeBound}
import fr.davit.taxonomy.model.{DnsHeader, DnsMessage, DnsOpCode, DnsQuestion, DnsResponseCode, DnsType}
import fr.davit.taxonomy.scodec.DnsCodec.DnsBits
import shapeless._

import scala.annotation.tailrec
import scala.concurrent.duration._

trait DnsCodec {

  val ascii: Charset = Charset.forName("US-ASCII")

  def size16(size: Int): Codec[Unit] = constant(BitVector.fromInt(size, 16))

  val characterString: Codec[String]          = variableSizeBytes(uint8, string(ascii))
  val dnsType: Codec[DnsType]                 = uint(1).xmap(DnsType.withValue, _.value)
  val dnsOpCode: Codec[DnsOpCode]             = uint4.xmap(DnsOpCode.withValue, _.value)
  val dnsResponseCode: Codec[DnsResponseCode] = uint4.xmap(DnsResponseCode.withValue, _.value)
  val dnsRecordType: Codec[DnsRecordType]     = uint16.xmap(DnsRecordType.withValue, _.value)
  val dnsRecordClass: Codec[DnsRecordClass]   = uint(15).xmap(DnsRecordClass.withValue, _.value)

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

  val label: Codec[String] = constant(bin"00") ~> variableSizeBytes(uint(6), string(ascii))
  val pointer: Codec[Int]  = constant(bin"11") ~> uint(14)

  def labels(implicit dnsBits: DnsBits, ptrs: Set[Int] = Set.empty): Codec[List[String]] = Codec(
    list(label) <~ constant(BitVector.lowByte),
    new Decoder[List[String]] {

      override def decode(bits: BitVector): Attempt[DecodeResult[List[String]]] = {
        @tailrec
        def go(rem: BitVector, buf: List[String]): Attempt[DecodeResult[List[String]]] =
          fallback(pointer, label).decode(rem) match {
            case f: Attempt.Failure =>
              f
            case Attempt.Successful(DecodeResult(Left(p), r)) =>
              // pointer
              if (ptrs.contains(p)) {
                Attempt.failure(Err("Domain name pointer cycle detected"))
              } else {
                labels(dnsBits, ptrs + p)
                  .decode(dnsBits.bits.drop(p * 8))
                  .map(result => DecodeResult(buf.reverse ++ result.value, r))
              }
            case Attempt.Successful(DecodeResult(Right(""), r)) =>
              // stop
              Attempt.successful(DecodeResult(buf.reverse, r))
            case Attempt.Successful(DecodeResult(Right(l), r)) =>
              // label
              go(r, l :: buf)
          }

        go(bits, List.empty)
      }
    }
  )

  def domainName(implicit dnsBits: DnsBits): Codec[String] = labels.xmap(_.mkString("."), _.split('.').toList)

  def dnsQuestionSection(implicit dnsBits: DnsBits): Codec[DnsQuestion] =
    (("qname" | domainName) ::
      ("qtype" | dnsRecordType) ::
      ("unicast-response" | bool) ::
      ("qclass" | dnsRecordClass)).as[DnsQuestion]

  val ttl: Codec[FiniteDuration] = uint32.xmap(_.seconds, _.toSeconds)

  val ipv4: Codec[Inet4Address] = bytesStrict(4)
    .xmap[Inet4Address](
      bytes => InetAddress.getByAddress(bytes.toArray).asInstanceOf[Inet4Address],
      ip => ByteVector(ip.getAddress)
    )

  val ipv6: Codec[Inet6Address] = bytesStrict(16).xmap(
    bytes => InetAddress.getByAddress(bytes.toArray).asInstanceOf[Inet6Address],
    ip => ByteVector(ip.getAddress)
  )

  val dnsARecordData: Codec[DnsARecordData]                                    = ipv4.as[DnsARecordData]
  val dnsAAAARecordData: Codec[DnsAAAARecordData]                              = ipv6.as[DnsAAAARecordData]
  def dnsCNAMERecordData(implicit dnsBits: DnsBits): Codec[DnsCNAMERecordData] = domainName.as[DnsCNAMERecordData]
  val dnsHINFORecordData: Codec[DnsHINFORecordData]                            = (characterString :: characterString).as[DnsHINFORecordData]
  def dnsMXRecordData(implicit dnsBits: DnsBits): Codec[DnsMXRecordData]       = (uint16 :: domainName).as[DnsMXRecordData]

  def dnsNAPTRRecordData(implicit dnsBits: DnsBits): Codec[DnsNAPTRRecordData] =
    (uint16 :: uint16 :: characterString :: characterString :: characterString :: domainName).as[DnsNAPTRRecordData]
  def dnsNSRecordData(implicit dnsBits: DnsBits): Codec[DnsNSRecordData]   = domainName.as[DnsNSRecordData]
  def dnsPTRRecordData(implicit dnsBits: DnsBits): Codec[DnsPTRRecordData] = domainName.as[DnsPTRRecordData]

  def dnsSOARecordData(implicit dnsBits: DnsBits): Codec[DnsSOARecordData] =
    (domainName :: domainName :: uint32 :: ttl :: ttl :: ttl :: ttl).as[DnsSOARecordData]

  def dnsSRVRecordData(implicit dnsBits: DnsBits): Codec[DnsSRVRecordData] =
    (uint16 :: uint16 :: uint16 :: domainName).as[DnsSRVRecordData]
  val dnsTXTRecordData: Codec[DnsTXTRecordData] = vector(characterString).xmap(DnsTXTRecordData, _.txt.toVector)

  def dnsRawRecordData(recordType: DnsRecordType): Codec[DnsRawRecordData] =
    vector(byte).xmap(DnsRawRecordData(recordType, _), _.data.toVector)

  def dnsRecordData(
      recordType: DnsRecordType
  )(implicit dnsBits: DnsBits): DiscriminatorCodec[DnsRecordData, DnsRecordType] =
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

  def rdata(recordType: DnsRecordType)(implicit dnsBits: DnsBits): Codec[DnsRecordData] =
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

  def dnsResourceRecord(implicit dnsBits: DnsBits): Codec[DnsResourceRecord] =
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

  val dnsMesage: Codec[DnsMessage] = new Codec[DnsMessage] {

    private def questionSection(count: Int)(implicit dnsBits: DnsBits) =
      "qdsection" | vectorOfN(provide(count), dnsQuestionSection)
    private def answerSection(count: Int)(implicit dnsBits: DnsBits) =
      "ansection" | vectorOfN(provide(count), dnsResourceRecord)
    private def authoritySection(count: Int)(implicit dnsBits: DnsBits) =
      "nssection" | vectorOfN(provide(count), dnsResourceRecord)
    private def additionalSection(count: Int)(implicit dnsBits: DnsBits) =
      "arsection" | vectorOfN(provide(count), dnsResourceRecord)

    override def sizeBound: SizeBound = SizeBound.atMost(512 * 8)

    override def decode(bits: BitVector): Attempt[DecodeResult[DnsMessage]] = {
      implicit val dnsBits: DnsBits = DnsBits(bits)
      for {
        header      <- dnsHeaderCodec.decode(bits)
        questions   <- questionSection(header.value.countQuestions).decode(header.remainder)
        answers     <- answerSection(header.value.countAnswerRecords).decode(questions.remainder)
        authorities <- authoritySection(header.value.countAuthorityRecords).decode(answers.remainder)
        additionals <- additionalSection(header.value.countAdditionalRecords).decode(authorities.remainder)
      } yield DecodeResult(
        DnsMessage(header.value, questions.value, answers.value, authorities.value, additionals.value),
        additionals.remainder
      )
    }

    override def encode(message: DnsMessage): Attempt[BitVector] = {
      implicit val dnsBits: DnsBits = DnsBits(BitVector.empty) // not used for encoding
      for {
        header      <- dnsHeaderCodec.encode(message.header)
        questions   <- questionSection(message.header.countQuestions).encode(message.questions.toVector)
        answers     <- answerSection(message.header.countAnswerRecords).encode(message.answers.toVector)
        authorities <- authoritySection(message.header.countAuthorityRecords).encode(message.authorities.toVector)
        additionals <- additionalSection(message.header.countAdditionalRecords).encode(message.additionals.toVector)
      } yield header ++ questions ++ answers ++ authorities ++ additionals
    }
  }
}

object DnsCodec extends DnsCodec {

  final case class DnsBits(bits: BitVector)
}
