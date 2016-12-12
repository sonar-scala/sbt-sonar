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
addSbtPlugin("com.github.mwz" % "sbt-sonar" % "0.1.0")
```

## Usage
Before using the plugin, make sure you have defined the `SONAR_SCANNER_HOME` environmental variable and updated the global settings in `$SONAR_SCANNER_HOME/conf/sonar-scanner.properties` to point to your SonarQube instance. You will also need a `sonar-project.properties` config file in the root directory of your project as explained in [SonarQube Scanner guide](http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner).

To run the plugin, execute the `sonarScan` sbt task in your project. The plugin will update the `sonar.projectVersion` property in `sonar-project.properties` to your current project version and it will run the `sonar-scanner` executable printing the progress to sbt console.

This plugin can be also easilly used with the `sbt-release` by wrapping the `sonarScan` task in a `releaseStepTask` in the following way:

```scala
import sbtsonar.SonarPlugin.autoImport.sonarScan
 
releaseProcess := Seq[ReleaseStep](
  ...
  releaseStepTask(sonarScan),
  ...
)
```

## Changelog
 * **0.1.0** - First release of the plugin :tada:!

## License
The project is licensed under the Apache License v2. See the [LICENSE file](LICENSE) for more details.
