/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalajs.linker

import org.scalajs.linker.interface.LinkerOutput
import org.scalajs.linker.interface.unstable.OutputFileImpl

@deprecated("Part of old Linker interface. Use NodeOutputDirectory instead.", "1.3.0")
object NodeOutputFile {
  import NodeFS._

  def apply(path: String): LinkerOutput.File = {
    val dir = NodeOutputDirectory(dirname(path))
    new OutputFileImpl(basename(path), dir)
  }
}
