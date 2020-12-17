import sbt._

object Dependencies {

  object Versions {
    val Enumeratum   = "1.6.1"
    val FS2          = "2.4.6"
    val ScalaTest    = "3.2.3"
    val Scodec       = "1.11.7"
    val ScodecStream = "2.0.0"
  }

  val Enumeratum   = "com.beachape" %% "enumeratum"    % Versions.Enumeratum
  val FS2Core      = "co.fs2"       %% "fs2-core"      % Versions.FS2
  val FS2IO        = "co.fs2"       %% "fs2-io"        % Versions.FS2
  val ScodecCore   = "org.scodec"   %% "scodec-core"   % Versions.Scodec
  val ScodecStream = "org.scodec"   %% "scodec-stream" % Versions.ScodecStream

  object Test {
    val ScalaTest = "org.scalatest" %% "scalatest" % Versions.ScalaTest % "it,test"
  }

}
