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

import enumeratum.ValueEnumMacros
import enumeratum.values.{IntEnum, IntEnumEntry}

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

sealed abstract class DnsType(val value: Int) extends IntEnumEntry

object DnsType extends IntEnum[DnsType] {

  case object Query extends DnsType(0)
  case object Response extends DnsType(1)

  override lazy val values: immutable.IndexedSeq[DnsType] = findValues
}

sealed trait DnsOpCode extends IntEnumEntry

object DnsOpCode extends IntEnum[DnsOpCode] {

  sealed abstract class Assigned(val value: Int) extends DnsOpCode
  final case class Unassigned(value: Int) extends DnsOpCode

  case object StandardQuery extends Assigned(0)
  case object InverseQuery extends Assigned(1)
  case object ServerStatusRequest extends Assigned(2)

  case object Notify extends Assigned(4)
  case object Update extends Assigned(5)
  case object DnsStatefulOperations extends Assigned(6)

  private def assignedValues: immutable.IndexedSeq[Assigned] =
    macro ValueEnumMacros.findIntValueEntriesImpl[Assigned]

  private def unassignedValues: immutable.IndexedSeq[Unassigned] =
    Unassigned(3) +: (7 until 16).map(Unassigned)

  override def values: immutable.IndexedSeq[DnsOpCode] =
    assignedValues ++ unassignedValues
}

sealed trait DnsResponseCode extends IntEnumEntry

object DnsResponseCode extends IntEnum[DnsResponseCode] {

  sealed abstract class Assigned(val value: Int) extends DnsResponseCode
  final case class Unassigned(value: Int) extends DnsResponseCode

  case object Success extends Assigned(0)
  case object FormatError extends Assigned(1)
  case object ServerFailure extends Assigned(2)
  case object NonExistentDomain extends Assigned(3)
  case object NotImplemented extends Assigned(4)
  case object Refused extends Assigned(5)
  case object ExtraDomain extends Assigned(6)
  case object ExtraRRSet extends Assigned(7)
  case object NonExistentRRSet extends Assigned(8)
  case object NotAuth extends Assigned(9)
  case object NotZone extends Assigned(10)
  case object DsoTypeNotImplemented extends Assigned(11)

  private def assignedValues: immutable.IndexedSeq[Assigned] =
    macro ValueEnumMacros.findIntValueEntriesImpl[Assigned]

  private def unassignedValues: immutable.IndexedSeq[Unassigned] =
    (12 until 16).map(Unassigned)

  override lazy val values: immutable.IndexedSeq[DnsResponseCode] =
    assignedValues ++ unassignedValues
}
