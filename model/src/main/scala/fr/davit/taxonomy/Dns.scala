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

package fr.davit.taxonomy

import fr.davit.taxonomy.record.DnsRecordType

import scala.concurrent.duration.FiniteDuration

sealed abstract class DnsType(val code: Int)

object DnsType {
  case object Query extends DnsType(0)
  case object Response extends DnsType(1)
}

sealed abstract class DnsOpCode(val code: Int)

object DnsOpCode {

  case object StandardQuery extends DnsOpCode(0)
  case object InverseQuery extends DnsOpCode(1)
  case object ServerStatusRequest extends DnsOpCode(2)
  case object Notify extends DnsOpCode(4)
  case object Update extends DnsOpCode(5)
  case object DnsStatefulOperations extends DnsOpCode(5)

  case class Unassigned(override val code: Int) extends DnsOpCode(code) {
    require(code == 3 || (5 <= code && code <= 15))
  }

}

sealed abstract class DnsResponseCode(val code: Int)

object DnsResponseCode {

  case object Success extends DnsResponseCode(0)
  case object FormatError extends DnsResponseCode(1)
  case object ServerFailure extends DnsResponseCode(2)
  case object NonExistentDomain extends DnsResponseCode(3)
  case object NotImplemented extends DnsResponseCode(4)
  case object Refused extends DnsResponseCode(5)
  case object ExtraDomain extends DnsResponseCode(6)
  case object ExtraRRSet extends DnsResponseCode(7)
  case object NonExistentRRSet extends DnsResponseCode(8)
  case object NotAuth extends DnsResponseCode(9)
  case object NotZone extends DnsResponseCode(10)
  case object DsoTypeNotImplemented extends DnsResponseCode(11)

  case class Unassigned(override val code: Int) extends DnsResponseCode(code) {
    require(12 <= code && code <= 255)
  }
}

final case class DnsHeader(
    id: Int,
    `type`: DnsType,
    opCode: DnsOpCode,
    isAuthoritativeAnswer: Boolean,
    isTruncated: Boolean,
    isRecursionDesired: Boolean,
    isRecursionAvailable: Boolean,
    countQuestions: Int,
    countAnswerRecords: Int,
    countAuthorityRecords: Int,
    countAdditionalRecords: Int
)

sealed abstract class DnsRecordClass(val code: Int)

object DnsRecordClass {

  case object Reserved extends DnsRecordClass(0)
  case object Internet extends DnsRecordClass(1)
  case object Chaos extends DnsRecordClass(3)
  case object Hesiod extends DnsRecordClass(4)
  case object Any extends DnsRecordClass(255)
  case class Unassigned(override val code: Int) extends DnsRecordClass(code) {
    require(code == 2 || (5 <= code && code <= 254))
  }
}

final case class DnsQuestion(
    name: String,
    `type`: DnsRecordType,
    `class`: DnsRecordClass
)

final case class DnsResourceRecord(name: String, `type`: DnsRecordType, `class`: DnsRecordClass, ttl: FiniteDuration)
