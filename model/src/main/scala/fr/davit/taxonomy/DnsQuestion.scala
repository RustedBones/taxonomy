package fr.davit.taxonomy

import fr.davit.taxonomy.record.{DnsRecordClass, DnsRecordType}

final case class DnsQuestion(
    name: String,
    `type`: DnsRecordType,
    `class`: DnsRecordClass
)
