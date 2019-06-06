import ReleaseTransformations._
enablePlugins(SbtPlugin)

name := "sbt-sonar"
organization := "com.github.mwz"
homepage := Some(url("https://github.com/mwz/sbt-sonar"))
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

crossSbtVersions := Seq("0.13.18", "1.2.8")
releaseCrossBuild := true
sbtPlugin := true
publishMavenStyle := false
scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-deprecation"
)
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.7" % "test"
scalafmtOnCompile in ThisBuild := true

// Scripted
scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
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
