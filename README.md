# taxonomy

[![Continuous Integration](https://github.com/RustedBones/taxonomy/actions/workflows/ci.yml/badge.svg)](https://github.com/RustedBones/taxonomy/actions/workflows/ci.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.davit/taxonomy-model_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.davit/taxonomy-model_2.13)
[![Software License](https://img.shields.io/badge/license-Apache%202-brightgreen.svg?style=flat)](LICENSE)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

Strongly typed DNS for scala based on the [cats ecosystem](https://github.com/typelevel/cats)
with [scodec](https://github.com/scodec/scodec) 
and [fs2](https://github.com/typelevel/fs2)

## Versions

| Version | Release date | cats version | Scala versions      |
| ------- | ------------ | -----------  | ------------------- |
| `0.3.0` | 2021-01-09   | `2.2.0`      | `2.13.4`, `2.12.12` |
| `0.2.0` | 2020-12-20   | `2.2.0`      | `2.13.4`, `2.12.12` |
| `0.1.0` | 2020-12-17   | `2.2.0`      | `2.13.4`, `2.12.12` |


## Getting taxonomy

```sbt
// DNS with fs2
libraryDependencies += "fr.davit" %% "taxonomy-fs2" % "<version>"
```

If you want to only part of the project with another IO implementation for instance,
you can import the following sub modules

```sbt
// for the scala model only
libraryDependencies += "fr.davit" %% "taxonomy-model"  % "<version>"
// for the binary protocol with scodec
libraryDependencies += "fr.davit" %% "taxonomy-scodec" % "<version>"
```

## Dns

Here is a quick example of a DNS lookup to the `9.9.9.9` DNS server for the `davit.fr` domain name

```scala
import java.net.{Inet4Address, InetAddress, InetSocketAddress}

import cats.effect._
import cats.effect.unsafe.implicits._
import fr.davit.taxonomy.model.record._
import fr.davit.taxonomy.model._
import fr.davit.taxonomy.scodec.DnsCodec
import fs2.io.net.Network
import munit.CatsEffectSuite
import scodec.Codec

import scala.concurrent.duration._

implicit val codec: Codec[DnsMessage] = DnsCodec.dnsMessage

val quad9DnsServer = new InetSocketAddress("9.9.9.9", 53)

val question        = DnsQuestion("davit.fr", DnsRecordType.A, unicastResponse = false, DnsRecordClass.Internet)
val query           = DnsMessage.query(id = 1, questions = Seq(question))
val socketResource  = Network[IO].openDatagramSocket()
val response        = socketResource.use(s => Dns.resolve(s, DnsPacket(quad9DnsServer, query))).unsafeRunSync()
```

## Based on taxonomy

- [`scout`](https://github.com/RustedBones/scout): zeroconf DNS-SD client and server
- [`shovel`](https://github.com/RustedBones/shovel): A JVM dig implementation
