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

package org.scalajs.linker.analyzer

import scala.collection.mutable
import scala.concurrent.ExecutionContext

private[analyzer] object Platform {
  def emptyThreadSafeMap[K, V]: mutable.Map[K, V] = mutable.Map.empty

  def adjustExecutionContextForParallelism(ec: ExecutionContext,
      parallel: Boolean): ExecutionContext = {
    ec // we're never parallel on JS
  }
}
