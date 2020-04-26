package fr.davit.taxonomy

import fr.davit.taxonomy.record.{DnsRecordClass, DnsRecordType}

import scala.concurrent.duration.FiniteDuration

final case class DnsQuestion(
    name: String,
    `type`: DnsRecordType,
    `class`: DnsRecordClass
)

final case class DnsResourceRecord(name: String, `type`: DnsRecordType, `class`: DnsRecordClass, ttl: FiniteDuration)
