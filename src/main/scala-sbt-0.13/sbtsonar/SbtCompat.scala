package sbtsonar

import scala.sys.process.ProcessBuilder

object SbtCompat {
  val Using = sbt.Using

  def process(process: ProcessBuilder): Stream[String] =
    process.lines
}
