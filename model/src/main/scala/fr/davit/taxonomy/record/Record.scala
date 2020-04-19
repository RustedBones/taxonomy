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

sealed abstract class Record(val `type`: RecordType)

object Record {

  case class A(address: Inet4Address) extends Record(RecordType.A)
  case class AAAA(address: Inet6Address) extends Record(RecordType.AAAA)
  case class CNAME(name: String) extends Record(RecordType.CNAME)
  case class HINFO(cpu: String, os: String) extends Record(RecordType.HINFO)
  case class MX(preference: Int, exchange: String) extends Record(RecordType.MX)
  case class NAPTR(order: Int, preference: Int, flags: String, services: String, regexp: String, replacement: String)
      extends Record(RecordType.NAPTR)
  case class NS(name: String) extends Record(RecordType.NS)
  case class OPT(options: List[String]) extends Record(RecordType.OPT)
  case class PTR(name: String) extends Record(RecordType.PTR)
  case class SOA(mname: String, rname: String, serial: Long, refresh: Long, retry: Long, expire: Long, minimum: Long)
      extends Record(RecordType.SOA)
  case class SRV(priority: Int, weight: Int, port: Int, target: String) extends Record(RecordType.SRV)
  case class TXT(txt: immutable.Seq[String]) extends Record(RecordType.TXT)
  case class Raw(override val `type`: RecordType, data: immutable.Seq[Byte]) extends Record(`type`)

}
