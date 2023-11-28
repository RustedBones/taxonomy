import org.scalafmt.sbt.ScalafmtPlugin

// General info
val username  = "RustedBones"
val repo      = "taxonomy"
val githubUrl = s"https://github.com/$username/$repo"

ThisBuild / tlBaseVersion       := "1.2"
ThisBuild / tlVersionIntroduced := Map("3" -> "1.1.0")
ThisBuild / organization        := "fr.davit"
ThisBuild / organizationName    := "Michel Davit"
ThisBuild / startYear           := Some(2020)
ThisBuild / licenses            := Seq(License.Apache2)
ThisBuild / homepage            := Some(url(githubUrl))
ThisBuild / scmInfo             := Some(ScmInfo(url(githubUrl), s"git@github.com:$username/$repo.git"))
ThisBuild / developers          := List(
  Developer(
    id = s"$username",
    name = "Michel Davit",
    email = "michel@davit.fr",
    url = url(s"https://github.com/$username")
  )
)

// scala versions
val scala3       = "3.3.1"
val defaultScala = scala3

// github actions
val java17      = JavaSpec.temurin("17")
val java11      = JavaSpec.temurin("11")
val defaultJava = java17

ThisBuild / scalaVersion                 := defaultScala
ThisBuild / githubWorkflowTargetBranches := Seq("main")
ThisBuild / githubWorkflowJavaVersions   := Seq(java17, java11)

// build
ThisBuild / tlFatalWarnings         := true
ThisBuild / tlJdkRelease            := Some(8)
ThisBuild / tlSonatypeUseLegacyHost := true

// mima
ThisBuild / mimaBinaryIssueFilters ++= Seq()

lazy val commonSettings = Defaults.itSettings ++
  headerSettings(Configurations.IntegrationTest) ++
  inConfig(IntegrationTest)(ScalafmtPlugin.scalafmtConfigSettings) ++ Seq(
    testFrameworks += new TestFramework("munit.Framework")
  )

lazy val `taxonomy` = project
  .in(file("."))
  .settings(commonSettings)
  .aggregate(
    `taxonomy-model`,
    `taxonomy-scodec`,
    `taxonomy-fs2`
  )
  .settings(
    publish / skip        := true,
    mimaPreviousArtifacts := Set.empty
  )

lazy val `taxonomy-model` = project
  .in(file("model"))
  .settings(commonSettings)

lazy val `taxonomy-scodec` = project
  .in(file("scodec"))
  .configs(IntegrationTest)
  .dependsOn(`taxonomy-model`)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.ScodecCore,
      Dependencies.Test.MUnitCE3
    )
  )

lazy val `taxonomy-fs2` = project
  .in(file("fs2"))
  .configs(IntegrationTest)
  .dependsOn(`taxonomy-scodec`)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.FS2Core,
      Dependencies.FS2IO,
      Dependencies.FS2Scodec,
      Dependencies.Test.MUnitCE3
    )
  )
