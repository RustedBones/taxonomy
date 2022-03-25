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

package fr.davit.taxonomy.model

import scala.collection.immutable

final case class DnsHeader(
    id: Int,
    `type`: DnsType,
    opCode: DnsOpCode,
    isAuthoritativeAnswer: Boolean,
    isTruncated: Boolean,
    isRecursionDesired: Boolean,
    isRecursionAvailable: Boolean,
    responseCode: DnsResponseCode
)

enum DnsType(val code: Int):
  case Query extends DnsType(0)
  case Response extends DnsType(1)

object DnsType:
  def apply(code: Int): DnsType = code match
    case 0 => Query
    case 1 => Response
    case _ => throw new IllegalArgumentException(s"Invalid dns type $code")

enum DnsOpCode(val code: Int):
  case StandardQuery extends DnsOpCode(0)
  case InverseQuery extends DnsOpCode(1)
  case ServerStatusRequest extends DnsOpCode(2)
  case Notify extends DnsOpCode(4)
  case Update extends DnsOpCode(5)
  case DnsStatefulOperations extends DnsOpCode(6)
  case Unassigned(value: Int) extends DnsOpCode(value)

object DnsOpCode:
  def apply(code: Int): DnsOpCode = code match
    case 0                     => StandardQuery
    case 1                     => InverseQuery
    case 2                     => ServerStatusRequest
    case 4                     => Notify
    case 5                     => Update
    case 6                     => DnsStatefulOperations
    case c if 0 <= c && c < 16 => Unassigned(c)
    case _                     => throw new IllegalArgumentException(s"Invalid dns op code $code")

enum DnsResponseCode(val code: Int):
  case Success extends DnsResponseCode(0)
  case FormatError extends DnsResponseCode(1)
  case ServerFailure extends DnsResponseCode(2)
  case NonExistentDomain extends DnsResponseCode(3)
  case NotImplemented extends DnsResponseCode(4)
  case Refused extends DnsResponseCode(5)
  case ExtraDomain extends DnsResponseCode(6)
  case ExtraRRSet extends DnsResponseCode(7)
  case NonExistentRRSet extends DnsResponseCode(8)
  case NotAuth extends DnsResponseCode(9)
  case NotZone extends DnsResponseCode(10)
  case DsoTypeNotImplemented extends DnsResponseCode(11)
  case Unassigned(value: Int) extends DnsResponseCode(value)

object DnsResponseCode:
  def apply(code: Int): DnsResponseCode = code match
    case 0                     => Success
    case 1                     => FormatError
    case 2                     => ServerFailure
    case 3                     => NonExistentDomain
    case 4                     => NotImplemented
    case 5                     => Refused
    case 6                     => ExtraDomain
    case 7                     => ExtraRRSet
    case 8                     => NonExistentRRSet
    case 9                     => NotAuth
    case 10                    => NotZone
    case 11                    => DsoTypeNotImplemented
    case c if 0 <= c && c < 16 => Unassigned(c)
    case _                     => throw new IllegalArgumentException(s"Invalid dns response code $code")
