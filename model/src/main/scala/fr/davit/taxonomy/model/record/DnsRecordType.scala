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

enum DnsRecordType(val code: Int):
  case A                      extends DnsRecordType(1)
  case NS                     extends DnsRecordType(2)
  case MD                     extends DnsRecordType(3)
  case MF                     extends DnsRecordType(4)
  case CNAME                  extends DnsRecordType(5)
  case SOA                    extends DnsRecordType(6)
  case MB                     extends DnsRecordType(7)
  case MG                     extends DnsRecordType(8)
  case MR                     extends DnsRecordType(9)
  case NULL                   extends DnsRecordType(10)
  case WKS                    extends DnsRecordType(11)
  case PTR                    extends DnsRecordType(12)
  case HINFO                  extends DnsRecordType(13)
  case MINFO                  extends DnsRecordType(14)
  case MX                     extends DnsRecordType(15)
  case TXT                    extends DnsRecordType(16)
  case RP                     extends DnsRecordType(17)
  case AFSDB                  extends DnsRecordType(18)
  case X25                    extends DnsRecordType(19)
  case ISDN                   extends DnsRecordType(20)
  case RT                     extends DnsRecordType(21)
  case NSAP                   extends DnsRecordType(22)
  case `NSAP-PTR`             extends DnsRecordType(23)
  case SIG                    extends DnsRecordType(24)
  case KEY                    extends DnsRecordType(25)
  case PX                     extends DnsRecordType(26)
  case GPOS                   extends DnsRecordType(27)
  case AAAA                   extends DnsRecordType(28)
  case LOC                    extends DnsRecordType(29)
  case NXT                    extends DnsRecordType(30)
  case EID                    extends DnsRecordType(31)
  case NIMLOC                 extends DnsRecordType(32)
  case SRV                    extends DnsRecordType(33)
  case ATMA                   extends DnsRecordType(34)
  case NAPTR                  extends DnsRecordType(35)
  case KX                     extends DnsRecordType(36)
  case CERT                   extends DnsRecordType(37)
  case A6                     extends DnsRecordType(38)
  case DNAME                  extends DnsRecordType(39)
  case SINK                   extends DnsRecordType(40)
  case OPT                    extends DnsRecordType(41)
  case APL                    extends DnsRecordType(42)
  case DS                     extends DnsRecordType(43)
  case SSHFP                  extends DnsRecordType(44)
  case IPSECKEY               extends DnsRecordType(45)
  case RRSIG                  extends DnsRecordType(46)
  case NSEC                   extends DnsRecordType(47)
  case DNSKEY                 extends DnsRecordType(48)
  case DHCID                  extends DnsRecordType(49)
  case NSEC3                  extends DnsRecordType(50)
  case NSEC3PARAM             extends DnsRecordType(51)
  case TLSA                   extends DnsRecordType(52)
  case SMIMEA                 extends DnsRecordType(53)
  case HIP                    extends DnsRecordType(55)
  case NINFO                  extends DnsRecordType(56)
  case RKEY                   extends DnsRecordType(57)
  case TALINK                 extends DnsRecordType(58)
  case CDS                    extends DnsRecordType(59)
  case CDNSKEY                extends DnsRecordType(60)
  case OPENPGPKEY             extends DnsRecordType(61)
  case CSYNC                  extends DnsRecordType(62)
  case ZONEMD                 extends DnsRecordType(63)
  case SPF                    extends DnsRecordType(99)
  case UINFO                  extends DnsRecordType(100)
  case UID                    extends DnsRecordType(101)
  case GID                    extends DnsRecordType(102)
  case UNSPEC                 extends DnsRecordType(103)
  case NID                    extends DnsRecordType(104)
  case L32                    extends DnsRecordType(105)
  case L64                    extends DnsRecordType(106)
  case LP                     extends DnsRecordType(107)
  case EUI48                  extends DnsRecordType(108)
  case EUI64                  extends DnsRecordType(109)
  case TKEY                   extends DnsRecordType(249)
  case TSIG                   extends DnsRecordType(250)
  case IXFR                   extends DnsRecordType(251)
  case AXFR                   extends DnsRecordType(252)
  case MAILB                  extends DnsRecordType(253)
  case MAILA                  extends DnsRecordType(254)
  case `*`                    extends DnsRecordType(255)
  case Unassigned(value: Int) extends DnsRecordType(value)

object DnsRecordType:
  def apply(code: Int): DnsRecordType = code match
    case 1                     => A
    case 2                     => NS
    case 3                     => MD
    case 4                     => MF
    case 5                     => CNAME
    case 6                     => SOA
    case 7                     => MB
    case 8                     => MG
    case 9                     => MR
    case 10                    => NULL
    case 11                    => WKS
    case 12                    => PTR
    case 13                    => HINFO
    case 14                    => MINFO
    case 15                    => MX
    case 16                    => TXT
    case 17                    => RP
    case 18                    => AFSDB
    case 19                    => X25
    case 20                    => ISDN
    case 21                    => RT
    case 22                    => NSAP
    case 23                    => `NSAP-PTR`
    case 24                    => SIG
    case 25                    => KEY
    case 26                    => PX
    case 27                    => GPOS
    case 28                    => AAAA
    case 29                    => LOC
    case 30                    => NXT
    case 31                    => EID
    case 32                    => NIMLOC
    case 33                    => SRV
    case 34                    => ATMA
    case 35                    => NAPTR
    case 36                    => KX
    case 37                    => CERT
    case 38                    => A6
    case 39                    => DNAME
    case 40                    => SINK
    case 41                    => OPT
    case 42                    => APL
    case 43                    => DS
    case 44                    => SSHFP
    case 45                    => IPSECKEY
    case 46                    => RRSIG
    case 47                    => NSEC
    case 48                    => DNSKEY
    case 49                    => DHCID
    case 50                    => NSEC3
    case 51                    => NSEC3PARAM
    case 52                    => TLSA
    case 53                    => SMIMEA
    case 55                    => HIP
    case 56                    => NINFO
    case 57                    => RKEY
    case 58                    => TALINK
    case 59                    => CDS
    case 60                    => CDNSKEY
    case 61                    => OPENPGPKEY
    case 62                    => CSYNC
    case 63                    => ZONEMD
    case 99                    => SPF
    case 100                   => UINFO
    case 101                   => UID
    case 102                   => GID
    case 103                   => UNSPEC
    case 104                   => NID
    case 105                   => L32
    case 106                   => L64
    case 107                   => LP
    case 108                   => EUI48
    case 109                   => EUI64
    case 249                   => TKEY
    case 250                   => TSIG
    case 251                   => IXFR
    case 252                   => AXFR
    case 253                   => MAILB
    case 254                   => MAILA
    case 255                   => `*`
    case c if 0 < c && c < 256 => Unassigned(c)
    case _                     => throw new IllegalArgumentException(s"Invalid dns record type $code")
