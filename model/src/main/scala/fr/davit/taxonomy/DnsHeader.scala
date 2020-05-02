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

final case class DnsType(code: Int) {
  require(code == 0 || code == 1)
}

object DnsType {

  val Query: DnsType    = DnsType(0)
  val Response: DnsType = DnsType(1)

  def apply(code: Boolean): DnsType = DnsType(if (code) 1 else 0)
}

final case class DnsOpCode(code: Int) {
  require(0 <= code && code < 16)
}

object DnsOpCode {

  val StandardQuery: DnsOpCode         = DnsOpCode(0)
  val InverseQuery: DnsOpCode          = DnsOpCode(1)
  val ServerStatusRequest: DnsOpCode   = DnsOpCode(2)
  val Notify: DnsOpCode                = DnsOpCode(4)
  val Update: DnsOpCode                = DnsOpCode(5)
  val DnsStatefulOperations: DnsOpCode = DnsOpCode(5)

}

final case class DnsResponseCode(code: Int) {
  require(0 <= code && code < 16)
}

object DnsResponseCode {

  val Success: DnsResponseCode               = DnsResponseCode(0)
  val FormatError: DnsResponseCode           = DnsResponseCode(1)
  val ServerFailure: DnsResponseCode         = DnsResponseCode(2)
  val NonExistentDomain: DnsResponseCode     = DnsResponseCode(3)
  val NotImplemented: DnsResponseCode        = DnsResponseCode(4)
  val Refused: DnsResponseCode               = DnsResponseCode(5)
  val ExtraDomain: DnsResponseCode           = DnsResponseCode(6)
  val ExtraRRSet: DnsResponseCode            = DnsResponseCode(7)
  val NonExistentRRSet: DnsResponseCode      = DnsResponseCode(8)
  val NotAuth: DnsResponseCode               = DnsResponseCode(9)
  val NotZone: DnsResponseCode               = DnsResponseCode(10)
  val DsoTypeNotImplemented: DnsResponseCode = DnsResponseCode(11)
}

final case class DnsHeader(
    id: Int,
    `type`: DnsType,
    opCode: DnsOpCode,
    isAuthoritativeAnswer: Boolean,
    isTruncated: Boolean,
    isRecursionDesired: Boolean,
    isRecursionAvailable: Boolean,
    responseCode: DnsResponseCode,
    countQuestions: Int,
    countAnswerRecords: Int,
    countAuthorityRecords: Int,
    countAdditionalRecords: Int
)
