import sbtsonar.SonarPlugin.autoImport.sonarUseExternalConfig

name := "external-config"

version := "0.1"

scalaVersion := "2.12.4"

scapegoatVersion in ThisBuild := "1.3.3"

sonarUseExternalConfig := true

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
