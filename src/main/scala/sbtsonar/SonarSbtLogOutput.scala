/*
 * Copyright 2016-2020 All sbt-sonar contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sbtsonar

import org.sonarsource.scanner.api.LogOutput
import sbt.{Level, Logger}

object SonarSbtLogOutput {
  def apply(props: Map[String, String])(implicit logger: Logger): LogOutput =
    new LogOutput {
      def log(formattedMessage: java.lang.String, level: LogOutput.Level): Unit =
        level match {
          case LogOutput.Level.TRACE | LogOutput.Level.DEBUG =>
            val verbose: Boolean =
              props
                .get("sonar.verbose")
                .exists(_.toLowerCase == "true")
            val debugTrace: Boolean =
              props
                .get("sonar.log.level")
                .map(_.toLowerCase)
                .exists(v => v == "debug" || v == "trace")
            // Log only if "sonar.verbose" is "true"
            // or "sonar.log.level" is either "DEBUG" or "TRACE".
            if (verbose || debugTrace)
              logger.log(Level.Info, formattedMessage)
          case LogOutput.Level.ERROR =>
            logger.log(Level.Error, formattedMessage)
          case LogOutput.Level.WARN =>
            logger.log(Level.Warn, formattedMessage)
          case LogOutput.Level.INFO =>
            logger.log(Level.Info, formattedMessage)
        }
    }
}
