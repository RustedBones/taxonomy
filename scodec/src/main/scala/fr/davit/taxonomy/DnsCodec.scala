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
import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}
import shapeless._

import scala.concurrent.duration._

trait DnsCodec {

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

  val domainName: Codec[String] = Codec(
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
  val dnsSRVRecordData: Codec[DnsSRVRecordData] = (uint16 :: uint16 :: uint16 :: qName).as[DnsSRVRecordData]
  val dnsTXTRecordData: Codec[DnsTXTRecordData] = vector(characterString).xmap(DnsTXTRecordData, _.txt.toVector)

  def dnsRawRecordData(recordType: DnsRecordType): Codec[DnsRawRecordData] =
    variableSizeBytes(uint16, vector(byte)).xmap(DnsRawRecordData(recordType, _), _.data.toVector)

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

  val dnsMesage: Codec[DnsMessage] = dnsHeaderCodec
    .flatPrepend { header =>
      ("qdsection" | vectorOfN(provide(header.countQuestions), dnsQuestionSection)) ::
        ("ansection" | vectorOfN(provide(header.countAnswerRecords), dnsResourceRecord)) ::
        ("nssection" | vectorOfN(provide(header.countAuthorityRecords), dnsResourceRecord)) ::
        ("arsection" | vectorOfN(provide(header.countAdditionalRecords), dnsResourceRecord))
    }
    .xmapc {
      case header :: questions :: answers :: authorities :: additionals :: HNil =>
        DnsMessage(header, questions, answers, authorities, additionals)
    } { message =>
      import message._
      header :: questions.toVector :: answers.toVector :: authorities.toVector :: additionals.toVector :: HNil
    }
}

object DnsCodec extends DnsCodec
