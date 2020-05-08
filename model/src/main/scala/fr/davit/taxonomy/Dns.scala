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

import java.util.Objects

import fr.davit.taxonomy.record.{DnsRecordClass, DnsRecordType, DnsResourceRecord}

import scala.collection.immutable

final case class DnsQuestion(
    name: String,
    `type`: DnsRecordType,
    `class`: DnsRecordClass
)

trait DnsMessage {
  def header: DnsHeader
  def questions: immutable.Seq[DnsQuestion]
  def answers: immutable.Seq[DnsResourceRecord]
  def authorities: immutable.Seq[DnsResourceRecord]
  def additionals: immutable.Seq[DnsResourceRecord]

  override def equals(other: Any): Boolean = other match {
    case that: DnsMessage =>
      this.header == that.header &&
        this.questions == that.questions &&
        this.answers == that.answers &&
        this.authorities == that.authorities &&
        this.additionals == that.additionals
    case _ => false
  }

  override def hashCode(): Int = Objects.hash(header, questions, answers, authorities, additionals)

}

object DnsMessage {

  private[taxonomy] final case class DnsMessageImpl(
      header: DnsHeader,
      questions: Vector[DnsQuestion],
      answers: Vector[DnsResourceRecord],
      authorities: Vector[DnsResourceRecord],
      additionals: Vector[DnsResourceRecord]
  ) extends DnsMessage

  def apply(
      header: DnsHeader,
      questions: Seq[DnsQuestion],
      answers: Seq[DnsResourceRecord],
      authority: Seq[DnsResourceRecord],
      additional: Seq[DnsResourceRecord]
  ): DnsMessage = DnsMessageImpl(
    header,
    questions.toVector,
    answers.toVector,
    authority.toVector,
    additional.toVector
  )
}

class DnsQuery(id: Int, opCode: DnsOpCode, override val questions: immutable.Seq[DnsQuestion]) extends DnsMessage {

  override def header: DnsHeader = DnsHeader(
    id,
    DnsType.Query,
    opCode,
    isAuthoritativeAnswer = false,
    isTruncated = false, // TODO compute this
    isRecursionDesired = true, // TODO parameter
    isRecursionAvailable = false,
    responseCode = DnsResponseCode.Success,
    countQuestions = questions.size,
    countAnswerRecords = 0,
    countAuthorityRecords = 0,
    countAdditionalRecords = 0
  )

  override def answers: immutable.Seq[DnsResourceRecord] = List.empty

  override def authorities: immutable.Seq[DnsResourceRecord] = List.empty

  override def additionals: immutable.Seq[DnsResourceRecord] = List.empty
}

object DnsQuery {

  def apply(id: Int, opCode: DnsOpCode, questions: Seq[DnsQuestion]): DnsQuery =
    new DnsQuery(id, opCode, questions.toVector)

}

final case class DnsIpv4Lookup(id: Int, names: String*)
    extends DnsQuery(
      id,
      DnsOpCode.StandardQuery,
      names.map(DnsQuestion(_, DnsRecordType.Ipv4Address, DnsRecordClass.Internet)).toVector
    )

final case class DnsIpv6Lookup(id: Int, names: String*)
    extends DnsQuery(
      id,
      DnsOpCode.StandardQuery,
      names.map(DnsQuestion(_, DnsRecordType.Ipv6Address, DnsRecordClass.Internet)).toVector
    )
