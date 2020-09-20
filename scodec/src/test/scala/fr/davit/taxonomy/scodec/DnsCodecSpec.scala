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

import java.net.{Inet4Address, InetAddress}

import fr.davit.taxonomy.model.record.{DnsARecordData, DnsRecordClass, DnsRecordType, DnsResourceRecord}
import fr.davit.taxonomy.model._
import fr.davit.taxonomy.scodec.DnsCodec.DnsBits
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scodec.bits._
import scodec.{Attempt, Err}

import scala.concurrent.duration._
import scala.util.control.NonFatal

class DnsCodecSpec extends AnyFlatSpec with Matchers {

  def resourceBin(path: String): BitVector = {
    val responseInputStream = getClass.getResourceAsStream(path)
    try {
      BitVector.fromInputStream(responseInputStream)
    } catch {
      case NonFatal(e) =>
        responseInputStream.close()
        throw e
    }
  }

  "DnsCodec" should "encode / decode dns header" in {
    val header = DnsHeader(
      id = 123,
      `type` = DnsType.Query,
      opCode = DnsOpCode.InverseQuery,
      isAuthoritativeAnswer = false,
      isTruncated = true,
      isRecursionDesired = false,
      isRecursionAvailable = true,
      responseCode = DnsResponseCode.Unassigned(15),
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
    DnsCodec.dnsHeaderCodec.complete.decode(data).require.value shouldBe header
  }

  it should "encode / decode domain name" in {
    val name = "www.example.com"

    val data = (
      ByteVector.fromByte(3) ++ ByteVector("www".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(7) ++ ByteVector("example".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(3) ++ ByteVector("com".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(0) // nul
    ).toBitVector
    implicit val dnsBit: DnsBits = DnsBits(data)

    DnsCodec.domainName.encode(name).require shouldBe data
    DnsCodec.domainName.complete.decode(data).require.value shouldBe name
  }

  it should "decode domain pointer" in {
    val name = "www.example.com"

    val data = (
      ByteVector.fill(6)(0) ++ // fake header
        ByteVector.fromByte(7) ++ ByteVector("example".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(3) ++ ByteVector("com".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(0) ++
        ByteVector.fromByte(3) ++ ByteVector("www".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(192.toByte) ++ ByteVector.fromByte(6)
    ).toBitVector
    val dnsBit: DnsBits = DnsBits(data)
    DnsCodec.domainName(dnsBit).complete.decode(data.drop((6 + 8 + 4 + 1) * 8)).require.value shouldBe name
  }

  it should "detect name pointer cycle" in {
    val data = (
      ByteVector.fromByte(5) ++ ByteVector("cycle".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(192.toByte) ++ ByteVector.fromByte(0)
    ).toBitVector
    val dnsBit: DnsBits = DnsBits(data)
    DnsCodec.domainName(dnsBit).complete.decode(data) shouldBe Attempt.failure(
      Err("Name contains a pointer that loops")
    )
  }

  it should "encode / decode A record" in {
    val name        = "name"
    val ttl         = 3.hours
    val ipv4        = InetAddress.getByAddress(Array[Byte](1, 2, 3, 4)).asInstanceOf[Inet4Address]
    val aRecordData = DnsARecordData(ipv4)
    val aRecord     = DnsResourceRecord(name, cacheFlush = false, DnsRecordClass.Internet, ttl, aRecordData)

    val data = (
      ByteVector(name.length.toByte) ++ ByteVector(name.getBytes(DnsCodec.ascii)) ++ ByteVector.fromByte(0) ++ // name
        ByteVector.fromInt(DnsRecordType.A.value, 2) ++ // type
        ByteVector.fromInt(DnsRecordClass.Internet.value, 2) ++ // class
        ByteVector.fromLong(ttl.toSeconds, 4) ++ // ttl
        ByteVector.fromInt(4, 2) ++ ByteVector(ipv4.getAddress) // rdlength + rdata
    ).toBitVector
    implicit val dnsBit: DnsBits = DnsBits(data)

    DnsCodec.dnsResourceRecord.encode(aRecord).require shouldBe data
    DnsCodec.dnsResourceRecord.complete.decode(data).require.value shouldBe aRecord
  }

  it should "encode / decode DNS messages" in {
    val header = DnsHeader(
      id = 1,
      `type` = DnsType.Query,
      opCode = DnsOpCode.StandardQuery,
      isAuthoritativeAnswer = false,
      isTruncated = false,
      isRecursionDesired = true,
      isRecursionAvailable = false,
      responseCode = DnsResponseCode.Success,
      countQuestions = 1,
      countAnswerRecords = 0,
      countAuthorityRecords = 0,
      countAdditionalRecords = 0
    )
    val question = DnsQuestion(
      name = "davit.fr",
      `type` = DnsRecordType.A,
      unicastResponse = false,
      `class` = DnsRecordClass.Internet
    )

    val query = DnsMessage(
      header,
      List(question),
      List.empty,
      List.empty,
      List.empty
    )
    val queryData = resourceBin("/query_davit_fr.bin")
    DnsCodec.dnsMesage.encode(query).require shouldBe queryData
    DnsCodec.dnsMesage.complete.decode(queryData).require.value shouldBe query

    val answer = DnsResourceRecord(
      name = "davit.fr",
      cacheFlush = false,
      `class` = DnsRecordClass.Internet,
      3.hours,
      DnsARecordData(InetAddress.getByName("217.70.184.38").asInstanceOf[Inet4Address])
    )
    val response = DnsMessage(
      header.copy(
        `type` = DnsType.Response,
        isRecursionDesired = true,
        isRecursionAvailable = true,
        countAnswerRecords = 1
      ),
      List(question),
      List(answer),
      List.empty,
      List.empty
    )

    val data = resourceBin("/response_davit_fr.bin")
    // DnsCodec.dnsMesage.encode(response).require shouldBe data
    DnsCodec.dnsMesage.complete.decode(data).require.value shouldBe response
  }

}
