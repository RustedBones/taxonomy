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

final case class DnsRecordType(code: Int) {
  require(0 <= code && code < 256)
}

object DnsRecordType {
  val A: DnsRecordType          = DnsRecordType(1)
  val NS: DnsRecordType         = DnsRecordType(2)
  val MD: DnsRecordType         = DnsRecordType(3)
  val MF: DnsRecordType         = DnsRecordType(4)
  val CNAME: DnsRecordType      = DnsRecordType(5)
  val SOA: DnsRecordType        = DnsRecordType(6)
  val MB: DnsRecordType         = DnsRecordType(7)
  val MG: DnsRecordType         = DnsRecordType(8)
  val MR: DnsRecordType         = DnsRecordType(9)
  val NULL: DnsRecordType       = DnsRecordType(10)
  val WKS: DnsRecordType        = DnsRecordType(11)
  val PTR: DnsRecordType        = DnsRecordType(12)
  val HINFO: DnsRecordType      = DnsRecordType(13)
  val MINFO: DnsRecordType      = DnsRecordType(14)
  val MX: DnsRecordType         = DnsRecordType(15)
  val TXT: DnsRecordType        = DnsRecordType(16)
  val RP: DnsRecordType         = DnsRecordType(17)
  val AFSDB: DnsRecordType      = DnsRecordType(18)
  val X25: DnsRecordType        = DnsRecordType(19)
  val ISDN: DnsRecordType       = DnsRecordType(20)
  val RT: DnsRecordType         = DnsRecordType(21)
  val NSAP: DnsRecordType       = DnsRecordType(22)
  val `NSAP-PTR`: DnsRecordType = DnsRecordType(23)
  val SIG: DnsRecordType        = DnsRecordType(24)
  val KEY: DnsRecordType        = DnsRecordType(25)
  val PX: DnsRecordType         = DnsRecordType(26)
  val GPOS: DnsRecordType       = DnsRecordType(27)
  val AAAA: DnsRecordType       = DnsRecordType(28)
  val LOC: DnsRecordType        = DnsRecordType(29)
  val NXT: DnsRecordType        = DnsRecordType(30)
  val EID: DnsRecordType        = DnsRecordType(31)
  val NIMLOC: DnsRecordType     = DnsRecordType(32)
  val SRV: DnsRecordType        = DnsRecordType(33)
  val ATMA: DnsRecordType       = DnsRecordType(34)
  val NAPTR: DnsRecordType      = DnsRecordType(35)
  val KX: DnsRecordType         = DnsRecordType(36)
  val CERT: DnsRecordType       = DnsRecordType(37)
  val A6: DnsRecordType         = DnsRecordType(38)
  val DNAME: DnsRecordType      = DnsRecordType(39)
  val SINK: DnsRecordType       = DnsRecordType(40)
  val OPT: DnsRecordType        = DnsRecordType(41)
  val APL: DnsRecordType        = DnsRecordType(42)
  val DS: DnsRecordType         = DnsRecordType(43)
  val SSHFP: DnsRecordType      = DnsRecordType(44)
  val IPSECKEY: DnsRecordType   = DnsRecordType(45)
  val RRSIG: DnsRecordType      = DnsRecordType(46)
  val NSEC: DnsRecordType       = DnsRecordType(47)
  val DNSKEY: DnsRecordType     = DnsRecordType(48)
  val DHCID: DnsRecordType      = DnsRecordType(49)
  val NSEC3: DnsRecordType      = DnsRecordType(50)
  val NSEC3PARAM: DnsRecordType = DnsRecordType(51)
  val TLSA: DnsRecordType       = DnsRecordType(52)
  val SMIMEA: DnsRecordType     = DnsRecordType(53)
  val HIP: DnsRecordType        = DnsRecordType(55)
  val NINFO: DnsRecordType      = DnsRecordType(56)
  val RKEY: DnsRecordType       = DnsRecordType(57)
  val TALINK: DnsRecordType     = DnsRecordType(58)
  val CDS: DnsRecordType        = DnsRecordType(59)
  val CDNSKEY: DnsRecordType    = DnsRecordType(60)
  val OPENPGPKEY: DnsRecordType = DnsRecordType(61)
  val CSYNC: DnsRecordType      = DnsRecordType(62)
  val ZONEMD: DnsRecordType     = DnsRecordType(63)
  val SPF: DnsRecordType        = DnsRecordType(99)
  val UINFO: DnsRecordType      = DnsRecordType(100)
  val UID: DnsRecordType        = DnsRecordType(101)
  val GID: DnsRecordType        = DnsRecordType(102)
  val UNSPEC: DnsRecordType     = DnsRecordType(103)
  val NID: DnsRecordType        = DnsRecordType(104)
  val L32: DnsRecordType        = DnsRecordType(105)
  val L64: DnsRecordType        = DnsRecordType(106)
  val LP: DnsRecordType         = DnsRecordType(107)
  val EUI48: DnsRecordType      = DnsRecordType(108)
  val EUI64: DnsRecordType      = DnsRecordType(109)
  val TKEY: DnsRecordType       = DnsRecordType(249)
  val TSIG: DnsRecordType       = DnsRecordType(250)
  val IXFR: DnsRecordType       = DnsRecordType(251)
  val AXFR: DnsRecordType       = DnsRecordType(252)
  val MAILB: DnsRecordType      = DnsRecordType(253)
  val MAILA: DnsRecordType      = DnsRecordType(254)
  val `*` : DnsRecordType       = DnsRecordType(255)
}
