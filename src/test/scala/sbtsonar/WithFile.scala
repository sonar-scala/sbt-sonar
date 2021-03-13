/*
 * Copyright 2016-2021 All sbt-sonar contributors
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

import java.io.File
import java.nio.file.Files

import sbt.IO

trait WithFile {
  def withFile(test: File => Any): Unit = {
    val file = Files.createTempFile("sonar-project", ".properties").toFile
    try test(file)
    finally IO.delete(file)
  }
}
