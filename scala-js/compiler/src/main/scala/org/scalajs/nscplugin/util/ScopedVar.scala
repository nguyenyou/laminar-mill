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

package org.scalajs.nscplugin.util

import language.implicitConversions

class ScopedVar[A](init: A) {
  import ScopedVar.Assignment

  private var value = init

  def this()(implicit ev: Null <:< A) = this(ev(null))

  def get: A = value
  def :=(newValue: A): Assignment[A] = new Assignment(this, newValue)
}

object ScopedVar {
  class Assignment[T](scVar: ScopedVar[T], value: T) {
    private[ScopedVar] def push(): AssignmentStackElement[T] = {
      val stack = new AssignmentStackElement(scVar, scVar.value)
      scVar.value = value
      stack
    }
  }

  private class AssignmentStackElement[T](scVar: ScopedVar[T], oldValue: T) {
    private[ScopedVar] def pop(): Unit = {
      scVar.value = oldValue
    }
  }

  implicit def toValue[T](scVar: ScopedVar[T]): T = scVar.get

  def withScopedVars[T](ass: Assignment[_]*)(body: => T): T = {
    val stack = ass.map(_.push())
    try body
    finally stack.reverse.foreach(_.pop())
  }
}
