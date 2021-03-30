import sbt._

object Dependencies {

  object Versions {
    val Enumeratum   = "1.6.1"
    val FS2          = "2.5.3"
    val MUnit        = "0.7.22"
    val Scodec       = "1.11.7"
    val ScodecStream = "2.0.1"
  }

  val Enumeratum   = "com.beachape" %% "enumeratum"    % Versions.Enumeratum
  val FS2Core      = "co.fs2"       %% "fs2-core"      % Versions.FS2
  val FS2IO        = "co.fs2"       %% "fs2-io"        % Versions.FS2
  val ScodecCore   = "org.scodec"   %% "scodec-core"   % Versions.Scodec
  val ScodecStream = "org.scodec"   %% "scodec-stream" % Versions.ScodecStream

  object Test {
    val MUnit = "org.scalameta" %% "munit" % Versions.MUnit % "it,test"
  }

}
