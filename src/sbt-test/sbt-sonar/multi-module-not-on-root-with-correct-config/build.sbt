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
    "sonar.projectName" -> "Multi Module not in root with correct config",
    "sonar.modules" -> "module1nirwcc,module2nirwcc",
    "module1nirwcc.sonar.projectName" -> "Module 1 not in root with correct config",
    "module2nirwcc.sonar.projectName" -> "Module 2 not in root with correct config",
    "module2nirwcc.sonar.projectBaseDir" -> "test-2/module2",
    "module1nirwcc.sonar.projectBaseDir" -> "test-1/module1"
  )
)

lazy val module1 = (project in file("test-1/module1"))
  .settings(baseSettings)
  .settings(name := "module1nirwcc")

lazy val module2 = (project in file("test-2/module2"))
  .settings(baseSettings)
  .settings(name := "module2nirwcc")

lazy val multiModule = (project in file("."))
  .aggregate(module1, module2)
  .settings(name := "multi-module-not-in-root-with-correct-config")
  .settings(baseSettings)
  .settings(sonarSettings)
  .settings(aggregate in sonarScan := false)
