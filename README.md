sbt-sonar
===
[![circleci-badge][]][circleci]
[![bintray-badge][]][bintray]
[![bintray-stats-badge][]][bintray-stats]
[![gitter-badge][]][gitter]

[circleci-badge]: https://img.shields.io/circleci/project/github/mwz/sbt-sonar/master.svg?label=Build
[circleci]: https://circleci.com/gh/mwz/sbt-sonar
[bintray-badge]: https://api.bintray.com/packages/mwz/sbt-plugin-releases/sbt-sonar/images/download.svg
[bintray]: https://bintray.com/mwz/sbt-plugin-releases/sbt-sonar/_latestVersion
[bintray-stats-badge]: https://img.shields.io/badge/dynamic/json.svg?uri=https://bintray.com/statistics/packageStatistics?pkgPath=/mwz/sbt-plugin-releases/sbt-sonar&query=$.totalDownloads&label=Downloads+(last+30+days)&colorB=brightgreen
[bintray-stats]: https://bintray.com/mwz/sbt-plugin-releases/sbt-sonar#statistics
[gitter-badge]: https://img.shields.io/gitter/room/sonar-scala/sbt-sonar.svg?colorB=46BC99&label=Chat
[gitter]: https://gitter.im/sonar-scala/sbt-sonar
[insightio-badge]: https://img.shields.io/badge/Insight.io-Ready-brightgreen.svg


An sbt plugin which can be used to run `sonar-scanner` launcher to analyse a Scala project with [SonarQube](https://www.sonarqube.org/) - a tool for continuous code inspection and quality management :white_check_mark:.
 This plugin is particularly useful if you use [sbt-release](https://www.github.com/sbt/sbt-release) for automated release process in your project.

## Requirements
 - sbt 0.13.5+ or 1.0+
 - Scala 2.11/2.12
 - SonarQube server - see my [sonar-scala-docker](https://github.com/mwz/sonar-scala-docker) repository, which provides a docker-compose recipe and a docker image for out-of-the-box SonarQube instance with support for [Scala](http://www.scala-lang.org), [Scoverage](https://github.com/scoverage/scalac-scoverage-plugin) (code coverage metrics) and [Scalastyle](http://www.scalastyle.org) + [Scapegoat](https://github.com/sksamuel/scapegoat) (static code analysis). Alternatively, see the instructions for [manual installation](http://docs.sonarqube.org/display/SONAR/Get+Started+in+Two+Minutes).
 - [sonar-scanner](http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) executable installed on your CI server or locally if you intend to run the analysis on your machine.

## Installation
To install this plugin in your project, add the following to your `./project/plugins.sbt` file:

```scala
addSbtPlugin("com.github.mwz" % "sbt-sonar" % "1.7.0")
```

## Usage
Before using the plugin, make sure you have defined the `SONAR_SCANNER_HOME` environmental variable, or `sonarScanner.home` system property, and updated the global settings in `$SONAR_SCANNER_HOME/conf/sonar-scanner.properties` to point to your SonarQube instance. 

You can define your project properties either in the external config file `sonar-project.properties`, which should be located in the root directory of your project as explained in [SonarQube Scanner guide](http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) or directly in sbt. By default, the plugin expects the properties to be defined in the `sonarProperties` setting key in sbt, which comes with the following set of predefined properties:

 - **sonar.projectName** - your project name defined in the `name` sbt setting key
 - **sonar.projectKey** - your project name transformed into a lowercase and dash-separated value
 - **sonar.sourceEncoding** - UTF-8
 - **sonar.sources** - default Scala source directory relative to the root of your project (usually `src/main/scala`, uses the value of `scalaSource in Compile`)
 - **sonar.tests** - default Scala tests directory relative to the root of your project (usually `src/test/scala`, uses the value of `scalaSource in Test`)
 - **sonar.scala.version** - defines the version of Scala used in your project (i.e. `scalaVersion`)
 - **sonar.scala.scoverage.reportPath** - relative path to the scoverage report (e.g. `target/scala-2.12/scoverage-report/scoverage.xml`)
 - **sonar.scala.scapegoat.reportPath** - relative path to the scapegoat report (e.g. `target/scala-2.12/scapegoat-report/scapegoat.xml`)

If you wish to add more properties to the existing config e.g. to configure your Sonar plugins or set up multi-module project, use the `++=` operator, e.g.:
 
```scala
import sbtsonar.SonarPlugin.autoImport.sonarProperties
 
sonarProperties ++= Map(
  "sonar.sources" -> "src/main/scala",
  "sonar.tests" -> "src/test/scala",
  "sonar.modules" -> "module1,module2",
  "module1.sonar.projectName" -> "Module 1",
  "module2.sonar.projectName" -> "Module 2"
  ...
)
```

To overwrite the entire config provided by default, use the `:=` operator, e.g.:

```scala
import sbtsonar.SonarPlugin.autoImport.sonarProperties
 
sonarProperties := Map(
  "sonar.projectName" -> "Project Name",
  "sonar.projectKey" -> "project-name",
  "sonar.sources" -> "src/main/scala",
  "sonar.tests" -> "src/test/scala",
  "sonar.junit.reportPaths" -> "target/test-reports",
  "sonar.sourceEncoding" -> "UTF-8",
  "sonar.scala.scoverage.reportPath" -> "target/scala-2.12/scoverage-report/scoverage.xml",
  "sonar.scala.scapegoat.reportPath" -> "target/scala-2.12/scapegoat-report/scapegoat.xml"
  ...
)
```

To use the `sonar-project.properties` file instead, you can set the `sonarUseExternalConfig` to `true`, e.g.:
```scala
import sbtsonar.SonarPlugin.autoImport.sonarUseExternalConfig
 
sonarUseExternalConfig := true
```

To run the plugin, execute the `sonarScan` sbt task in your project. Depending on the configuration option you have chosen, the plugin will update the `sonar.projectVersion` property to your current project version either in `sonar-project.properties` file or in the `sonarProperties` in sbt config and it will run the `sonar-scanner` executable printing the progress to sbt console. 
Also, you can overwrite/set [sonarProperties](https://docs.sonarqube.org/display/SONAR/Analysis+Parameters) via system properties (java options) when you execute `sonarScan` command, e.g.:
```scala
 sbt -Dsonar.projectName=dev-projectName sonarScan
```

This plugin can be also easily used with the `sbt-release` by wrapping the `sonarScan` task in a `releaseStepTask` in the following way:

```scala
import sbtsonar.SonarPlugin.autoImport.sonarScan
 
releaseProcess := Seq[ReleaseStep](
  ...
  releaseStepCommand("coverageOn"),
  releaseStepTask(test),
  releaseStepCommand("coverageOff"),
  releaseStepTask(coverageReport),
  releaseStepTask(scapegoat),
  releaseStepTask(sonarScan),
  ...
)
```

## Examples
Please see [src/sbt-test](https://github.com/mwz/sbt-sonar/tree/master/src/sbt-test/sbt-sonar) directory for some example projects.

## Changelog
 * **1.7.0** - Renamed deprecated `sonar.scoverage.reportPath` property to `sonar.scala.scoverage.reportPath` ([#30](https://github.com/mwz/sbt-sonar/pull/30)).
 * **1.6.0** - Set automatically the `sonar.tests` property ([#25](https://github.com/mwz/sbt-sonar/pull/25)).
 * **1.5.0** - Allow sbt-sonar to run on Windows ([#16](https://github.com/mwz/sbt-sonar/pull/16)).
 * **1.4.0** - Set automatically the `sonar.scala.version` property ([#13](https://github.com/mwz/sbt-sonar/pull/13)).
 * **1.3.0** - Allow to set sonar properties via system properties ([#7](https://github.com/mwz/sbt-sonar/pull/7)).
 * **1.2.0** - Use `SONAR_SCANNER_HOME/bin` for lookup of the sonar-scanner executable ([#4](https://github.com/mwz/sbt-sonar/issues/4)).
 * **1.1.0** - Search for `sonar-scanner` home directory in system properties (`sonarScanner.home`) if `SONAR_SCANNER_HOME` environmental variable is not defined ([#1](https://github.com/mwz/sbt-sonar/issues/1)).
 * **1.0.0** - Support for sbt 1.0 :muscle: default scoverage and scapegoat report paths added automatically to the `sonarProperties` config; added unit and sbt tests.
 * **0.3.1** - Updated the scope of `scalaSource` setting key to resolve scoping ambiguity with some other plugins.
 * **0.3.0** - Defined a set of default project settings in the `sonarProperties` config key.
 * **0.2.0** - Added the ability to define sonar project properties directly in sbt.
 * **0.1.0** - First release of the plugin! :tada:

## License
The project is licensed under the Apache License v2. See the [LICENSE file](LICENSE) for more details.
