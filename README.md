<h1 align="left"> <img src="https://sonar-scala.com/img/logo.svg" height="80px"> sbt-sonar</h1>

[![circleci-badge]][circleci] [![bintray-badge]][bintray]
[![bintray-stats-badge]][bintray-stats] [![gitter-badge]][gitter]

[bintray]: https://bintray.com/mwz/sbt-plugin-releases/sbt-sonar/_latestVersion
[bintray-badge]:
  https://api.bintray.com/packages/mwz/sbt-plugin-releases/sbt-sonar/images/download.svg
[bintray-stats]:
  https://bintray.com/mwz/sbt-plugin-releases/sbt-sonar#statistics
[bintray-stats-badge]:
  https://img.shields.io/badge/dynamic/json.svg?uri=https://bintray.com/statistics/packageStatistics?pkgPath=/mwz/sbt-plugin-releases/sbt-sonar&query=$.totalDownloads&label=Downloads+(last+30+days)&colorB=brightgreen
[circleci]: https://circleci.com/gh/mwz/sbt-sonar
[circleci-badge]:
  https://img.shields.io/circleci/project/github/mwz/sbt-sonar/master.svg?label=Build
[gitter]: https://gitter.im/sonar-scala/sonar-scala
[gitter-badge]:
  https://img.shields.io/gitter/room/sonar-scala/sonar-scala.svg?colorB=46BC99&label=Chat

sbt-sonar is an sbt plugin, which provides an easy way to integrate Scala
projects with [SonarQube](https://www.sonarqube.org) - a tool for continuous
code inspection and quality management :white_check_mark:.

Under the hood, it uses the embedded
[sonar-scanner API](https://github.com/SonarSource/sonar-scanner-api) library,
which allows you to run SonarQube scan without the need to have the
sonar-scanner executable installed in your environment.

This plugin is particularly useful for CI when used together with e.g.
[sbt-release](https://www.github.com/sbt/sbt-release) plugin for an automated
release process in your project, but it can be also used on its own.

## Documentation

See the project website
[sonar-scala.com](https://sonar-scala.com/docs/setup/sbt-sonar) for
documentation.

## License

The project is licensed under the Apache License v2. See the [LICENSE](LICENSE)
file for more details.
