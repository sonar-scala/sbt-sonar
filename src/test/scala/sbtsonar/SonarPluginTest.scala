/*
 * Copyright 2016-2020 All sbt-sonar contributors
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

import java.io.File
import java.nio.file.Paths

import scala.collection.JavaConverters._
import scala.util.Properties

import SbtCompat.Logger
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.sonarsource.scanner.api.EmbeddedScanner
import sbt.IO

class SonarPluginTest extends FlatSpec with Matchers with MockitoSugar with WithFile {

  val sonarPropertiesFileContent =
    """# Root project information
      |sonar.projectKey=org.mycompany.myproject
      |sonar.projectName=My Project
      |sonar.projectVersion=1.0
      |
      |# Some properties that will be inherited by the modules
      |sonar.sources=src
      |
      |# List of the module identifiers
      |sonar.modules=module1,module2
      |
      |# Properties can obviously be overriden for
      |# each module - just prefix them with the module ID
      |module1.sonar.projectName=Module 1
      |module2.sonar.projectName=Module 2""".stripMargin

  "sourcesDir" should "resolve correctly the relative path" in {
    SonarPlugin.sourcesDir(new File("."), new File("./a/b")) shouldBe
    Some("sonar.sources" -> Paths.get("a/b").toString)
  }

  "testsDir" should "resolve correctly the relative path" in {
    SonarPlugin.testsDir(new File("."), new File("./a/b")) shouldBe
    Some("sonar.tests" -> Paths.get("a/b").toString)
  }

  "reports" should "resolve correctly the relative path to the report files" in {
    SonarPlugin.reports(new File("."), new File("./a/b")) shouldBe
    Seq(
      "sonar.scala.scoverage.reportPath" -> Paths.get("a/b/scoverage-report/scoverage.xml").toString,
      "sonar.scala.scapegoat.reportPath" -> Paths.get("a/b/scapegoat-report/scapegoat.xml").toString
    )
  }

  "updatePropertiesFile" should
  "update the sonar properties file with the current project version" in withFile { file =>
    val expectedContent =
      """# Root project information
        |sonar.projectKey=org.mycompany.myproject
        |sonar.projectName=My Project
        |sonar.projectVersion=123.456.789
        |
        |# Some properties that will be inherited by the modules
        |sonar.sources=src
        |
        |# List of the module identifiers
        |sonar.modules=module1,module2
        |
        |# Properties can obviously be overriden for
        |# each module - just prefix them with the module ID
        |module1.sonar.projectName=Module 1
        |module2.sonar.projectName=Module 2""".stripMargin

    IO.writeLines(file, sonarPropertiesFileContent.split(Properties.lineSeparator).toSeq)
    SonarPlugin.updatePropertiesFile(file.getParentFile, file.getName, "123.456.789")

    IO.readLines(file) shouldBe expectedContent.split("\\r?\\n").toList
  }

  "sonarScannerArgs" should
  "convert a map with sonar config properties into a sequence of java env properties " in {
    val sonarProperties = Map("a" -> "b", "c.d" -> "e.f")
    val systemProperties = Map("sonar.host.url" -> "http://localhost", "sonar.verbose" -> "true")

    val noExternalConfig = SonarPlugin.sonarScannerArgs(
      sonarUseExternalConfig = false,
      sonarProperties = sonarProperties,
      systemProperties = systemProperties,
      version = "987.654.321"
    )
    noExternalConfig should contain theSameElementsAs Seq(
      "-Da=b",
      "-Dc.d=e.f",
      "-Dsonar.projectVersion=987.654.321",
      "-Dsonar.host.url=http://localhost",
      "-Dsonar.verbose=true"
    )

    val withExternalConfig = SonarPlugin.sonarScannerArgs(
      sonarUseExternalConfig = true,
      sonarProperties = sonarProperties,
      systemProperties = systemProperties,
      version = "987.654.321"
    )
    withExternalConfig should contain theSameElementsAs Seq(
      "-Dsonar.host.url=http://localhost",
      "-Dsonar.verbose=true"
    )
  }

  "propertiesFromFile" should "read sonar properties from a file" in withFile { file =>
    IO.writeLines(file, sonarPropertiesFileContent.split(Properties.lineSeparator).toSeq)

    val expected = Map(
      "sonar.projectKey" -> "org.mycompany.myproject",
      "sonar.projectName" -> "My Project",
      "sonar.projectVersion" -> "1.0",
      "sonar.sources" -> "src",
      "sonar.modules" -> "module1,module2",
      "module1.sonar.projectName" -> "Module 1",
      "module2.sonar.projectName" -> "Module 2"
    )

    SonarPlugin.propertiesFromFile(file) should contain theSameElementsAs expected
  }

  "useEmbeddedScanner" should "start the analysis using the embedded scanner" in {
    implicit val log = Logger.Null
    val embeddedScanner = mock[EmbeddedScanner]
    when(embeddedScanner.addGlobalProperties(any())).thenReturn(embeddedScanner)

    SonarPlugin.useEmbeddedScanner(
      useExternalConfig = false,
      propertiesFile = new File("."),
      version = "1.2.3",
      sonarProperties = Map("sonar.property1" -> "value1"),
      systemProperties = Map("sonar.property2" -> "value2"),
      embeddedScanner
    )

    val properties = Map(
      "sonar.property1" -> "value1",
      "sonar.property2" -> "value2",
      "sonar.projectVersion" -> "1.2.3"
    )
    verify(embeddedScanner).addGlobalProperties(properties.asJava)
    verify(embeddedScanner).start
    verify(embeddedScanner).execute(properties.asJava)
  }

  "useEmbeddedScanner" should "respect properties from an external file" in withFile { file =>
    IO.writeLines(file, sonarPropertiesFileContent.split(Properties.lineSeparator).toSeq)

    implicit val log = Logger.Null
    val embeddedScanner = mock[EmbeddedScanner]
    when(embeddedScanner.addGlobalProperties(any())).thenReturn(embeddedScanner)

    SonarPlugin.useEmbeddedScanner(
      useExternalConfig = true,
      propertiesFile = file,
      version = "1.2.3",
      sonarProperties = Map("sonar.property1" -> "value1"),
      systemProperties = Map("sonar.property2" -> "value2"),
      embeddedScanner
    )

    val properties = Map(
      "sonar.projectKey" -> "org.mycompany.myproject",
      "sonar.projectName" -> "My Project",
      "sonar.sources" -> "src",
      "sonar.modules" -> "module1,module2",
      "module1.sonar.projectName" -> "Module 1",
      "module2.sonar.projectName" -> "Module 2",
      "sonar.projectVersion" -> "1.2.3",
      "sonar.property2" -> "value2"
    )
    verify(embeddedScanner).addGlobalProperties(properties.asJava)
    verify(embeddedScanner).start
    verify(embeddedScanner).execute(properties.asJava)
  }
}
