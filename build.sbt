import java.time.Year

import de.heikoseeberger.sbtheader.License
import ReleaseTransformations._

enablePlugins(AutomateHeaderPlugin)
enablePlugins(SbtPlugin)

name := "sbt-sonar"
organization := "com.github.mwz"
homepage := Some(url("https://github.com/mwz/sbt-sonar"))

// Licence
organizationName := "All sbt-sonar contributors"
startYear := Some(2016)
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
headerLicense := Some(
  License.ALv2(
    s"${startYear.value.get}-${Year.now}",
    organizationName.value
  )
)
excludeFilter.in(headerResources) := "*.scala"

crossSbtVersions := Seq("0.13.18", "1.3.2")
releaseCrossBuild := true
sbtPlugin := true
publishMavenStyle := false
scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-deprecation"
)
libraryDependencies ++= List(
  "org.sonarsource.scanner.api" % "sonar-scanner-api" % "2.15.0.2182" % Compile,
  "org.scalatest"               %% "scalatest"        % "3.2.2"       % Test,
  "org.scalatestplus"           %% "mockito-1-10"     % "3.1.0.0"     % Test,
  "org.mockito"                 % "mockito-core"      % "3.5.10"       % Test
)
scalafmtOnCompile in ThisBuild :=
  sys.env
    .get("DISABLE_SCALAFMT")
    .forall(_.toLowerCase == "false")
cancelable in Global := true

// Scripted
scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++ Seq(
    "-Xmx1024M",
    "-Dplugin.version=" + version.value,
    "-Dsonar.host.url=http://localhost"
  )
}
scriptedBufferLog := false

// Bintray
bintrayRepository := "sbt-plugin-releases"
bintrayPackage := "sbt-sonar"
bintrayReleaseOnPublish := false

// Release
releaseVersionBump := sbtrelease.Version.Bump.Minor
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("^ publish"),
  releaseStepTask(bintrayRelease in thisProjectRef.value),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
