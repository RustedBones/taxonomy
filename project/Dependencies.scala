import sbt._

object Dependencies {

  object Versions {
    val FS2          = "2.2.1"
    val ScalaTest    = "3.1.1"
    val Scodec       = "1.11.7"
    val ScodecStream = "2.0.0"
  }

  val FS2Core      = "co.fs2"     %% "fs2-core"      % Versions.FS2
  val FS2IO        = "co.fs2"     %% "fs2-io"        % Versions.FS2
  val ScodecCore   = "org.scodec" %% "scodec-core"   % Versions.Scodec
  val ScodecStream = "org.scodec" %% "scodec-stream" % Versions.ScodecStream

  object Test {
    val ScalaTest = "org.scalatest" %% "scalatest" % Versions.ScalaTest % "it,test"
  }

}
