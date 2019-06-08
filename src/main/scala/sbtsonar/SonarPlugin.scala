/*
 * Copyright 2016-2019 All sbt-sonar contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sbtsonar

import java.nio.file.Paths
import java.util.{Properties => JavaProperties}

import org.sonarsource.scanner.api.{EmbeddedScanner, LogOutput}
import sbt.Keys._
import sbt._

import scala.collection.JavaConverters._
import scala.sys.process.Process
import scala.util.Properties

object SonarPlugin extends AutoPlugin {
  private val SonarProjectVersionKey = "sonar.projectVersion"
  private val SonarExternalConfigFileName = "sonar-project.properties"
  private val ScoverageReport = "scoverage-report/scoverage.xml"
  private val ScapegoatReport = "scapegoat-report/scapegoat.xml"

  object autoImport {
    val sonarUseExternalConfig: SettingKey[Boolean] = settingKey(
      "Whether to use an external sonar-project.properties file instead of the properties defined in the sonarProperties Map."
    )
    val sonarUseSonarScannerCli: SettingKey[Boolean] = settingKey(
      "Whether to use a standalone sonar-scanner CLI instead of the embedded sonar-scanner API."
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
    sonarUseSonarScannerCli := false,
    sonarProperties := (
        Seq(
          "sonar.projectName" -> name.value,
          "sonar.projectKey" -> normalizedName.value,
          "sonar.sourceEncoding" -> "UTF-8",
          "sonar.scala.version" -> scalaVersion.value
        ) ++
        // Base sources directory.
        sourcesDir(baseDirectory.value, (scalaSource in Compile).value) ++

        // Base tests directory.
        testsDir(baseDirectory.value, (scalaSource in Test).value) ++

        // Scoverage & Scapegoat report directories.
        reports(baseDirectory.value, (crossTarget in Compile).value)
      ).toMap,
    sonarScan := {
      implicit val log: Logger = streams.value.log

      // Allow to set sonar properties via system properties
      // [https://docs.sonarqube.org/display/SONAR/Analysis+Parameters]
      val systemProperties: Map[String, String] =
        sys.props.filterKeys(_.startsWith("sonar.")).toMap

      if (sonarUseSonarScannerCli.value)
        useStandaloneScanner(
          sonarUseExternalConfig.value,
          baseDirectory.value,
          version.value,
          sonarProperties.value,
          systemProperties
        )
      else {
        val logOutput: LogOutput = SonarSbtLogOutput(systemProperties)
        val embeddedScanner: EmbeddedScanner =
          EmbeddedScanner
            .create(
              "sbt-sonar",
              getClass.getPackage.getImplementationVersion,
              logOutput
            )

        useEmbeddedScanner(
          sonarUseExternalConfig.value,
          new File(baseDirectory.value, SonarExternalConfigFileName),
          version.value,
          sonarProperties.value,
          systemProperties,
          embeddedScanner
        )
      }
    }
  )

  private[sbtsonar] def sourcesDir(baseDir: File, scalaSources: File): Option[(String, String)] =
    IO.relativizeFile(baseDir, scalaSources).map("sonar.sources" -> _.toString)

  private[sbtsonar] def testsDir(baseDir: File, scalaTests: File): Option[(String, String)] =
    IO.relativizeFile(baseDir, scalaTests).map("sonar.tests" -> _.toString)

  private[sbtsonar] def reports(baseDir: File, crossTarget: File): Seq[(String, String)] =
    IO.relativizeFile(baseDir, crossTarget)
      .map { dir =>
        Seq(
          "sonar.scala.scoverage.reportPath" -> new File(dir, ScoverageReport).toString,
          "sonar.scala.scapegoat.reportPath" -> new File(dir, ScapegoatReport).toString
        )
      }
      .toSeq
      .flatten

  private[sbtsonar] def updatePropertiesFile(baseDir: File, fileName: String, version: String): Unit = {
    val sonarPropsFile = new File(baseDir, fileName)
    val sonarProps: List[String] = IO.readLines(sonarPropsFile)

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
    val updatedSonarProps: List[String] = loop(sonarProps)
    IO.writeLines(sonarPropsFile, updatedSonarProps)
  }

  private[sbtsonar] def sonarScannerArgs(
    sonarUseExternalConfig: Boolean,
    sonarProperties: Map[String, String],
    systemProperties: Map[String, String],
    version: String
  ): List[String] = {
    val properties: Map[String, String] =
      if (sonarUseExternalConfig) systemProperties
      else (sonarProperties + (SonarProjectVersionKey -> version)) ++ systemProperties

    properties.map {
      case (key, value) => s"-D$key=$value"
    }.toList
  }

  private[sbtsonar] def useStandaloneScanner(
    sonarUseExternalConfig: Boolean,
    baseDirectory: File,
    version: String,
    sonarProperties: Map[String, String],
    systemProperties: Map[String, String]
  )(implicit log: Logger): Unit = {
    val sonarHome: String =
      sys.env
        .get("SONAR_SCANNER_HOME")
        .orElse(sys.props.get("sonarScanner.home"))
        .getOrElse(
          sys.error(
            "SONAR_SCANNER_HOME environment variable or sonarScanner.home system property not defined."
          )
        )

    // Update the external properties file if the sonarUseExternalConfig is set to true.
    if (sonarUseExternalConfig)
      updatePropertiesFile(baseDirectory, SonarExternalConfigFileName, version)

    val args: Seq[String] =
      sonarScannerArgs(sonarUseExternalConfig, sonarProperties, systemProperties, version)

    val executablePath: String =
      if (Properties.isWin) "bin/sonar-scanner.bat"
      else "bin/sonar-scanner"

    val sonarScanner: String =
      Paths
        .get(sonarHome)
        .resolve(executablePath)
        .toAbsolutePath
        .toString

    // Run sonar-scanner executable.
    SbtCompat
      .process(Process(sonarScanner, args))
      .foreach(log.info(_))
  }

  private[sbtsonar] def propertiesFromFile(file: File): Map[String, String] = {
    SbtCompat.Using.fileInputStream(file) { stream =>
      val props = new JavaProperties
      props.load(stream)
      props.asScala.toMap
    }
  }

  private[sbtsonar] def useEmbeddedScanner(
    useExternalConfig: Boolean,
    propertiesFile: File,
    version: String,
    sonarProperties: Map[String, String],
    systemProperties: Map[String, String],
    embeddedScanner: EmbeddedScanner
  )(implicit log: Logger): Unit = {
    val properties: Map[String, String] =
      if (useExternalConfig)
        // Overwrite project version.
        (propertiesFromFile(propertiesFile) + (SonarProjectVersionKey -> version)) ++ systemProperties
      else sonarProperties ++ systemProperties

    val configuredScanner =
      embeddedScanner.addGlobalProperties(properties.asJava)

    configuredScanner.start()
    log.info("SonarQube server version: " + configuredScanner.serverVersion)
    configuredScanner.execute(properties.asJava)
  }
}
