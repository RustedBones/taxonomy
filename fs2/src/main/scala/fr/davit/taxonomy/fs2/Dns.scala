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

import java.net.{InetAddress, InetSocketAddress, NetworkInterface, ProtocolFamily}

import cats.effect._
import cats.implicits._
import fr.davit.taxonomy.fs2.DnsSocketOptions.MulticastGroup
import fr.davit.taxonomy.model.DnsMessage
import fr.davit.taxonomy.scodec.DnsCodec
import fs2._
import fs2.io.udp.{Packet, Socket, SocketGroup}
import scodec.stream.{StreamDecoder, StreamEncoder}

final case class DnsPacket(address: InetSocketAddress, query: DnsMessage)

final case class DnsSocketOptions(
    reuseAddress: Boolean = false,
    sendBufferSize: Option[Int] = None,
    receiveBufferSize: Option[Int] = None,
    allowBroadcast: Boolean = true,
    protocolFamily: Option[ProtocolFamily] = None,
    multicastInterface: Option[NetworkInterface] = None,
    multicastTTL: Option[Int] = None,
    multicastLoopback: Boolean = true,
    multicastGroups: List[MulticastGroup] = List.empty
)

object DnsSocketOptions {

  final case class MulticastGroup(group: InetAddress, interface: NetworkInterface)

  val Defaults: DnsSocketOptions = DnsSocketOptions()

}

object Dns {

  def udpSocket[F[_]: Concurrent: ContextShift](port: Int, options: DnsSocketOptions): Resource[F, Socket[F]] = {
    (for {
      blocker     <- Blocker[F]
      socketGroup <- SocketGroup[F](blocker)
      socket <- socketGroup.open(
        new InetSocketAddress(port),
        options.reuseAddress,
        options.sendBufferSize,
        options.receiveBufferSize,
        options.allowBroadcast,
        options.protocolFamily,
        options.multicastInterface,
        options.multicastTTL,
        options.multicastLoopback
      )
    } yield socket).evalTap(s => options.multicastGroups.traverse(g => s.join(g.group, g.interface).void))
  }

  def resolve[F[_]: Concurrent: ContextShift](
      packet: DnsPacket,
      port: Int = 0,
      options: DnsSocketOptions = DnsSocketOptions.Defaults
  ): F[DnsMessage] =
    udpSocket(port, options).use { socket =>
      for {
        data <- Sync[F].delay(DnsCodec.dnsMesage.encode(packet.query).require)
        datagram = Packet(packet.address, Chunk.byteVector(data.toByteVector))
        _       <- socket.write(datagram)
        p       <- socket.read()
        message <- Sync[F].delay(DnsCodec.dnsMesage.decode(p.bytes.toByteVector.toBitVector).require.value)
      } yield message
    }

  def stream[F[_]: Concurrent: ContextShift](
      input: Stream[F, DnsPacket],
      port: Int = 0,
      options: DnsSocketOptions = DnsSocketOptions.Defaults
  ): Stream[F, Unit] =
    for {
      socket <- Stream.resource(udpSocket(port, options))
      packet <- input
      _ <- Stream(packet.query)
        .through(StreamEncoder.once(DnsCodec.dnsMesage).toPipeByte[F])
        .chunks
        .map(data => Packet(packet.address, data))
        .through(socket.writes())
    } yield ()

  def listen[F[_]: Concurrent: ContextShift](
      port: Int = 0,
      options: DnsSocketOptions = DnsSocketOptions.Defaults
  ): Stream[F, DnsMessage] =
    for {
      socket   <- Stream.resource(udpSocket(port, options))
      datagram <- socket.reads()
      message <- Stream
        .chunk(datagram.bytes)
        .through(StreamDecoder.once(DnsCodec.dnsMesage).toPipeByte[F])
    } yield message
}
