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

import java.io.File
import java.nio.file.Paths

import org.scalatest._
import sbt.IO

import scala.util.Properties

class SonarPluginTest extends FlatSpec with Matchers with WithFile {

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
    val content =
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

    IO.writeLines(file, content.split(Properties.lineSeparator).toSeq)
    SonarPlugin.updatePropertiesFile(file.getParentFile, file.getName, "123.456.789")

    IO.readLines(file) shouldBe expectedContent.split("\\r?\\n").toList
  }

  "sonarScannerArgs" should
  "convert a map with sonar config properties into a sequence of java env properties " in {
    val sonarProperties = Map("a" -> "b", "c.d" -> "e.f")
    val result = SonarPlugin.sonarScannerArgs(
      sonarUseExternalConfig = false,
      sonarProperties = sonarProperties,
      version = "987.654.321"
    )
    val expected = Seq("-Da=b", "-Dc.d=e.f", "-Dsonar.projectVersion=987.654.321")
    result shouldBe expected
  }
}
