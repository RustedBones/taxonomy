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

import java.net.{Inet4Address, Inet6Address, InetAddress}
import java.nio.charset.Charset

import fr.davit.taxonomy.record._
import scodec.bits._
import scodec.codecs._
import scodec.{Attempt, Codec, DecodeResult, SizeBound}
import shapeless._

import scala.annotation.tailrec
import scala.concurrent.duration._

trait DnsCodec {

  import DnsCodec._

  val ascii: Charset = Charset.forName("US-ASCII")

  def size16(size: Int): Codec[Unit] = constant(BitVector.fromInt(size, 16))

  val characterString: Codec[String]          = variableSizeBytes(uint8, string(ascii))
  val dnsType: Codec[DnsType]                 = uint(1).xmap(DnsType.withValue, _.value)
  val dnsOpCode: Codec[DnsOpCode]             = uint4.xmap(DnsOpCode.withValue, _.value)
  val dnsResponseCode: Codec[DnsResponseCode] = uint4.xmap(DnsResponseCode.withValue, _.value)
  val dnsRecordType: Codec[DnsRecordType]     = uint16.xmap(DnsRecordType.withValue, _.value)
  val dnsRecordClass: Codec[DnsRecordClass]   = uint16.xmap(DnsRecordClass.withValue, _.value)

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
  val pointer: Codec[Int]  = constant(bin"11") ~> uint(14)

  val domainName: Codec[String] = new Codec[Domain] {

    override def sizeBound: SizeBound = SizeBound.unknown

    override def encode(domain: Domain): Attempt[BitVector] = {

      @tailrec
      def encodeRec(domain: Domain, attemptBuffer: Attempt[BitVector]): Attempt[BitVector] =
        (attemptBuffer, domain) match {
          case (f: Attempt.Failure, _)                       => f
          case (Attempt.Successful(buf), Domain.Root)        => Attempt.successful(buf ++ BitVector.lowByte)
          case (Attempt.Successful(buf), Domain.Label(v, d)) => encodeRec(d, label.encode(v).map(buf ++ _))
          case (Attempt.Successful(buf), Domain.Pointer(o))  => pointer.encode(o).map(buf ++ _)
        }

      encodeRec(domain, Attempt.successful(BitVector.empty))
    }

    override def decode(bits: BitVector): Attempt[DecodeResult[Domain]] = {

      @tailrec
      def decodeRec(attemptBuf: Attempt[DecodeResult[Domain]]): Attempt[DecodeResult[Domain]] =
        attemptBuf match {
          case f: Attempt.Failure => f
          case Attempt.Successful(DecodeResult(domain, remainder)) =>
            fallback(pointer, label).decode(remainder) match {
              case f: Attempt.Failure =>
                f
              case Attempt.Successful(DecodeResult(Left(p), r)) =>
                Attempt.Successful(DecodeResult(domain.reverseAndPrepend(Domain.Pointer(p)), r))
              case Attempt.Successful(DecodeResult(Right(""), r)) =>
                Attempt.Successful(DecodeResult(domain.reverseAndPrepend(Domain.Root), r))
              case Attempt.Successful(DecodeResult(Right(l), r)) =>
                decodeRec(Attempt.Successful(DecodeResult(Domain.Label(l, domain), r)))
            }
        }

      decodeRec(Attempt.successful(DecodeResult(Domain.Root, bits)))
    }
  }.xmap(_.toString, Domain.apply)

  val dnsQuestionSection: Codec[DnsQuestion] =
    (("qname" | domainName) ::
      ("qtype" | dnsRecordType) ::
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

  val dnsARecordData: Codec[DnsARecordData]         = ipv4.as[DnsARecordData]
  val dnsAAAARecordData: Codec[DnsAAAARecordData]   = ipv6.as[DnsAAAARecordData]
  val dnsCNAMERecordData: Codec[DnsCNAMERecordData] = domainName.as[DnsCNAMERecordData]
  val dnsHINFORecordData: Codec[DnsHINFORecordData] = (characterString :: characterString).as[DnsHINFORecordData]
  val dnsMXRecordData: Codec[DnsMXRecordData]       = (uint16 :: domainName).as[DnsMXRecordData]

  val dnsNAPTRRecordData: Codec[DnsNAPTRRecordData] =
    (uint16 :: uint16 :: characterString :: characterString :: characterString :: domainName).as[DnsNAPTRRecordData]
  val dnsNSRecordData: Codec[DnsNSRecordData]   = domainName.as[DnsNSRecordData]
  val dnsPTRRecordData: Codec[DnsPTRRecordData] = domainName.as[DnsPTRRecordData]

  val dnsSOARecordData: Codec[DnsSOARecordData] =
    (domainName :: domainName :: uint32 :: ttl :: ttl :: ttl :: ttl).as[DnsSOARecordData]
  val dnsSRVRecordData: Codec[DnsSRVRecordData] = (uint16 :: uint16 :: uint16 :: domainName).as[DnsSRVRecordData]
  val dnsTXTRecordData: Codec[DnsTXTRecordData] = vector(characterString).xmap(DnsTXTRecordData, _.txt.toVector)

  def dnsRawRecordData(recordType: DnsRecordType): Codec[DnsRawRecordData] =
    vector(byte).xmap(DnsRawRecordData(recordType, _), _.data.toVector)

  def dnsRecordData(recordType: DnsRecordType): DiscriminatorCodec[DnsRecordData, DnsRecordType] =
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

  val dnsResourceRecord: Codec[DnsResourceRecord] = (("name" | domainName) :: ("type" | dnsRecordType))
    .consume[DnsResourceRecord] {
      case name :: recordType :: HNil =>
        (provide(name) :: ("class" | dnsRecordClass) :: ("ttl" | ttl) :: ("rdata" | rdata(recordType)))
          .as[DnsResourceRecord]
    } { rr =>
      rr.name :: rr.data.`type` :: HNil
    }

  val dnsMesage: Codec[DnsMessage] = new Codec[DnsMessage] {

    private def questionSection(count: Int)   = "qdsection" | vectorOfN(provide(count), dnsQuestionSection)
    private def answerSection(count: Int)     = "ansection" | vectorOfN(provide(count), dnsResourceRecord)
    private def authoritySection(count: Int)  = "nssection" | vectorOfN(provide(count), dnsResourceRecord)
    private def additionalSection(count: Int) = "arsection" | vectorOfN(provide(count), dnsResourceRecord)

    override def sizeBound: SizeBound = SizeBound.atMost(512 * 8)

    override def decode(bits: BitVector): Attempt[DecodeResult[DnsMessage]] = {
      implicit val dnsMesageBits = DnsMessageBits(bits)
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

    override def encode(message: DnsMessage): Attempt[BitVector] =
      for {
        header      <- dnsHeaderCodec.encode(message.header)
        questions   <- questionSection(message.header.countQuestions).encode(message.questions.toVector)
        answers     <- answerSection(message.header.countAnswerRecords).encode(message.answers.toVector)
        authorities <- authoritySection(message.header.countAuthorityRecords).encode(message.authorities.toVector)
        additionals <- additionalSection(message.header.countAdditionalRecords).encode(message.additionals.toVector)
      } yield header ++ questions ++ answers ++ authorities ++ additionals
  }
}

object DnsCodec extends DnsCodec {

  final case class DnsMessageBits(bits: BitVector)

  sealed trait Domain {
    def reverseAndPrepend(d: Domain): Domain
  }

  object Domain {

    def apply(domain: String): Domain = domain.split('.').foldRight[Domain](Domain.Root) {
      case ("", Root) => Root
      case (l, d)     => Label(l, d)
    }

    final case object Root extends Domain {
      def reverseAndPrepend(d: Domain): Domain = d
      override def toString: String            = "."
    }
    final case class Pointer(offest: Int) extends Domain {
      def reverseAndPrepend(d: Domain): Domain = d
      override def toString: String            = s"$offest"
    }
    final case class Label(value: String, next: Domain = Root) extends Domain {
      def reverseAndPrepend(d: Domain): Domain = next.reverseAndPrepend(Label(value, d))
      override def toString: String = next match {
        case Root => value
        case _    => s"$value.$next"
      }
    }
  }

}
