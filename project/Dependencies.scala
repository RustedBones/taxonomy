import sbt._

object Dependencies {

  object Versions {
    val FS2          = "3.2.5"
    val MUnitCE3     = "1.0.7"
    val Scodec       = "2.0.0"
    val ScodecStream = "3.0.2"
  }

  val FS2Core      = "co.fs2"       %% "fs2-core"      % Versions.FS2
  val FS2IO        = "co.fs2"       %% "fs2-io"        % Versions.FS2
  val ScodecCore   = "org.scodec"   %% "scodec-core"   % Versions.Scodec
  val ScodecStream = "org.scodec"   %% "scodec-stream" % Versions.ScodecStream

  object Test {
    val MUnitCE3 = "org.typelevel" %% "munit-cats-effect-3" % Versions.MUnitCE3 % "it,test"
  }

}
