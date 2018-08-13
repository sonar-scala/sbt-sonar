package sbtsonar

import java.nio.file.Paths

import sbt.Keys._
import sbt.{settingKey, taskKey, AutoPlugin, Compile, File, IO, Logger, PluginTrigger, SettingKey, TaskKey}

import scala.sys.process.Process

object SonarPlugin extends AutoPlugin {

  private val SonarProjectVersionKey = "sonar.projectVersion"
  private val SonarExternalConfigFileName = "sonar-project.properties"
  private val ScoverageReport = "scoverage-report/scoverage.xml"
  private val ScapegoatReport = "scapegoat-report/scapegoat.xml"

  object autoImport {
    val sonarUseExternalConfig: SettingKey[Boolean] = settingKey(
      "Whether to use an external sonar-project.properties file instead of the properties defined in the sonarProperties Map."
    )
    val sonarProperties: SettingKey[Map[String, String]] = settingKey(
      "SonarScanner configuration properties."
    )
    val sonarScan: TaskKey[Unit] = taskKey(
      "Update project version in sonar-project.properties and run sonar scanner process."
    )
  }

  import autoImport._

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings = Seq(
    sonarUseExternalConfig := false,
    sonarProperties := (
      Seq(
        "sonar.projectName" -> name.value,
        "sonar.projectKey" -> normalizedName.value,
        "sonar.sourceEncoding" -> "UTF-8",
        "sonar.scala.version" -> scalaVersion.value
      ) ++
      // Base sources directory.
      sourcesDir(baseDirectory.value, (scalaSource in Compile).value) ++

      // Scoverage & Scapegoat report directories.
      reports(baseDirectory.value, (crossTarget in Compile).value)
    ).toMap,
    sonarScan := {
      implicit val logger: Logger = streams.value.log

      val sonarHome = sys.env
        .get("SONAR_SCANNER_HOME")
        .orElse(sys.props.get("sonarScanner.home"))
        .getOrElse(
          sys.error(
            "SONAR_SCANNER_HOME environment variable or sonarScanner.home system property not defined."
          )
        )

      // Update the external properties file if the sonarUseExternalConfig is set to true.
      if (sonarUseExternalConfig.value)
        updatePropertiesFile(baseDirectory.value, SonarExternalConfigFileName, version.value)

      //Allow to set sonar properties via system properties: [https://docs.sonarqube.org/display/SONAR/Analysis+Parameters]
      val mergedSonarProperties = sonarProperties.value ++ sys.props.filterKeys(_.startsWith("sonar."))

      val args = sonarScannerArgs(sonarUseExternalConfig.value, mergedSonarProperties, version.value)

      val sonarScanner = Paths.get(sonarHome).resolve("bin/sonar-scanner").toAbsolutePath.toString

      // Run sonar-scanner executable.
      Process(sonarScanner, args).lines.foreach(logInfo)
    }
  )

  private[sbtsonar] def sourcesDir(baseDir: File, scalaSource: File): Option[(String, String)] =
    IO.relativizeFile(baseDir, scalaSource).map("sonar.sources" -> _.toString)

  private[sbtsonar] def reports(baseDir: File, crossTarget: File): Seq[(String, String)] =
    IO.relativizeFile(baseDir, crossTarget)
      .map { dir =>
        Seq(
          "sonar.scoverage.reportPath" -> new File(dir, ScoverageReport).toString,
          "sonar.scala.scapegoat.reportPath" -> new File(dir, ScapegoatReport).toString
        )
      }
      .toSeq
      .flatten

  private[sbtsonar] def updatePropertiesFile(baseDir: File, fileName: String, version: String): Unit = {
    val sonarPropsFile = new File(baseDir, fileName)
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

  private[sbtsonar] def sonarScannerArgs(
    sonarUseExternalConfig: Boolean,
    sonarProperties: Map[String, String],
    version: String
  ): Seq[String] = {
    if (sonarUseExternalConfig) Seq()
    else {
      val withVersion = sonarProperties + (SonarProjectVersionKey -> version)
      withVersion.map {
        case (key, value) => s"-D$key=$value"
      }.toSeq
    }
  }

  private[sbtsonar] def logInfo(msg: String)(implicit logger: Logger): Unit = logger.info(msg)
}
