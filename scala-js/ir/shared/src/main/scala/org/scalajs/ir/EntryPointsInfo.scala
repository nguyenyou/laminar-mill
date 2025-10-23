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

package org.scalajs.ir

import Names.ClassName
import Trees._

final class EntryPointsInfo(
    val className: ClassName,
    val hasEntryPoint: Boolean
)

object EntryPointsInfo {
  def forClassDef(classDef: ClassDef): EntryPointsInfo = {
    val hasEntryPoint = {
      classDef.topLevelExportDefs.nonEmpty ||
      classDef.methods.exists(m =>
          m.flags.namespace == MemberNamespace.StaticConstructor &&
          m.methodName.isStaticInitializer)
    }
    new EntryPointsInfo(classDef.name.name, hasEntryPoint)
  }
}
