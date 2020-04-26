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

sealed abstract class DnsRecordType(val code: Int)

object DnsRecordType {
  case object A extends DnsRecordType(1)
  case object NS extends DnsRecordType(2)
  case object MD extends DnsRecordType(3)
  case object MF extends DnsRecordType(4)
  case object CNAME extends DnsRecordType(5)
  case object SOA extends DnsRecordType(6)
  case object MB extends DnsRecordType(7)
  case object MG extends DnsRecordType(8)
  case object MR extends DnsRecordType(9)
  case object NULL extends DnsRecordType(10)
  case object WKS extends DnsRecordType(11)
  case object PTR extends DnsRecordType(12)
  case object HINFO extends DnsRecordType(13)
  case object MINFO extends DnsRecordType(14)
  case object MX extends DnsRecordType(15)
  case object TXT extends DnsRecordType(16)
  case object RP extends DnsRecordType(17)
  case object AFSDB extends DnsRecordType(18)
  case object X25 extends DnsRecordType(19)
  case object ISDN extends DnsRecordType(20)
  case object RT extends DnsRecordType(21)
  case object NSAP extends DnsRecordType(22)
  case object `NSAP-PTR` extends DnsRecordType(23)
  case object SIG extends DnsRecordType(24)
  case object KEY extends DnsRecordType(25)
  case object PX extends DnsRecordType(26)
  case object GPOS extends DnsRecordType(27)
  case object AAAA extends DnsRecordType(28)
  case object LOC extends DnsRecordType(29)
  case object NXT extends DnsRecordType(30)
  case object EID extends DnsRecordType(31)
  case object NIMLOC extends DnsRecordType(32)
  case object SRV extends DnsRecordType(33)
  case object ATMA extends DnsRecordType(34)
  case object NAPTR extends DnsRecordType(35)
  case object KX extends DnsRecordType(36)
  case object CERT extends DnsRecordType(37)
  case object A6 extends DnsRecordType(38)
  case object DNAME extends DnsRecordType(39)
  case object SINK extends DnsRecordType(40)
  case object OPT extends DnsRecordType(41)
  case object APL extends DnsRecordType(42)
  case object DS extends DnsRecordType(43)
  case object SSHFP extends DnsRecordType(44)
  case object IPSECKEY extends DnsRecordType(45)
  case object RRSIG extends DnsRecordType(46)
  case object NSEC extends DnsRecordType(47)
  case object DNSKEY extends DnsRecordType(48)
  case object DHCID extends DnsRecordType(49)
  case object NSEC3 extends DnsRecordType(50)
  case object NSEC3PARAM extends DnsRecordType(51)
  case object TLSA extends DnsRecordType(52)
  case object SMIMEA extends DnsRecordType(53)
  case object HIP extends DnsRecordType(55)
  case object NINFO extends DnsRecordType(56)
  case object RKEY extends DnsRecordType(57)
  case object TALINK extends DnsRecordType(58)
  case object CDS extends DnsRecordType(59)
  case object CDNSKEY extends DnsRecordType(60)
  case object OPENPGPKEY extends DnsRecordType(61)
  case object CSYNC extends DnsRecordType(62)
  case object ZONEMD extends DnsRecordType(63)
  case object SPF extends DnsRecordType(99)
  case object UINFO extends DnsRecordType(100)
  case object UID extends DnsRecordType(101)
  case object GID extends DnsRecordType(102)
  case object UNSPEC extends DnsRecordType(103)
  case object NID extends DnsRecordType(104)
  case object L32 extends DnsRecordType(105)
  case object L64 extends DnsRecordType(106)
  case object LP extends DnsRecordType(107)
  case object EUI48 extends DnsRecordType(108)
  case object EUI64 extends DnsRecordType(109)
  case object TKEY extends DnsRecordType(249)
  case object TSIG extends DnsRecordType(250)
  case object IXFR extends DnsRecordType(251)
  case object AXFR extends DnsRecordType(252)
  case object MAILB extends DnsRecordType(253)
  case object MAILA extends DnsRecordType(254)
  case object `*` extends DnsRecordType(255)
  case class Unassigned(override val code: Int) extends DnsRecordType(code) {
    require(code == 54 || (64 <= code && code <= 98) || (110 <= code && code <= 248))
  }
}
