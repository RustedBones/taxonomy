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
  val Ipv4Address: DnsRecordType            = DnsRecordType(1)
  val NameServer: DnsRecordType             = DnsRecordType(2)
  val CanonicalName: DnsRecordType          = DnsRecordType(5)
  val StartOfAuthority: DnsRecordType       = DnsRecordType(6)
  val WellKnownService: DnsRecordType       = DnsRecordType(11)
  val Pointer: DnsRecordType                = DnsRecordType(12)
  val HostInformation: DnsRecordType        = DnsRecordType(13)
  val MailboxInformation: DnsRecordType     = DnsRecordType(14)
  val MailExchange: DnsRecordType           = DnsRecordType(15)
  val Text: DnsRecordType                   = DnsRecordType(16)
  val ResponsiblePerson: DnsRecordType      = DnsRecordType(17)
  val AFSDataBase: DnsRecordType            = DnsRecordType(18)
  val X25: DnsRecordType                    = DnsRecordType(19)
  val ISDN: DnsRecordType                   = DnsRecordType(20)
  val RouteThrough: DnsRecordType           = DnsRecordType(21)
  val NSAP: DnsRecordType                   = DnsRecordType(22)
  val NSAPPointer: DnsRecordType            = DnsRecordType(23)
  val SecuritySignature: DnsRecordType      = DnsRecordType(24)
  val SecurityKey: DnsRecordType            = DnsRecordType(25)
  val PX: DnsRecordType                     = DnsRecordType(26)
  val GeographicalPosition: DnsRecordType   = DnsRecordType(27)
  val Ipv6Address: DnsRecordType            = DnsRecordType(28)
  val Location: DnsRecordType               = DnsRecordType(29)
  val EndpointIdentifier: DnsRecordType     = DnsRecordType(31)
  val NimrodLocator: DnsRecordType          = DnsRecordType(32)
  val ServerSelection: DnsRecordType        = DnsRecordType(33)
  val ATMAddress: DnsRecordType             = DnsRecordType(34)
  val NamingAuthorityPointer: DnsRecordType = DnsRecordType(35)
  val KeyExchanger: DnsRecordType           = DnsRecordType(36)
  val CERT: DnsRecordType                   = DnsRecordType(37)
  val A6: DnsRecordType                     = DnsRecordType(38)
  val DNAME: DnsRecordType                  = DnsRecordType(39)
  val SINK: DnsRecordType                   = DnsRecordType(40)
  val OPT: DnsRecordType                    = DnsRecordType(41)
  val APL: DnsRecordType                    = DnsRecordType(42)
  val DelegationSigner: DnsRecordType       = DnsRecordType(43)
  val SSHFingerPrint: DnsRecordType         = DnsRecordType(44)
  val IPSECKEY: DnsRecordType               = DnsRecordType(45)
  val RRSIG: DnsRecordType                  = DnsRecordType(46)
  val NSEC: DnsRecordType                   = DnsRecordType(47)
  val DNSKEY: DnsRecordType                 = DnsRecordType(48)
  val DHCID: DnsRecordType                  = DnsRecordType(49)
  val NSEC3: DnsRecordType                  = DnsRecordType(50)
  val NSEC3PARAM: DnsRecordType             = DnsRecordType(51)
  val TLSA: DnsRecordType                   = DnsRecordType(52)
  val SMIMEA: DnsRecordType                 = DnsRecordType(53)
  val HostIdentityProtocol: DnsRecordType   = DnsRecordType(55)
  val SPF: DnsRecordType                    = DnsRecordType(99)
  val UINFO: DnsRecordType                  = DnsRecordType(100)
  val UID: DnsRecordType                    = DnsRecordType(101)
  val GID: DnsRecordType                    = DnsRecordType(102)
  val UNSPEC: DnsRecordType                 = DnsRecordType(103)
  val TransactionKey: DnsRecordType         = DnsRecordType(249)
  val TransactionSignature: DnsRecordType   = DnsRecordType(250)
  val IncrementalTransfer: DnsRecordType    = DnsRecordType(251)
  val TransferEntireZone: DnsRecordType     = DnsRecordType(252)
  val `*` : DnsRecordType                   = DnsRecordType(255)
}
