package sbtsonar

import sbt.Keys.{baseDirectory, streams, version}
import sbt.Path.richFile
import sbt.{AutoPlugin, IO, PluginTrigger, Process, TaskKey, taskKey}

object SonarPlugin extends AutoPlugin {

  object autoImport {
    val sonarScan: TaskKey[Unit] = taskKey("Update project version in sonar-project.properties and run sonar scanner process.")
  }

  import autoImport._

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings = Seq(
    sonarScan := {
      if (Option(System.getenv("SONAR_SCANNER_HOME")).isEmpty)
        sys.error("SONAR_SCANNER_HOME environmental variable is not defined.")

      val projectVersionKey = "sonar.projectVersion"
      val sonarPropsFile = baseDirectory.value / "sonar-project.properties"
      val sonarProps = IO.readLines(sonarPropsFile)

      def logInfo(msg: String) = streams.value.log.info(msg)

      def loop(lines: List[String]): List[String] = {
        lines match {
          case h :: tail if h.contains(projectVersionKey) =>
            s"$projectVersionKey=${version.value}" :: tail
          case h :: tail =>
            h :: loop(tail)
          case _ =>
            Nil
        }
      }

      // Update sonar project version.
      val updatedSonarProps = loop(sonarProps)
      IO.writeLines(sonarPropsFile, updatedSonarProps)

      // Run sonar-scanner executable.
      Process("sonar-scanner").lines.foreach(logInfo)
    }
  )
}
