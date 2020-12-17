# taxonomy

[![Continuous Integration](https://github.com/RustedBones/taxonomy/workflows/Continuous%20Integration/badge.svg?branch=master)](https://github.com/RustedBones/taxonomy/actions?query=branch%3Amaster+workflow%3A"Continuous+Integration")
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.davit/taxonomy_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.davit/taxonomy_2.13)
[![Software License](https://img.shields.io/badge/license-Apache%202-brightgreen.svg?style=flat)](LICENSE)

Strongly typed DNS for scala based on the [cats ecosystem](https://github.com/typelevel/cats)
with [scodec](https://github.com/scodec/scodec) 
and [fs2](https://github.com/typelevel/fs2)

## Versions

| Version | Release date | cats version | Scala versions      |
| ------- | ------------ | -----------  | ------------------- |
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

## DNS

Here is a quick example of a DNS lookup to the `9.9.9.9` DNS server for the `davit.fr` domain name

```scala
import java.net.{Inet4Address, InetAddress, InetSocketAddress}

import cats.effect._
import fr.davit.taxonomy.model.record._
import fr.davit.taxonomy.model._
import fr.davit.taxonomy.scodec.DnsCodec
import fs2.io.udp.SocketGroup
import scodec.Codec

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
implicit val codec: Codec[DnsMessage]       = DnsCodec.dnsMessage

val quad9DnsServer = new InetSocketAddress("9.9.9.9", 53)

val question = DnsQuestion("davit.fr", DnsRecordType.A, unicastResponse = false, DnsRecordClass.Internet)
val query    = DnsMessage.query(id = 1, questions = Seq(question))
val socketResource = for {
  blocker     <- Blocker[IO]
  socketGroup <- SocketGroup[IO](blocker)
  socket      <- socketGroup.open[IO]()
} yield socket

val response = socketResource.use(s => Dns.resolve(s, DnsPacket(quad9DnsServer, query))).unsafeRunSync()
```
