package sbtsonar

import java.nio.file.Paths

import sbt.Keys._
import sbt.{AutoPlugin, Compile, File, IO, Logger, PluginTrigger, SettingKey, TaskKey, settingKey, taskKey}

import scala.sys.process.Process

object SonarPlugin extends AutoPlugin {

  private val SonarProjectVersionKey = "sonar.projectVersion"
  private val SonarExternalConfigFileName = "sonar-project.properties"
  private val ScoverageReport = "scoverage-report/scoverage.xml"
  private val ScapegoatReport = "scapegoat-report/scapegoat.xml"

  object autoImport {
    val sonarUseExternalConfig: SettingKey[Boolean] = settingKey("Whether to use an external sonar-project.properties file instead of the properties defined in the sonarProperties Map.")
    val sonarProperties: SettingKey[Map[String, String]] = settingKey("SonarScanner configuration properties.")
    val sonarScan: TaskKey[Unit] = taskKey("Update project version in sonar-project.properties and run sonar scanner process.")
  }

  import autoImport._

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings = Seq(
    sonarUseExternalConfig := false,
    sonarProperties := (
      Seq(
        "sonar.projectName" -> name.value,
        "sonar.projectKey" -> normalizedName.value,
        "sonar.sourceEncoding" -> "UTF-8"
      ) ++
        // Base sources directory.
        sourcesDir(baseDirectory.value, (scalaSource in Compile).value) ++

        // Scoverage & Scapegoat report directories.
        reports(baseDirectory.value, (crossTarget in Compile).value)
      ).toMap,
    sonarScan := {
      implicit val logger: Logger = streams.value.log

      val maybeSonarHome = Option(System.getenv("SONAR_SCANNER_HOME"))
        .orElse(Option(System.getProperty("sonarScanner.home")))

      if (maybeSonarHome.isEmpty)
        sys.error("SONAR_SCANNER_HOME environmental variable or sonarScanner.home system property not defined.")

      // Update the external properties file if the sonarUseExternalConfig is set to true.
      if (sonarUseExternalConfig.value)
        updatePropertiesFile(baseDirectory.value, SonarExternalConfigFileName, version.value)

      val args = sonarScannerArgs(sonarUseExternalConfig.value, sonarProperties.value, version.value)

      val sonarScannerBinDirectory = maybeSonarHome.map(sonarScannerPath => Paths.get(sonarScannerPath).resolve("bin").toFile)

      // Run sonar-scanner executable.
      Process("sonar-scanner" +: args, sonarScannerBinDirectory).lines.foreach(logInfo)
    }
  )

  private[sbtsonar] def sourcesDir(baseDir: File, scalaSource: File): Option[(String, String)] =
    IO.relativizeFile(baseDir, scalaSource).map("sonar.sources" -> _.toString)

  private[sbtsonar] def reports(baseDir: File, crossTarget: File): Seq[(String, String)] =
    IO.relativizeFile(baseDir, crossTarget).map { dir =>
      Seq(
        "sonar.scoverage.reportPath" -> new File(dir, ScoverageReport).toString,
        "sonar.scala.scapegoat.reportPath" -> new File(dir, ScapegoatReport).toString
      )
    }.toSeq.flatten

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

  private[sbtsonar] def sonarScannerArgs(sonarUseExternalConfig: Boolean,
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

  private[sbtsonar] def logInfo(msg: String)(implicit logger: Logger): Unit = logger.info(msg)
}
