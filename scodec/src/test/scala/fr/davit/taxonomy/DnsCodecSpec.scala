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

import java.net.{Inet4Address, InetAddress}

import fr.davit.taxonomy.record.{DnsIpv4AddressRecord, DnsRecordClass, DnsRecordType}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scodec.bits._

import scala.concurrent.duration._

class DnsCodecSpec extends AnyFlatSpec with Matchers {

  "DnsCodec" should "encode / decode dns header" in {
    val header = DnsHeader(
      id = 123,
      `type` = DnsType.Query,
      opCode = DnsOpCode.InverseQuery,
      isAuthoritativeAnswer = false,
      isTruncated = true,
      isRecursionDesired = false,
      isRecursionAvailable = true,
      responseCode = DnsResponseCode(15),
      countQuestions = 1,
      countAnswerRecords = 2,
      countAuthorityRecords = 3,
      countAdditionalRecords = 4
    )

    // format: off
                                 //                                1  1  1  1  1  1
    val data =                   //  0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
      bin"0000000001111011" ++   // |                      ID                       |
        bin"0000101010001111" ++ // |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
        bin"0000000000000001" ++ // |                    QDCOUNT                    |
        bin"0000000000000010" ++ // |                    ANCOUNT                    |
        bin"0000000000000011" ++ // |                    NSCOUNT                    |
        bin"0000000000000100"    // |                    ARCOUNT                    |
    // format: on

    DnsCodec.dnsHeaderCodec.encode(header).require shouldBe data
    DnsCodec.dnsHeaderCodec.decode(data).require.value shouldBe header
  }

  it should "encode / decode domain query name" in {
    val name = "www.example.com"

    val data = (
      ByteVector.fromByte(3) ++ ByteVector("www".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(7) ++ ByteVector("example".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(3) ++ ByteVector("com".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(0) // nul
    ).toBitVector

    DnsCodec.qName.encode(name).require shouldBe data
    DnsCodec.qName.decode(data).require.value shouldBe name

    // double check with name
    DnsCodec.name.encode(name).require shouldBe data
    DnsCodec.name.decode(data).require.value shouldBe name
  }

  it should "encode / decode A record" in {
    val name    = "name"
    val ttl     = 3.hours
    val ipv4    = InetAddress.getByAddress(Array[Byte](1, 2, 3, 4)).asInstanceOf[Inet4Address]
    val aRecord = DnsIpv4AddressRecord(name, ttl, ipv4)

    val data = (
      ByteVector(name.length.toByte) ++ ByteVector(name.getBytes(DnsCodec.ascii)) ++ ByteVector.fromByte(0) ++ // name
        ByteVector.fromInt(DnsRecordType.Ipv4Address.code, 2) ++ // type
        ByteVector.fromInt(DnsRecordClass.Internet.code, 2) ++ // class
        ByteVector.fromLong(ttl.toSeconds, 4) ++ // ttl
        ByteVector.fromInt(4, 2) ++ ByteVector(ipv4.getAddress) // rdlength + rdata
    ).toBitVector

    DnsCodec.dnsResourceRecord.encode(aRecord).require shouldBe data
    DnsCodec.dnsResourceRecord.decode(data).require.value shouldBe aRecord
  }

}
