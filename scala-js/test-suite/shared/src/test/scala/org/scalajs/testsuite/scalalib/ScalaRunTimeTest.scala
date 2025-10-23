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

package org.scalajs.testsuite.scalalib

import org.junit.Test
import org.junit.Assert.{assertFalse, assertTrue}

import scala.concurrent.Future

class ScalaRunTimeTest {

  @Test def scalaRunTimeIsArray(): Unit = {
    def isScalaArray(x: Any): Boolean = {
      x match {
        case _: Array[_] => true
        case _           => false
      }
    }

    assertTrue(isScalaArray(Array(1, 2, 3)))
    assertFalse(isScalaArray(42))
    assertFalse(isScalaArray(Future.successful(42)))
  }

}
