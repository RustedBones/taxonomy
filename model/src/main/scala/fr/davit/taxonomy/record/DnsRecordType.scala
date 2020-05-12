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

import enumeratum.ValueEnumMacros
import enumeratum.values.{IntEnum, IntEnumEntry}

import scala.collection.immutable
import scala.language.experimental.macros

sealed trait DnsRecordType extends IntEnumEntry

object DnsRecordType extends IntEnum[DnsRecordType] {

  sealed abstract class Assigned(val value: Int) extends DnsRecordType
  final case class Unassigned(value: Int) extends DnsRecordType

  case object A extends Assigned(1)
  case object NS extends Assigned(2)
  case object MD extends Assigned(3)
  case object MF extends Assigned(4)
  case object CNAME extends Assigned(5)
  case object SOA extends Assigned(6)
  case object MB extends Assigned(7)
  case object MG extends Assigned(8)
  case object MR extends Assigned(9)
  case object NULL extends Assigned(10)
  case object WKS extends Assigned(11)
  case object PTR extends Assigned(12)
  case object HINFO extends Assigned(13)
  case object MINFO extends Assigned(14)
  case object MX extends Assigned(15)
  case object TXT extends Assigned(16)
  case object RP extends Assigned(17)
  case object AFSDB extends Assigned(18)
  case object X25 extends Assigned(19)
  case object ISDN extends Assigned(20)
  case object RT extends Assigned(21)
  case object NSAP extends Assigned(22)
  case object `NSAP-PTR` extends Assigned(23)
  case object SIG extends Assigned(24)
  case object KEY extends Assigned(25)
  case object PX extends Assigned(26)
  case object GPOS extends Assigned(27)
  case object AAAA extends Assigned(28)
  case object LOC extends Assigned(29)
  case object NXT extends Assigned(30)
  case object EID extends Assigned(31)
  case object NIMLOC extends Assigned(32)
  case object SRV extends Assigned(33)
  case object ATMA extends Assigned(34)
  case object NAPTR extends Assigned(35)
  case object KX extends Assigned(36)
  case object CERT extends Assigned(37)
  case object A6 extends Assigned(38)
  case object DNAME extends Assigned(39)
  case object SINK extends Assigned(40)
  case object OPT extends Assigned(41)
  case object APL extends Assigned(42)
  case object DS extends Assigned(43)
  case object SSHFP extends Assigned(44)
  case object IPSECKEY extends Assigned(45)
  case object RRSIG extends Assigned(46)
  case object NSEC extends Assigned(47)
  case object DNSKEY extends Assigned(48)
  case object DHCID extends Assigned(49)
  case object NSEC3 extends Assigned(50)
  case object NSEC3PARAM extends Assigned(51)
  case object TLSA extends Assigned(52)
  case object SMIMEA extends Assigned(53)
  case object HIP extends Assigned(55)
  case object NINFO extends Assigned(56)
  case object RKEY extends Assigned(57)
  case object TALINK extends Assigned(58)
  case object CDS extends Assigned(59)
  case object CDNSKEY extends Assigned(60)
  case object OPENPGPKEY extends Assigned(61)
  case object CSYNC extends Assigned(62)
  case object ZONEMD extends Assigned(63)
  case object SPF extends Assigned(99)
  case object UINFO extends Assigned(100)
  case object UID extends Assigned(101)
  case object GID extends Assigned(102)
  case object UNSPEC extends Assigned(103)
  case object NID extends Assigned(104)
  case object L32 extends Assigned(105)
  case object L64 extends Assigned(106)
  case object LP extends Assigned(107)
  case object EUI48 extends Assigned(108)
  case object EUI64 extends Assigned(109)
  case object TKEY extends Assigned(249)
  case object TSIG extends Assigned(250)
  case object IXFR extends Assigned(251)
  case object AXFR extends Assigned(252)
  case object MAILB extends Assigned(253)
  case object MAILA extends Assigned(254)
  case object `*` extends Assigned(255)

  private def assignedValues: immutable.IndexedSeq[Assigned] =
    macro ValueEnumMacros.findIntValueEntriesImpl[Assigned]

  private def unassignedValues: immutable.IndexedSeq[Unassigned] =
    Unassigned(54) +: ((64 to 98).map(Unassigned) ++ (110 until 248).map(Unassigned))

  override lazy val values: immutable.IndexedSeq[DnsRecordType] =
    assignedValues ++ unassignedValues

}
