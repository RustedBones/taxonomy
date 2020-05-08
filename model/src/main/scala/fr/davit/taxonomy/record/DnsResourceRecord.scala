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

package fr.davit.taxonomy.record

import java.net.{Inet4Address, Inet6Address, InetAddress}
import java.util.Objects

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

trait DnsResourceRecord {
  def name: String
  def `type`: DnsRecordType
  def `class`: DnsRecordClass
  def ttl: FiniteDuration
  def data: immutable.Seq[Byte]

  override def equals(other: Any): Boolean = other match {
    case that: DnsResourceRecord =>
      this.name == that.name &&
        this.`type` == that.`type` &&
        this.`class` == that.`class` &&
        this.ttl == that.ttl &&
        this.data == that.data
    case _ => false
  }

  override def hashCode(): Int = Objects.hash(name, `type`, `class`, ttl, data)

}

object DnsResourceRecord {

  private[taxonomy] case class DnsResourceRecordImpl(
      name: String,
      `type`: DnsRecordType,
      `class`: DnsRecordClass,
      ttl: FiniteDuration,
      data: Vector[Byte]
  ) extends DnsResourceRecord

  def apply(
      name: String,
      `type`: DnsRecordType,
      `class`: DnsRecordClass,
      ttl: FiniteDuration,
      data: Seq[Byte]
  ): DnsResourceRecord = DnsResourceRecordImpl(name, `type`, `class`, ttl, data.toVector)

}

trait DnsInternetRecord extends DnsResourceRecord {
  override def `class`: DnsRecordClass = DnsRecordClass.Internet
}

final case class DnsIpv4AddressRecord(name: String, ttl: FiniteDuration, address: Inet4Address)
    extends DnsInternetRecord {
  override def `type`: DnsRecordType     = DnsRecordType.Ipv4Address
  override def data: immutable.Seq[Byte] = address.getAddress.toList
}

object DnsIpv4AddressRecord {

  def unapply(record: DnsResourceRecord): Option[(String, FiniteDuration, Inet4Address)] = {
    if (record.`class` == DnsRecordClass.Internet && record.`type` == DnsRecordType.Ipv4Address) {
      val address = InetAddress.getByAddress(null, record.data.toArray).asInstanceOf[Inet4Address]
      Some((record.name, record.ttl, address))
    } else {
      None
    }
  }

}

final case class DnsIpv6AddressRecord(name: String, ttl: FiniteDuration, address: Inet6Address)
    extends DnsInternetRecord {
  override def `type`: DnsRecordType     = DnsRecordType.Ipv4Address
  override def data: immutable.Seq[Byte] = address.getAddress.toList
}

object DnsIpv6AddressRecord {

  def unapply(record: DnsResourceRecord): Option[(String, FiniteDuration, Inet6Address)] = {
    if (record.`class` == DnsRecordClass.Internet && record.`type` == DnsRecordType.Ipv6Address) {
      val address = InetAddress.getByAddress(null, record.data.toArray).asInstanceOf[Inet6Address]
      Some((record.name, record.ttl, address))
    } else {
      None
    }
  }

}

//case class CNAME(name: String, ttl: FiniteDuration, cname: String) extends DnsInternetRecord {
//  override def `type`: DnsRecordType     = DnsRecordType.A
//  override def data: immutable.Seq[Byte] = ???
//}
//
//case class DnsHINFORecord(name: String, ttl: FiniteDuration, cpu: String, os: String) extends DnsInternetRecord {
//  override def `type`: DnsRecordType = ???
//
//  override def data: immutable.Seq[Byte] = ???
//}
//case class DnsMXRecord(name: String, ttl: FiniteDuration, preference: Int, exchange: String) extends DnsInternetRecord {
//  override def `type`: DnsRecordType = ???
//
//  override def data: immutable.Seq[Byte] = ???
//}
//case class DnsNAPTRRecord(
//    name: String,
//    ttl: FiniteDuration,
//    order: Int,
//    preference: Int,
//    flags: String,
//    services: String,
//    regexp: String,
//    replacement: String
//) extends DnsInternetRecord {
//  override def `type`: DnsRecordType = ???
//
//  override def data: immutable.Seq[Byte] = ???
//}
//case class DnsNSRecord(name: String, ttl: FiniteDuration, nsdname: String) extends DnsInternetRecord {
//  override def `type`: DnsRecordType = ???
//
//  override def data: immutable.Seq[Byte] = ???
//}
//case class DnsOPTRecord(name: String, ttl: FiniteDuration, options: List[String]) extends DnsInternetRecord {
//  override def `type`: DnsRecordType = ???
//
//  override def data: immutable.Seq[Byte] = ???
//}
//case class DnsPTRRecord(name: String, ttl: FiniteDuration, ptrdname: String) extends DnsInternetRecord {
//  override def `type`: DnsRecordType = ???
//
//  override def data: immutable.Seq[Byte] = ???
//}
//case class DnsSOARecord(
//    name: String,
//    ttl: FiniteDuration,
//    mname: String,
//    rname: String,
//    serial: Long,
//    refresh: Long,
//    retry: Long,
//    expire: Long,
//    minimum: Long
//) extends DnsInternetRecord {
//  override def `type`: DnsRecordType = ???
//
//  override def data: immutable.Seq[Byte] = ???
//}
//case class DnsSRVRecord(name: String, ttl: FiniteDuration, priority: Int, weight: Int, port: Int, target: String)
//    extends DnsInternetRecord {
//  override def `type`: DnsRecordType = ???
//
//  override def data: immutable.Seq[Byte] = ???
//}
//case class DnsTXTRecord(name: String, ttl: FiniteDuration, txt: immutable.Seq[String]) extends DnsInternetRecord {
//  override def `type`: DnsRecordType = ???
//
//  override def data: immutable.Seq[Byte] = ???
//}
