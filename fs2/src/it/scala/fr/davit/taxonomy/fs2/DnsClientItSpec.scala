package fr.davit.taxonomy.fs2

import java.net.{Inet4Address, InetAddress, InetSocketAddress}

import cats.effect._
import fr.davit.taxonomy.record.{DnsARecordData, DnsRecordClass, DnsRecordType, DnsResourceRecord}
import fr.davit.taxonomy.{DnsMessage, DnsQuestion, DnsType}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class DnsClientItSpec extends AnyFlatSpec with Matchers {

  val quad9DnsServer = new InetSocketAddress("9.9.9.9", 53)

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  "DnsFs2" should "lookup DNS queries" in {
    val question = DnsQuestion("davit.fr", DnsRecordType.A, DnsRecordClass.Internet)
    val query    = DnsMessage.query(id = 1, questions = Seq(question))
    val response = DnsFs2
      .resolve[IO](DnsPacket(quad9DnsServer, query))
      .unsafeRunSync()

    val ip = InetAddress.getByName("217.70.184.38").asInstanceOf[Inet4Address]
    response shouldBe DnsMessage(
      query.header.copy(`type` = DnsType.Response, isRecursionAvailable = true, countAnswerRecords = 1),
      query.questions,
      Seq(DnsResourceRecord("12", DnsRecordClass.Internet, 3.hours, DnsARecordData(ip))), // TODO fix label ptr
      Seq.empty,
      Seq.empty
    )
  }
}
