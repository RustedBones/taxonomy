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

import fr.davit.taxonomy.record.RecordType

import scala.concurrent.duration.FiniteDuration

sealed abstract class DnsType(val code: Int)

object DnsType {
  case object Query extends DnsType(0)
  case object Response extends DnsType(1)
}

sealed abstract class OpCode(val code: Int)

object OpCode {

  case object StandardQuery extends OpCode(0)
  case object InverseQuery extends OpCode(1)
  case object ServerStatusRequest extends OpCode(2)
  case object Notify extends OpCode(4)
  case object Update extends OpCode(5)
  case object DnsStatefulOperations extends OpCode(5)

  case class Unassigned(override val code: Int) extends OpCode(code) {
    require(code == 3 || (5 <= code && code <= 15))
  }

}

sealed abstract class ResponseCode(val code: Int)

object ResponseCode {

  case object Success extends ResponseCode(0)
  case object FormatError extends ResponseCode(1)
  case object ServerFailure extends ResponseCode(2)
  case object NonExistentDomain extends ResponseCode(3)
  case object NotImplemented extends ResponseCode(4)
  case object Refused extends ResponseCode(5)
  case object ExtraDomain extends ResponseCode(6)
  case object ExtraRRSet extends ResponseCode(7)
  case object NonExistentRRSet extends ResponseCode(8)
  case object NotAuth extends ResponseCode(9)
  case object NotZone extends ResponseCode(10)
  case object DsoTypeNotImplemented extends ResponseCode(11)

  case class Unassigned(override val code: Int) extends ResponseCode(code) {
    require(12 <= code && code <= 255)
  }
}

final case class DnsHeader(
    id: Int,
    `type`: DnsType,
    opCode: OpCode,
    isAuthoritativeAnswer: Boolean,
    isTruncated: Boolean,
    isRecursionDesired: Boolean,
    isRecursionAvailable: Boolean,
    countQuestions: Int,
    countAnswerRecords: Int,
    countAuthorityRecords: Int,
    countAdditionalRecords: Int
)

sealed abstract class RecordClass(val code: Int)

object RecordClass {

  case object Reserved extends RecordClass(0)
  case object Internet extends RecordClass(1)
  case object Chaos extends RecordClass(3)
  case object Hesiod extends RecordClass(4)
  case object Any extends RecordClass(255)
  case class Unassigned(override val code: Int) extends RecordClass(code) {
    require(code == 2 || (5 <= code && code <= 254))
  }
}

final case class Question(
    name: String,
    `type`: RecordType,
    `class`: RecordClass
)

final case class ResourceRecord(name: String, `type`: RecordType, `class`: RecordClass, ttl: FiniteDuration)
