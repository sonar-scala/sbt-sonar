# sbt-sonar

[![circleci-badge]][circleci] [![bintray-badge]][bintray] [![bintray-stats-badge]][bintray-stats] [![gitter-badge]][gitter]

An sbt plugin which provides an easy way to integrate Scala projects with [SonarQube](https://www.sonarqube.org) - a tool for continuous code inspection and quality management :white_check_mark:.

This plugin is particularly useful when used together with [sbt-release](https://www.github.com/sbt/sbt-release) for an automated release process in your project, but it can be also used without sbt-release.

## Requirements

- sbt 0.13.5+ or 1.0+
- Scala 2.11/2.12
- [SonarQube](https://www.sonarqube.org/downloads) server - see my [sonar-scala-docker](https://github.com/mwz/sonar-scala-docker) repository, which provides a docker-compose recipes and a docker images for out-of-the-box SonarQube instance with support for [Scala](http://www.scala-lang.org), [Scoverage](https://github.com/scoverage/scalac-scoverage-plugin) (code coverage metrics), [Scalastyle](http://www.scalastyle.org) and [Scapegoat](https://github.com/sksamuel/scapegoat) (static code analysis). Alternatively, see the instructions for [manual installation](http://docs.sonarqube.org/display/SONAR/Get+Started+in+Two+Minutes).

## Installation

To install this plugin in your project, add the following to your `./project/plugins.sbt` file:

```scala
addSbtPlugin("com.github.mwz" % "sbt-sonar" % "2.0.0")
```

## Usage

You can define your project properties either in the external config file `sonar-project.properties`, which should be located in the root directory of your project as explained in [SonarQube Scanner guide](http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) or directly in sbt. By default, the plugin expects the properties to be defined in the `sonarProperties` setting key in sbt, which comes with the following set of **predefined** properties:

- **sonar.projectName** - your project name defined in the `name` sbt setting key
- **sonar.projectKey** - your project name transformed into a lowercase and dash-separated value
- **sonar.sourceEncoding** - UTF-8
- **sonar.sources** - default Scala source directory relative to the root of your project (usually `src/main/scala`, uses the value of `scalaSource in Compile` defined by sbt)
- **sonar.tests** - default Scala tests directory relative to the root of your project (usually `src/test/scala`, uses the value of `scalaSource in Test` defined by sbt)
- **sonar.scala.version** - defines the version of Scala used in your project (i.e. `scalaVersion`)
- **sonar.scala.scoverage.reportPath** - relative path to the scoverage report (e.g. `target/scala-2.12/scoverage-report/scoverage.xml`)
- **sonar.scala.scapegoat.reportPath** - relative path to the scapegoat report (e.g. `target/scala-2.12/scapegoat-report/scapegoat.xml`)

If you wish to add more properties to the existing config e.g. to configure your Sonar plugins or set up a multi-module project, use the `++=` operator, e.g.:

```scala
import sbtsonar.SonarPlugin.autoImport.sonarProperties

sonarProperties ++= Map(
  "sonar.host.url" -> "https://your-sonarqube-server.com",
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
  "sonar.host.url" -> "https://your-sonarqube-server.com",
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

### External config

To use the external `sonar-project.properties` file instead, you can set the `sonarUseExternalConfig` to `true`, e.g.:

```scala
import sbtsonar.SonarPlugin.autoImport.sonarUseExternalConfig

sonarUseExternalConfig := true
```

### Execute SonarQube scan

To run the plugin, execute the **`sonarScan`** sbt task in your project. Depending on the configuration option you have chosen, the plugin will update the `sonar.projectVersion` property to your current project version either in `sonar-project.properties` file or in the `sonarProperties` in sbt config and it will run the SonarQube scan printing the progress to sbt console.

Also, you can overwrite/set [sonarProperties](https://docs.sonarqube.org/display/SONAR/Analysis+Parameters) via system properties (java options) when you execute `sonarScan` command, e.g.:

```scala
sbt -Dsonar.projectName=dev-projectName sonarScan
```

Please remember to set the `sonar.host.url` property before you execute the analysis. You can do that either by adding it to the `sonarProperties` settings in sbt (as shown in the examples above), or you can set it via a system property, e.g.:

```scala
sbt -Dsonar.host.url=https://your-sonarqube-server.com sonarScan
```

### sbt-release

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

### Fallback mode

It is possible to make the plugin call through to a standalone [sonar-scanner](http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) executable, if that's what you prefer, for any reasons. This was the default behaviour before version 2.0 and in case you experience any issues with 2.x, you can fall back to using the standalone mode.

In order to do that, you need to have the [sonar-scanner](http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) installed on your CI server or locally, if you intend to run the analysis on your machine. You also need to make sure you have defined the `SONAR_SCANNER_HOME` environmental variable, or `sonarScanner.home` system property, and updated the global settings in `$SONAR_SCANNER_HOME/conf/sonar-scanner.properties` to point to your SonarQube instance (you can also do that by setting the `sonar.host.url` via system properties, as shown above).

To enable the fallback mode set the `sonarUseSonarScannerCli` seting to `true`, e.g.:

```scala
import sbtsonar.SonarPlugin.autoImport.sonarUseSonarScannerCli

sonarUseSonarScannerCli := true
```

## Examples

Please see [src/sbt-test](https://github.com/mwz/sbt-sonar/tree/master/src/sbt-test/sbt-sonar) directory for some example projects.

## Changelog

- **2.0.0** - Use an embedded sonar-scanner ([#34](https://github.com/mwz/sbt-sonar/pull/34)) :confetti_ball:. This version removes the dependency on having the standalone sonar-scanner-cli installed. To upgrade from 1.x please define the `sonar.host.url` property explicitly before running the `sonarScan` task (see the [Execute SonarQube section](#execute-sonarqube-scan) for more details). If you want to fallback to the default behaviour from 1.x, which makes the plugin call through to the standalone sonar-scanner, you can set the `sonarUseSonarScannerCli` setting to `true` (see the [Fallback mode](#fallback-mode) section for more details).

<details>
  <summary>Previous releases</summary>
  <ul>
  <li><strong>1.7.0</strong> - Renamed deprecated <code>sonar.scoverage.reportPath</code> property to <code>sonar.scala.scoverage.reportPath</code> (<a href="https://github.com/mwz/sbt-sonar/pull/30">#30</a>).</li>
  <li><strong>1.6.0</strong> - Set automatically the <code>sonar.tests</code> property (<a href="https://github.com/mwz/sbt-sonar/pull/25">#25</a>).</li>
  <li><strong>1.5.0</strong> - Allow sbt-sonar to run on Windows (<a href="https://github.com/mwz/sbt-sonar/pull/16">#16</a>).</li>
  <li><strong>1.4.0</strong> - Set automatically the <code>sonar.scala.version</code> property (<a href="https://github.com/mwz/sbt-sonar/pull/13">#13</a>).</li>
  <li><strong>1.3.0</strong> - Allow to set sonar properties via system properties (<a href="https://github.com/mwz/sbt-sonar/pull/7">#7</a>).</li>
  <li><strong>1.2.0</strong> - Use <code>SONAR_SCANNER_HOME/bin</code> for lookup of the sonar-scanner executable (<a href="https://github.com/mwz/sbt-sonar/issues/4">#4</a>).</li>
  <li><strong>1.1.0</strong> - Search for <code>sonar-scanner</code> home directory in system properties (<code>sonarScanner.home</code>) if <code>SONAR_SCANNER_HOME</code> environmental variable is not defined (<a href="https://github.com/mwz/sbt-sonar/issues/1">#1</a>).</li>
  <li><strong>1.0.0</strong> - Support for sbt 1.0 :muscle: default scoverage and scapegoat report paths added automatically to the <code>sonarProperties</code> config; added unit and sbt tests.</li>
  <li><strong>0.3.1</strong> - Updated the scope of <code>scalaSource</code> setting key to resolve scoping ambiguity with some other plugins.</li>
  <li><strong>0.3.0</strong> - Defined a set of default project settings in the <code>sonarProperties</code> config key.</li>
  <li><strong>0.2.0</strong> - Added the ability to define sonar project properties directly in sbt.</li>
  <li><strong>0.1.0</strong> - First release of the plugin! :tada:</li>
</ul>
</details>

# License

The project is licensed under the Apache License v2\. See the [LICENSE file](LICENSE) for more details.

[bintray]: https://bintray.com/mwz/sbt-plugin-releases/sbt-sonar/_latestVersion
[bintray-badge]: https://api.bintray.com/packages/mwz/sbt-plugin-releases/sbt-sonar/images/download.svg
[bintray-stats]: https://bintray.com/mwz/sbt-plugin-releases/sbt-sonar#statistics
[bintray-stats-badge]: https://img.shields.io/badge/dynamic/json.svg?uri=https://bintray.com/statistics/packageStatistics?pkgPath=/mwz/sbt-plugin-releases/sbt-sonar&query=$.totalDownloads&label=Downloads+(last+30+days)&colorB=brightgreen
[circleci]: https://circleci.com/gh/mwz/sbt-sonar
[circleci-badge]: https://img.shields.io/circleci/project/github/mwz/sbt-sonar/master.svg?label=Build
[gitter]: https://gitter.im/sonar-scala/sbt-sonar
[gitter-badge]: https://img.shields.io/gitter/room/sonar-scala/sbt-sonar.svg?colorB=46BC99&label=Chat
[insightio-badge]: https://img.shields.io/badge/Insight.io-Ready-brightgreen.svg
