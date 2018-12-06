package sbtsonar

import org.sonarsource.scanner.api.LogOutput
import org.sonarsource.scanner.api.LogOutput.{Level => SonarLogLevel}
import sbt.{Level, Logger}

class SbtSonarLoggerAdapter(implicit logger: Logger) extends LogOutput {
  override def log(formattedMessage: String, level: SonarLogLevel): Unit = {
    val sbtLevel =
      level match {
        case SonarLogLevel.TRACE => Level.Debug
        case SonarLogLevel.DEBUG => Level.Debug
        case SonarLogLevel.INFO  => Level.Info
        case SonarLogLevel.WARN  => Level.Warn
        case SonarLogLevel.ERROR => Level.Error
        case _                   => Level.Info
      }
    logger.log(sbtLevel, formattedMessage)
  }
}
