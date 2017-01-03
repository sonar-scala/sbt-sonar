package sbtsonar

import sbt.Keys._
import sbt.Path.richFile
import sbt.{AutoPlugin, Compile, File, IO, Logger, PluginTrigger, Process, SettingKey, TaskKey, settingKey, taskKey}

object SonarPlugin extends AutoPlugin {

  private val SonarProjectVersionKey = "sonar.projectVersion"
  private val SonarExternalConfigFileName = "sonar-project.properties"

  object autoImport {
    val sonarUseExternalConfig: SettingKey[Boolean] = settingKey("Whether to use an external sonar-project.properties file instead of the properties defined in the sonarProperties Map.")
    val sonarProperties: SettingKey[Map[String, String]] = settingKey("SonarScanner configuration properties.")
    val sonarScan: TaskKey[Unit] = taskKey("Update project version in sonar-project.properties and run sonar scanner process.")
  }

  import autoImport._

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings = Seq(
    sonarUseExternalConfig := false,
    sonarProperties := (Seq(
      "sonar.projectName" -> name.value,
      "sonar.projectKey" -> normalizedName.value,
      "sonar.sourceEncoding" -> "UTF-8"
    ) ++ (scalaSource in Compile).value.relativeTo(baseDirectory.value).map {
      dir => "sonar.sources" -> dir.toString
    }).toMap,
    sonarScan := {
      implicit val logger = streams.value.log

      if (Option(System.getenv("SONAR_SCANNER_HOME")).isEmpty)
        sys.error("SONAR_SCANNER_HOME environmental variable is not defined.")

      // Update the external properties file if the sonarUseExternalConfig is set to true.
      if (sonarUseExternalConfig.value)
        updatePropertiesFile(baseDirectory.value, version.value)

      val args = sonarScannerArgs(sonarUseExternalConfig.value, sonarProperties.value, version.value)

      // Run sonar-scanner executable.
      Process("sonar-scanner", args).lines.foreach(logInfo)
    }
  )

  private def updatePropertiesFile(baseDir: File, version: String): Unit = {
    val sonarPropsFile = baseDir / SonarExternalConfigFileName
    val sonarProps = IO.readLines(sonarPropsFile)

    def loop(lines: List[String]): List[String] = {
      lines match {
        case h :: tail if h.contains(SonarProjectVersionKey) =>
          s"$SonarProjectVersionKey=$version" :: tail
        case h :: tail =>
          h :: loop(tail)
        case _ =>
          Nil
      }
    }

    // Update sonar project version.
    val updatedSonarProps = loop(sonarProps)
    IO.writeLines(sonarPropsFile, updatedSonarProps)
  }

  private def sonarScannerArgs(sonarUseExternalConfig: Boolean,
                               sonarProperties: Map[String, String],
                               version: String): Seq[String] = {
    if (sonarUseExternalConfig) Seq()
    else {
      val withVersion = sonarProperties + (SonarProjectVersionKey -> version)
      withVersion.map {
        case (key, value) => s"-D$key=$value"
      }.toSeq
    }
  }

  private def logInfo(msg: String)(implicit logger: Logger) = logger.info(msg)
}
