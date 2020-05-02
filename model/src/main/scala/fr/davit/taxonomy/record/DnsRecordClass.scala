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
