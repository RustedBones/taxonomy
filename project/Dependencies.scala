import sbt.*

object Dependencies:

  object Versions:
    val FS2      = "3.4.0"
    val MUnitCE3 = "1.0.7"
    val Scodec   = "2.2.1"

  val FS2Core    = "co.fs2"     %% "fs2-core"    % Versions.FS2
  val FS2IO      = "co.fs2"     %% "fs2-io"      % Versions.FS2
  val FS2Scodec  = "co.fs2"     %% "fs2-scodec"  % Versions.FS2
  val ScodecCore = "org.scodec" %% "scodec-core" % Versions.Scodec

  object Test:
    val MUnitCE3 = "org.typelevel" %% "munit-cats-effect-3" % Versions.MUnitCE3 % "it,test"
