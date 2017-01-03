# sbt-sonar
An sbt plugin which can be used to run `sonar-scanner` launcher to analyse a Scala project with [SonarQube](www.sonarqube.org) - a tool for continuous code inspection and quality management :white_check_mark:.
 This plugin is particularly useful if you use [sbt-release](https://www.github.com/sbt/sbt-release) for automated release process in your project.

## Requirements
 - sbt 0.13.5+
 - SonarQube server - see my [sonarqube-scala-docker](https://github.com/mwz/sonarqube-scala-docker) repository, which provides a docker-compose recipe for out-of-the-box SonarQube instance with support for Scala, Scoverage (code coverage metrics) and Scalastyle + Scapegoat (static code analysis). Alternatively, see the instructions for [manual installation](http://docs.sonarqube.org/display/SONAR/Get+Started+in+Two+Minutes).
 - [sonar-scanner](http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) executable installed on your CI server or locally if you intend to run the analysis on your machine.

## Installation
To install this plugin in your project, add the following to your `./project/plugins.sbt` file:

```scala
addSbtPlugin("com.github.mwz" % "sbt-sonar" % "0.3.1")
```

## Usage
Before using the plugin, make sure you have defined the `SONAR_SCANNER_HOME` environmental variable and updated the global settings in `$SONAR_SCANNER_HOME/conf/sonar-scanner.properties` to point to your SonarQube instance. 

You can define your project properties either in the external config file `sonar-project.properties`, which should be located in the root directory of your project as explained in [SonarQube Scanner guide](http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) or directly in sbt. By default, the plugin expects the properties to be defined in the `sonarProperties` setting key in sbt, which comes with the following set of predefined properties:

 - *sonar.projectName* - your project name defined in the `name` sbt setting key.
 - *sonar.projectKey* - your project name transformed into a lowercase and dash-separated value.
 - *sonar.sourceEncoding* - UTF-8.
 - *sonar.sources* - default Scala source directory relative to the root of your project (usually src/main/scala).

If you wish to add more properties to the existing config e.g. to configure your Sonar plugins or set up multi-module project, use the `++=` operator, e.g.:
 
```scala
import sbtsonar.SonarPlugin.autoImport.sonarProperties
 
sonarProperties ++= Map(
  "sonar.scoverage.reportPath" -> "target/scala-2.11/scoverage-report/scoverage.xml",
  "sonar.scala.scapegoat.reportPath" -> "target/scala-2.11/scapegoat-report/scapegoat.xml"
)
```

To overwrite the entire config provided by default, use the `:=` operator, e.g.:

```scala
import sbtsonar.SonarPlugin.autoImport.sonarProperties
 
sonarProperties := Map(
  "sonar.projectName" -> "Project Name",
  "sonar.projectKey" -> "project-name",
  "sonar.sources" -> "src/main/scala",
  "sonar.sourceEncoding" -> "UTF-8",
  "sonar.scoverage.reportPath" -> "target/scala-2.11/scoverage-report/scoverage.xml",
  "sonar.scala.scapegoat.reportPath" -> "target/scala-2.11/scapegoat-report/scapegoat.xml"
)
```

To use the `sonar-project.properties` file instead, you can set the `sonarUseExternalConfig` to `true`, e.g.:
```scala
import sbtsonar.SonarPlugin.autoImport.sonarUseExternalConfig
 
sonarUseExternalConfig := true
```

To run the plugin, execute the `sonarScan` sbt task in your project. Depending on the configuration option you have chosen, the plugin will update the `sonar.projectVersion` property to your current project version either in `sonar-project.properties` file or in the `sonarProperties` in sbt config and it will run the `sonar-scanner` executable printing the progress to sbt console.

This plugin can be also easily used with the `sbt-release` by wrapping the `sonarScan` task in a `releaseStepTask` in the following way:

```scala
import sbtsonar.SonarPlugin.autoImport.sonarScan
 
releaseProcess := Seq[ReleaseStep](
  ...
  releaseStepTask(sonarScan),
  ...
)
```

## Changelog
 * **0.3.1** - Updated the scope of `scalaSource` setting key to resolve scoping ambiguity with some other plugins.
 * **0.3.0** - Defined a set of default project settings in the `sonarProperties` config key.
 * **0.2.0** - Added the ability to define sonar project properties directly in sbt.
 * **0.1.0** - First release of the plugin :tada:!

## License
The project is licensed under the Apache License v2. See the [LICENSE file](LICENSE) for more details.
