import scala.annotation.nowarn

// General info
val username = "RustedBones"
val repo     = "taxonomy"

// for sbt-github-actions
ThisBuild / scalaVersion := "3.2.1"
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(name = Some("Check project"), commands = List("scalafmtCheckAll", "headerCheckAll")),
  WorkflowStep.Sbt(name = Some("Build project"), commands = List("test", "IntegrationTest/test"))
)
ThisBuild / githubWorkflowTargetBranches := Seq("main")
ThisBuild / githubWorkflowPublishTargetBranches := Seq.empty

lazy val commonSettings = Defaults.itSettings ++
  headerSettings(Configurations.IntegrationTest) ++
  Seq(
    organization := "fr.davit",
    organizationName := "Michel Davit",
    scalaVersion := (ThisBuild / scalaVersion).value,
    homepage := Some(url(s"https://github.com/$username/$repo")),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    startYear := Some(2020),
    scmInfo := Some(ScmInfo(url(s"https://github.com/$username/$repo"), s"git@github.com:$username/$repo.git")),
    developers := List(
      Developer(
        id = s"$username",
        name = "Michel Davit",
        email = "michel@davit.fr",
        url = url(s"https://github.com/$username")
      )
    ),
    publishMavenStyle := true,
    Test / publishArtifact := false,
    publishTo := {
      val resolver = if (isSnapshot.value) {
        Opts.resolver.sonatypeSnapshots: @nowarn("cat=deprecation")
      } else {
        Opts.resolver.sonatypeStaging
      }
      Some(resolver)
    },
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    credentials ++= (for {
      username <- sys.env.get("SONATYPE_USERNAME")
      password <- sys.env.get("SONATYPE_PASSWORD")
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq,
    testFrameworks += new TestFramework("munit.Framework")
  )

lazy val `taxonomy` = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(`taxonomy-model`, `taxonomy-scodec`, `taxonomy-fs2`)
  .settings(
    publish / skip := true
  )

lazy val `taxonomy-model` = (project in file("model"))
  .settings(commonSettings: _*)

lazy val `taxonomy-scodec` = (project in file("scodec"))
  .configs(IntegrationTest)
  .dependsOn(`taxonomy-model`)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.ScodecCore,
      Dependencies.Test.MUnitCE3
    )
  )

lazy val `taxonomy-fs2` = (project in file("fs2"))
  .configs(IntegrationTest)
  .dependsOn(`taxonomy-scodec`)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.FS2Core,
      Dependencies.FS2IO,
      Dependencies.FS2Scodec,
      Dependencies.Test.MUnitCE3
    )
  )
