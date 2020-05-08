package fr.davit.taxonomy.fs2

import java.net.InetSocketAddress

import cats.effect.{Blocker, Concurrent, ContextShift, Resource}
import fr.davit.taxonomy.{DnsCodec, DnsMessage, DnsQuery}
import fs2._
import fs2.io.udp.{Packet, Socket, SocketGroup}
import scodec.stream.{StreamDecoder, StreamEncoder}

class DnsClient[F[_]: Concurrent: ContextShift](socket: Socket[F]) {

  def send(address: InetSocketAddress, query: DnsQuery): Stream[F, Unit] =
    Stream(query)
      .through(StreamEncoder.once(DnsCodec.dnsMesage).toPipeByte)
      .chunks
      .map(data => Packet(address, data))
      .through(socket.writes())

  def listen(): Stream[F, DnsMessage] =
    socket
      .reads()
      .flatMap(packet => Stream.chunk(packet.bytes))
      .through(StreamDecoder.once(DnsCodec.dnsMesage).toPipeByte)

  def resolve(address: InetSocketAddress, query: DnsQuery): Stream[F, DnsMessage] =
    send(address, query).drain ++ listen()
}

object DnsClient {

  def bind[F[_]: Concurrent: ContextShift](port: Int = 0): Resource[F, DnsClient[F]] = {
    for {
      blocker     <- Blocker[F]
      socketGroup <- SocketGroup[F](blocker)
      socket      <- socketGroup.open(new InetSocketAddress(port))
    } yield new DnsClient[F](socket)
  }

}
