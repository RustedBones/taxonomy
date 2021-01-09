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

package fr.davit.taxonomy.fs2

import cats.effect._
import fr.davit.taxonomy.model.record.{DnsARecordData, DnsRecordClass, DnsRecordType, DnsResourceRecord}
import fr.davit.taxonomy.model.{DnsMessage, DnsPacket, DnsQuestion, DnsType}
import fr.davit.taxonomy.scodec.DnsCodec
import fs2.io.udp.SocketGroup
import munit.FunSuite
import scodec.Codec

import java.net.{Inet4Address, InetAddress, InetSocketAddress}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class DnsClientItSpec extends FunSuite {

  val quad9DnsServer = new InetSocketAddress("9.9.9.9", 53)

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val codec: Codec[DnsMessage]       = DnsCodec.dnsMessage

  test("lookup queries") {
    val question = DnsQuestion("davit.fr", DnsRecordType.A, unicastResponse = false, DnsRecordClass.Internet)
    val query    = DnsMessage.query(id = 1, questions = Seq(question))
    val socketResource = for {
      blocker     <- Blocker[IO]
      socketGroup <- SocketGroup[IO](blocker)
      socket      <- socketGroup.open[IO]()
    } yield socket

    val response = socketResource.use(s => Dns.resolve(s, DnsPacket(quad9DnsServer, query))).unsafeRunSync()

    val ip = InetAddress.getByName("217.70.184.38").asInstanceOf[Inet4Address]
    val message = DnsMessage(
      query.header.copy(`type` = DnsType.Response, isRecursionAvailable = true),
      query.questions,
      List(DnsResourceRecord("davit.fr", cacheFlush = false, DnsRecordClass.Internet, 3.hours, DnsARecordData(ip))),
      List.empty,
      List.empty
    )
    assertEquals(response, DnsPacket(quad9DnsServer, message))
  }
}
