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

package org.scalajs.linker.interface

object StandardConfigPlatformExtensions {

  final class ConfigExt private[interface] (private val self: StandardConfig)
      extends AnyVal {

    /** Whether to actually use the Google Closure Compiler pass. */
    def closureCompiler: Boolean = self.closureCompilerIfAvailable

    def withClosureCompiler(closureCompiler: Boolean): StandardConfig =
      self.withClosureCompilerIfAvailable(closureCompiler)
  }
}
