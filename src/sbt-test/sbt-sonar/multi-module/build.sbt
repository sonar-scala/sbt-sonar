import sbtsonar.SonarPlugin.autoImport.sonarProperties
import sbtsonar.SonarPlugin.autoImport.sonarScan

lazy val baseSettings = Seq(
  version := "0.1",
  scalaVersion := "2.12.4",
  scapegoatVersion in ThisBuild := "1.3.3",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)
lazy val sonarSettings = Seq(
  sonarProperties ++= Map(
    "sonar.projectName" -> "Multi Module",
    "sonar.modules" -> "module1,module2",
    "module1.sonar.projectName" -> "Module 1",
    "module2.sonar.projectName" -> "Module 2"
  )
)

lazy val module1 = (project in file("module1"))
  .settings(baseSettings)
  .settings(name := "module2")

lazy val module2 = (project in file("module2"))
  .settings(baseSettings)
  .settings(name := "module1")

lazy val multiModule = (project in file("."))
  .aggregate(module1, module2)
  .settings(name := "multi-module")
  .settings(baseSettings)
  .settings(sonarSettings)
  .settings(aggregate in sonarScan := false)
