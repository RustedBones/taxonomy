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
import munit.FunSuite
import scodec.bits._
import scodec.{Attempt, Err}

import scala.concurrent.duration._
import scala.util.control.NonFatal

class DnsCodecSpec extends FunSuite {

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

  test("encode / decode dns header") {
    val header = DnsHeader(
      id = 123,
      `type` = DnsType.Query,
      opCode = DnsOpCode.InverseQuery,
      isAuthoritativeAnswer = false,
      isTruncated = true,
      isRecursionDesired = false,
      isRecursionAvailable = true,
      responseCode = DnsResponseCode.Unassigned(15)
    )

    // format: off
                                 //                                1  1  1  1  1  1
    val data =                   //  0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
      bin"0000000001111011" ++   // |                      ID                       |
        bin"0000101010001111"    // |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
    // format: on

    assertEquals(DnsCodec.dnsHeader.encode(header).require, data)
    assertEquals(DnsCodec.dnsHeader.complete.decode(data).require.value, header)
  }

  test("encode / decode domain name") {
    val name = "www.example.com"

    val data = (
      ByteVector.fromByte(3) ++ ByteVector("www".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(7) ++ ByteVector("example".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(3) ++ ByteVector("com".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(0) // nul
    ).toBitVector

    assertEquals(DnsCodec.domainName.encode(name).require, data)
    assertEquals(DnsCodec.domainName.complete.decode(data).require.value, name)
  }

  test("decode domain pointers") {
    val webDomain = "www.example.com"
    val appDomain = "app.example.com"
    val prt       = 6.toByte

    val data = (
      ByteVector.fill(prt.toLong)(0) ++ // fake header
        ByteVector.fromByte(7) ++ ByteVector("example".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(3) ++ ByteVector("com".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(0) ++
        ByteVector.fromByte(3) ++ ByteVector("www".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(192.toByte) ++ ByteVector.fromByte(prt) ++
        ByteVector.fromByte(3) ++ ByteVector("app".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(192.toByte) ++ ByteVector.fromByte(prt)
    ).toBitVector
    val messageDecoder = new DnsMessageDecoder(data)
    assertEquals(messageDecoder.domainName.decode(data.drop((6 + 8 + 4 + 1) * 8)).require.value, webDomain)
    assertEquals(messageDecoder.domainName.decode(data.drop((6 + 8 + 4 + 1 + 4 + 2) * 8)).require.value, appDomain)
  }

  test("detect name pointer cycle") {
    val data = (
      ByteVector.fromByte(5) ++ ByteVector("cycle".getBytes(DnsCodec.ascii)) ++
        ByteVector.fromByte(192.toByte) ++ ByteVector.fromByte(0)
    ).toBitVector
    val messageDecoder = new DnsMessageDecoder(data)
    val failure        = Attempt.failure(Err("Name contains a pointer that loops"))
    assertEquals(messageDecoder.domainName.complete.decode(data), failure)
  }

  test("encode / decode A record") {
    val name        = "name"
    val ttl         = 3.hours
    val ipv4        = InetAddress.getByAddress(Array[Byte](1, 2, 3, 4)).asInstanceOf[Inet4Address]
    val aRecordData = DnsARecordData(ipv4)
    val aRecord     = DnsResourceRecord(name, cacheFlush = false, DnsRecordClass.Internet, ttl, aRecordData)

    val data = (
      ByteVector(name.length.toByte) ++ ByteVector(name.getBytes(DnsCodec.ascii)) ++ ByteVector.fromByte(0) ++ // name
        ByteVector.fromInt(DnsRecordType.A.code, 2) ++ // type
        ByteVector.fromInt(DnsRecordClass.Internet.code, 2) ++ // class
        ByteVector.fromLong(ttl.toSeconds, 4) ++ // ttl
        ByteVector.fromInt(4, 2) ++ ByteVector(ipv4.getAddress) // rdlength + rdata
    ).toBitVector
    assertEquals(DnsCodec.dnsResourceRecord.encode(aRecord).require, data)
    assertEquals(DnsCodec.dnsResourceRecord.complete.decode(data).require.value, aRecord)
  }

  test("encode / decode DNS messages") {
    val header = DnsHeader(
      id = 1,
      `type` = DnsType.Query,
      opCode = DnsOpCode.StandardQuery,
      isAuthoritativeAnswer = false,
      isTruncated = false,
      isRecursionDesired = true,
      isRecursionAvailable = false,
      responseCode = DnsResponseCode.Success
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
    assertEquals(DnsCodec.dnsMessage.encode(query).require, queryData)
    assertEquals(DnsCodec.dnsMessage.complete.decode(queryData).require.value, query)

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
        isRecursionAvailable = true
      ),
      List(question),
      List(answer),
      List.empty,
      List.empty
    )

    val data = resourceBin("/response_davit_fr.bin")
    // DnsCodec.dnsMesage.encode(response).require shouldBe data
    assertEquals(DnsCodec.dnsMessage.complete.decode(data).require.value, response)
  }

}
