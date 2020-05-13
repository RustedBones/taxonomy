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

import fr.davit.taxonomy.record.{DnsRecordClass, DnsRecordType, DnsResourceRecord}

import scala.collection.immutable

final case class DnsQuestion(
    name: String,
    `type`: DnsRecordType,
    `class`: DnsRecordClass
)

final case class DnsMessage(
    header: DnsHeader,
    questions: immutable.Seq[DnsQuestion],
    answers: immutable.Seq[DnsResourceRecord],
    authorities: immutable.Seq[DnsResourceRecord],
    additionals: immutable.Seq[DnsResourceRecord]
)

object DnsMessage {

  def query(
      id: Int = 0,
      opCode: DnsOpCode = DnsOpCode.StandardQuery,
      isRecursionDesired: Boolean = true,
      questions: Seq[DnsQuestion] = Seq.empty
  ): DnsMessage = {
    val header = DnsHeader(
      id,
      DnsType.Query,
      opCode,
      isAuthoritativeAnswer = false,
      isTruncated = false,
      isRecursionDesired = isRecursionDesired,
      isRecursionAvailable = false,
      responseCode = DnsResponseCode.Success,
      countQuestions = questions.size,
      countAnswerRecords = 0,
      countAuthorityRecords = 0,
      countAdditionalRecords = 0
    )

    DnsMessage(
      header,
      questions.toVector,
      Vector.empty,
      Vector.empty,
      Vector.empty
    )
  }
}
