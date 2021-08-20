import java.time.Year

import de.heikoseeberger.sbtheader.License

enablePlugins(AutomateHeaderPlugin)
enablePlugins(SbtPlugin)

name := "sbt-sonar"
organization := "com.sonar-scala"
homepage := Some(url("https://github.com/sonar-scala/sbt-sonar"))

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
scmInfo := Some(
  ScmInfo(
    url("https://github.com/sonar-scala/sbt-sonar"),
    "scm:git:https://github.com/sonar-scala/sbt-sonar.git",
    Some("scm:git:git@github.com:sonar-scala/sbt-sonar.git")
  )
)
developers := List(
  Developer(
    "mwz",
    "Michael Wizner",
    "@mwz",
    url("https://github.com/mwz")
  )
)

crossSbtVersions := Seq("0.13.18", "1.5.0")
sbtPlugin := true
scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-deprecation"
)
libraryDependencies ++= List(
  "org.sonarsource.scanner.api" % "sonar-scanner-api" % "2.16.1.361" % Compile,
  "org.scalatest"              %% "scalatest"         % "3.2.9"      % Test,
  "org.scalatestplus"          %% "mockito-1-10"      % "3.1.0.0"    % Test,
  "org.mockito"                 % "mockito-core"      % "3.12.1"      % Test
)
scalafmtOnCompile in ThisBuild :=
  sys.env
    .get("CI")
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

// Sonatype
sonatypeCredentialHost := "s01.oss.sonatype.org"
