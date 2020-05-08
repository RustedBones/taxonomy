package fr.davit.taxonomy.fs2

import java.net.{Inet4Address, InetAddress, InetSocketAddress}

import cats.effect._
import fr.davit.taxonomy.record.DnsIpv4AddressRecord
import fr.davit.taxonomy.{DnsIpv4Lookup, DnsMessage, DnsType}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class DnsClientItSpec extends AnyFlatSpec with Matchers {

  val quad9DnsServer = new InetSocketAddress("9.9.9.9", 53)

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  "DnsClient" should "send DNS queries" in {
    val query = DnsIpv4Lookup(1, "davit.fr")
    val response = DnsClient.bind[IO]()
      .use(_.resolve(quad9DnsServer, query).compile.toList)
      .unsafeRunSync()
      .head

    val ip = InetAddress.getByName("217.70.184.38").asInstanceOf[Inet4Address]
    response shouldBe DnsMessage(
      query.header.copy(`type` = DnsType.Response, isRecursionAvailable = true, countAnswerRecords = 1),
      query.questions,
      Seq(DnsIpv4AddressRecord("12", 3.hours, ip)), // TODO fix label ptr
      Seq.empty,
      Seq.empty
    )
  }
}
