import sbt._

object Dependencies {

  object Versions {
    val ScalaTest = "3.1.1"
    val Scodec    = "1.11.7"
  }

  val Scodec = "org.scodec" %% "scodec-core" % Versions.Scodec

  object Test {
    val ScalaTest = "org.scalatest" %% "scalatest" % Versions.ScalaTest % "it,test"
  }

}
