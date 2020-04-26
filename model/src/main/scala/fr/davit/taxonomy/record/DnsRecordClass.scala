package fr.davit.taxonomy.record

final case class DnsRecordClass(code: Int) {
  require(0 <= code && code < 256)
}

object DnsRecordClass {

  val Reserved: DnsRecordClass = DnsRecordClass(0)
  val Internet: DnsRecordClass = DnsRecordClass(1)
  val Chaos: DnsRecordClass    = DnsRecordClass(3)
  val Hesiod: DnsRecordClass   = DnsRecordClass(4)
  val Any: DnsRecordClass      = DnsRecordClass(255)

}
