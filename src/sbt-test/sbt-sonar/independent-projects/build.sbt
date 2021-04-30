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
    "sonar.projectName" -> "Independent projects"
  )
)

lazy val module1 = (project in file("test-1/module1"))
  .settings(baseSettings)
  .settings(name := "module2indep")

lazy val module2 = (project in file("test-2/module2"))
  .settings(baseSettings)
  .settings(name := "module1indep")

lazy val independantProjects = (project in file("."))
  .aggregate(module1, module2)
  .settings(name := "independent-projects")
  .settings(baseSettings)
  .settings(sonarSettings)
