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
import fr.davit.taxonomy.scodec.DnsCodec
import fs2._
import fs2.io.udp.{Packet, Socket}
import scodec.stream.{StreamDecoder, StreamEncoder}

final case class DnsPacket(address: InetSocketAddress, query: DnsMessage)

object Dns {

  def resolve[F[_]: Concurrent: ContextShift](
      socket: Socket[F],
      packet: DnsPacket
  ): F[DnsMessage] = for {
      data <- Sync[F].delay(DnsCodec.dnsMesage.encode(packet.query).require)
      datagram = Packet(packet.address, Chunk.byteVector(data.toByteVector))
      _       <- socket.write(datagram)
      p       <- socket.read()
      message <- Sync[F].delay(DnsCodec.dnsMesage.decode(p.bytes.toByteVector.toBitVector).require.value)
    } yield message

  def stream[F[_]: Concurrent: ContextShift](socket: Socket[F]): Pipe[F, DnsPacket, Unit] = { input =>
    for {
      packet <- input
      _ <- Stream(packet.query)
        .through(StreamEncoder.once(DnsCodec.dnsMesage).toPipeByte[F])
        .chunks
        .map(data => Packet(packet.address, data))
        .through(socket.writes())
    } yield ()
  }

  def listen[F[_]: Concurrent: ContextShift](socket: Socket[F]): Stream[F, DnsMessage] =
    for {
      datagram <- socket.reads()
      message <- Stream
        .chunk(datagram.bytes)
        .through(StreamDecoder.once(DnsCodec.dnsMesage).toPipeByte[F])
    } yield message
}
