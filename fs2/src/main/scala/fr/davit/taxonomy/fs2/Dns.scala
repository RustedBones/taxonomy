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

package fr.davit.taxonomy.fs2

import java.net.InetSocketAddress

import cats.effect._
import cats.implicits._
import fr.davit.taxonomy.model.DnsMessage
import fs2._
import fs2.io.udp.{Packet, Socket}
import scodec.Codec
import scodec.stream.{StreamDecoder, StreamEncoder}
import sun.net.dns.ResolverConfiguration

final case class DnsPacket(address: InetSocketAddress, query: DnsMessage)

object Dns {

  def resolverConfiguration[F[_]: Sync]: Resource[F, ResolverConfiguration] =
    Resource.make(Sync[F].delay(ResolverConfiguration.open()))(_ => Sync[F].unit)

  def resolve[F[_]: Concurrent: ContextShift](
      socket: Socket[F],
      packet: DnsPacket
  )(implicit codec: Codec[DnsMessage]): F[DnsMessage] =
    for {
      data <- Sync[F].delay(codec.encode(packet.query).require)
      datagram = Packet(packet.address, Chunk.byteVector(data.toByteVector))
      _       <- socket.write(datagram)
      p       <- socket.read()
      message <- Sync[F].delay(codec.decode(p.bytes.toByteVector.toBitVector).require.value)
    } yield message

  def stream[F[_]: Concurrent: ContextShift](
      socket: Socket[F]
  )(implicit codec: Codec[DnsMessage]): Pipe[F, DnsPacket, Unit] = { input =>
    for {
      packet <- input
      _ <- Stream(packet.query)
        .through(StreamEncoder.once(codec).toPipeByte[F])
        .chunks
        .map(data => Packet(packet.address, data))
        .through(socket.writes())
    } yield ()
  }

  def listen[F[_]: Concurrent: ContextShift](
      socket: Socket[F]
  )(implicit codec: Codec[DnsMessage]): Stream[F, DnsMessage] =
    for {
      datagram <- socket.reads()
      message <- Stream
        .chunk(datagram.bytes)
        .through(StreamDecoder.once(codec).toPipeByte[F])
    } yield message
}
