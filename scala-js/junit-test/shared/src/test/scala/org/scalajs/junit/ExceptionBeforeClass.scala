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

package org.scalajs.junit

import org.junit._
import org.scalajs.junit.utils._

object ExceptionBeforeClass {
  @BeforeClass def beforeClass(): Unit =
    throw new IllegalArgumentException("foo")
}

class ExceptionBeforeClass {
  @Test def test1(): Unit = ()
  @Test def test2(): Unit = ()
}

class ExceptionBeforeClassAssertions extends JUnitTest
