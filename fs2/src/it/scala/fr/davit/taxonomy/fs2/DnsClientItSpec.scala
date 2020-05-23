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

import java.net.{Inet4Address, InetAddress, InetSocketAddress}

import cats.effect._
import fr.davit.taxonomy.model.record.{DnsARecordData, DnsRecordClass, DnsRecordType, DnsResourceRecord}
import fr.davit.taxonomy.model.{DnsMessage, DnsQuestion, DnsType}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class DnsClientItSpec extends AnyFlatSpec with Matchers {

  val quad9DnsServer = new InetSocketAddress("9.9.9.9", 53)

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  "Dns" should "lookup DNS queries" in {
    val question = DnsQuestion("davit.fr", DnsRecordType.A, DnsRecordClass.Internet)
    val query    = DnsMessage.query(id = 1, questions = Seq(question))
    val response = Dns
      .resolve[IO](DnsPacket(quad9DnsServer, query))
      .unsafeRunSync()

    val ip = InetAddress.getByName("217.70.184.38").asInstanceOf[Inet4Address]
    response shouldBe DnsMessage(
      query.header.copy(`type` = DnsType.Response, isRecursionAvailable = true, countAnswerRecords = 1),
      query.questions,
      Seq(DnsResourceRecord("davit.fr", DnsRecordClass.Internet, 3.hours, DnsARecordData(ip))), // TODO fix label ptr
      Seq.empty,
      Seq.empty
    )
  }
}
