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

package fr.davit.taxonomy.model.record

import java.net.{Inet4Address, Inet6Address}

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

final case class DnsResourceRecord(
    name: String,
    cacheFlush: Boolean,
    `class`: DnsRecordClass,
    ttl: FiniteDuration,
    data: DnsRecordData
)

enum DnsRecordClass(val code: Int):
  case Internet extends DnsRecordClass(1)
  case Chaos extends DnsRecordClass(3)
  case Hesiod extends DnsRecordClass(4)
  case None extends DnsRecordClass(234)
  case Any extends DnsRecordClass(255)
  case Unassigned(value: Int) extends DnsRecordClass(value)

object DnsRecordClass:
  def apply(code: Int): DnsRecordClass = code match
    case 1                      => Internet
    case 3                      => Chaos
    case 4                      => Hesiod
    case 254                    => None
    case 255                    => Any
    case c if 0 <= c && c < 256 => Unassigned(c)

abstract class DnsRecordData(val `type`: DnsRecordType)

// format: off
final case class DnsARecordData(address: Inet4Address) extends DnsRecordData(DnsRecordType.A)
final case class DnsAAAARecordData(address: Inet6Address) extends DnsRecordData(DnsRecordType.AAAA)
final case class DnsCNAMERecordData(cname: String) extends DnsRecordData(DnsRecordType.CNAME)
final case class DnsHINFORecordData(cpu: String, os: String) extends DnsRecordData(DnsRecordType.HINFO)
final case class DnsMXRecordData(preference: Int, exchange: String) extends DnsRecordData(DnsRecordType.MX)
final case class DnsNAPTRRecordData(order: Int, preference: Int, flags: String, services: String, regexp: String, replacement: String) extends DnsRecordData(DnsRecordType.NAPTR)
final case class DnsNSRecordData(nsdname: String) extends DnsRecordData(DnsRecordType.NS)
final case class DnsPTRRecordData(ptrdname: String) extends DnsRecordData(DnsRecordType.PTR)
final case class DnsSOARecordData(mname: String, rname: String, serial: Long, refresh: FiniteDuration, retry: FiniteDuration, expire: FiniteDuration, minimum: FiniteDuration) extends DnsRecordData(DnsRecordType.SOA)
final case class DnsSRVRecordData(priority: Int, weight: Int, port: Int, target: String) extends DnsRecordData(DnsRecordType.SRV)
final case class DnsTXTRecordData(txt: immutable.Seq[String]) extends DnsRecordData(DnsRecordType.TXT)

final case class DnsRawRecordData(override val `type`: DnsRecordType, data: immutable.Seq[Byte]) extends DnsRecordData(`type`)
// format: on
