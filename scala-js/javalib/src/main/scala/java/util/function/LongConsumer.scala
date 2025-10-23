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

package java.util.function

@FunctionalInterface
trait LongConsumer {
  def accept(value: Long): Unit

  def andThen(after: LongConsumer): LongConsumer = { (value: Long) =>
    this.accept(value)
    after.accept(value)
  }
}
