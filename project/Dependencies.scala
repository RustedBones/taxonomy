import sbt._

object Dependencies {

  object Versions {
    val Enumeratum   = "1.6.1"
    val FS2          = "3.0.2"
    val MUnitCE3     = "1.0.2"
    val Scodec       = "1.11.7"
    val ScodecStream = "3.0.0"
  }

  val Enumeratum   = "com.beachape" %% "enumeratum"    % Versions.Enumeratum
  val FS2Core      = "co.fs2"       %% "fs2-core"      % Versions.FS2
  val FS2IO        = "co.fs2"       %% "fs2-io"        % Versions.FS2
  val ScodecCore   = "org.scodec"   %% "scodec-core"   % Versions.Scodec
  val ScodecStream = "org.scodec"   %% "scodec-stream" % Versions.ScodecStream

  object Test {
    val MUnitCE3 = "org.typelevel" %% "munit-cats-effect-3" % Versions.MUnitCE3 % "it,test"
  }

}
