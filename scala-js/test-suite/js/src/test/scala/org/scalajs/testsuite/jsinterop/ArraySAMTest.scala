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

package org.scalajs.testsuite.jsinterop

import scala.language.implicitConversions

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

import org.junit.Assert._
import org.junit.Test

import org.scalajs.testsuite.utils.JSAssert._

class ArraySAMTest {
  import ArraySAMTest._

  @Test def jsMap(): Unit = {
    assertJSArrayEquals(js.Array(2, 3, 1, 2),
        js.Array("Sc", "ala", ".", "js").jsMap(_.length))
  }

  @Test def jsFilter(): Unit = {
    assertJSArrayEquals(js.Array(56, -20, 86),
        js.Array(56, 30, -20, 33, 54, 86).jsFilter(_ % 3 != 0))
  }
}

object ArraySAMTest {
  @inline implicit def jsArrayOps[A](array: js.Array[A]): JSArrayOps[A] =
    array.asInstanceOf[JSArrayOps[A]]

  @js.native
  trait JSArrayOps[A] extends js.Object {
    @JSName("map")
    def jsMap[B, T](callbackfn: js.ThisFunction3[T, A, Int, js.Array[A], B],
        thisArg: T): js.Array[B]
    @JSName("map")
    def jsMap[B](callbackfn: js.Function3[A, Int, js.Array[A], B]): js.Array[B]
    @JSName("map")
    def jsMap[B](callbackfn: js.Function2[A, Int, B]): js.Array[B]
    @JSName("map")
    def jsMap[B](callbackfn: js.Function1[A, B]): js.Array[B]

    @JSName("filter")
    def jsFilter[T](callbackfn: js.ThisFunction3[T, A, Int, js.Array[A], Boolean],
        thisArg: T): js.Array[A]
    @JSName("filter")
    def jsFilter(callbackfn: js.Function3[A, Int, js.Array[A], Boolean]): js.Array[A]
    @JSName("filter")
    def jsFilter(callbackfn: js.Function2[A, Int, Boolean]): js.Array[A]
    @JSName("filter")
    def jsFilter(callbackfn: js.Function1[A, Boolean]): js.Array[A]
  }
}
