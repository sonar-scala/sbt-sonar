package sbtsonar

import java.io.File
import java.nio.file.Files

import sbt.IO

trait WithFile {
  def withFile(test: File => Any) {
    val file = Files.createTempFile("sonar-project", ".properties").toFile
    try test(file)
    finally IO.delete(file)
  }
}
