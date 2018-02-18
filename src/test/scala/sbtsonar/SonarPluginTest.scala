package sbtsonar

import java.io.File

import org.scalatest._
import sbt.IO

import scala.util.Properties

class SonarPluginTest extends FlatSpec with Matchers with WithFile {

  "sourcesDir" should "resolve correctly the relative path" in {
    SonarPlugin.sourcesDir(new File("."), new File("./a/b")) shouldBe
    Some("sonar.sources" -> "a/b")
  }

  "reports" should "resolve correctly the relative path to the report files" in {
    SonarPlugin.reports(new File("."), new File("./a/b")) shouldBe
    Seq(
      "sonar.scoverage.reportPath" -> "a/b/scoverage-report/scoverage.xml",
      "sonar.scala.scapegoat.reportPath" -> "a/b/scapegoat-report/scapegoat.xml"
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

    IO.readLines(file) shouldBe expectedContent.split(Properties.lineSeparator).toList
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
