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

sealed abstract class RecordType(val code: Int)

object RecordType {
  case object A extends RecordType(1)
  case object NS extends RecordType(2)
  case object MD extends RecordType(3)
  case object MF extends RecordType(4)
  case object CNAME extends RecordType(5)
  case object SOA extends RecordType(6)
  case object MB extends RecordType(7)
  case object MG extends RecordType(8)
  case object MR extends RecordType(9)
  case object NULL extends RecordType(10)
  case object WKS extends RecordType(11)
  case object PTR extends RecordType(12)
  case object HINFO extends RecordType(13)
  case object MINFO extends RecordType(14)
  case object MX extends RecordType(15)
  case object TXT extends RecordType(16)
  case object RP extends RecordType(17)
  case object AFSDB extends RecordType(18)
  case object X25 extends RecordType(19)
  case object ISDN extends RecordType(20)
  case object RT extends RecordType(21)
  case object NSAP extends RecordType(22)
  case object `NSAP-PTR` extends RecordType(23)
  case object SIG extends RecordType(24)
  case object KEY extends RecordType(25)
  case object PX extends RecordType(26)
  case object GPOS extends RecordType(27)
  case object AAAA extends RecordType(28)
  case object LOC extends RecordType(29)
  case object NXT extends RecordType(30)
  case object EID extends RecordType(31)
  case object NIMLOC extends RecordType(32)
  case object SRV extends RecordType(33)
  case object ATMA extends RecordType(34)
  case object NAPTR extends RecordType(35)
  case object KX extends RecordType(36)
  case object CERT extends RecordType(37)
  case object A6 extends RecordType(38)
  case object DNAME extends RecordType(39)
  case object SINK extends RecordType(40)
  case object OPT extends RecordType(41)
  case object APL extends RecordType(42)
  case object DS extends RecordType(43)
  case object SSHFP extends RecordType(44)
  case object IPSECKEY extends RecordType(45)
  case object RRSIG extends RecordType(46)
  case object NSEC extends RecordType(47)
  case object DNSKEY extends RecordType(48)
  case object DHCID extends RecordType(49)
  case object NSEC3 extends RecordType(50)
  case object NSEC3PARAM extends RecordType(51)
  case object TLSA extends RecordType(52)
  case object SMIMEA extends RecordType(53)
  case object HIP extends RecordType(55)
  case object NINFO extends RecordType(56)
  case object RKEY extends RecordType(57)
  case object TALINK extends RecordType(58)
  case object CDS extends RecordType(59)
  case object CDNSKEY extends RecordType(60)
  case object OPENPGPKEY extends RecordType(61)
  case object CSYNC extends RecordType(62)
  case object ZONEMD extends RecordType(63)
  case object SPF extends RecordType(99)
  case object UINFO extends RecordType(100)
  case object UID extends RecordType(101)
  case object GID extends RecordType(102)
  case object UNSPEC extends RecordType(103)
  case object NID extends RecordType(104)
  case object L32 extends RecordType(105)
  case object L64 extends RecordType(106)
  case object LP extends RecordType(107)
  case object EUI48 extends RecordType(108)
  case object EUI64 extends RecordType(109)
  case object TKEY extends RecordType(249)
  case object TSIG extends RecordType(250)
  case object IXFR extends RecordType(251)
  case object AXFR extends RecordType(252)
  case object MAILB extends RecordType(253)
  case object MAILA extends RecordType(254)
  case object `*` extends RecordType(255)
  case class Unassigned(override val code: Int) extends RecordType(code) {
    require(code == 54 || (64 <= code && code <= 98) || (110 <= code && code <= 248))
  }
}
