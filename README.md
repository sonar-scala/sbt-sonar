<h1 align="left"> <img src="https://sonar-scala.com/img/logo.svg" height="80px"> sbt-sonar</h1>

![](https://img.shields.io/github/workflow/status/sonar-scala/sbt-sonar/Release/master)
[![maven-badge]][maven] [![gitter-badge]][gitter]

[maven]: https://search.maven.org/artifact/com.sonar-scala/sbt-sonar
[maven-badge]:
  https://maven-badges.herokuapp.com/maven-central/com.sonar-scala/sbt-sonar/badge.svg
[bintray]: https://bintray.com/mwz/sbt-plugin-releases/sbt-sonar/_latestVersion
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
