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

import java.net.{Inet4Address, Inet6Address}

import scala.collection.immutable

sealed abstract class DnsRecord(val `type`: DnsRecordType)

object DnsRecord {

  case class A(address: Inet4Address) extends DnsRecord(DnsRecordType.A)
  case class AAAA(address: Inet6Address) extends DnsRecord(DnsRecordType.AAAA)
  case class CNAME(name: String) extends DnsRecord(DnsRecordType.CNAME)
  case class HINFO(cpu: String, os: String) extends DnsRecord(DnsRecordType.HINFO)
  case class MX(preference: Int, exchange: String) extends DnsRecord(DnsRecordType.MX)
  case class NAPTR(order: Int, preference: Int, flags: String, services: String, regexp: String, replacement: String)
      extends DnsRecord(DnsRecordType.NAPTR)
  case class NS(name: String) extends DnsRecord(DnsRecordType.NS)
  case class OPT(options: List[String]) extends DnsRecord(DnsRecordType.OPT)
  case class PTR(name: String) extends DnsRecord(DnsRecordType.PTR)
  case class SOA(mname: String, rname: String, serial: Long, refresh: Long, retry: Long, expire: Long, minimum: Long)
      extends DnsRecord(DnsRecordType.SOA)
  case class SRV(priority: Int, weight: Int, port: Int, target: String) extends DnsRecord(DnsRecordType.SRV)
  case class TXT(txt: immutable.Seq[String]) extends DnsRecord(DnsRecordType.TXT)
  case class Raw(override val `type`: DnsRecordType, data: immutable.Seq[Byte]) extends DnsRecord(`type`)

}
