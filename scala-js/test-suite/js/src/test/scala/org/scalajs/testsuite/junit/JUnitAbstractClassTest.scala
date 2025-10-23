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

package org.scalajs.testsuite.junit

import org.junit.Assert._
import org.junit.Test

abstract class JUnitAbstractClassTest {
  @Test def test1(): Unit = ()
}

class JUnitAbstractClassExtended1Test extends JUnitAbstractClassTest

class JUnitAbstractClassExtended2Test extends JUnitAbstractClassTest {
  @Test def test2(): Unit = ()
}

class JUnitAbstractClassTestCheck {
  @Test def testAbstractClass1(): Unit = {
    val boot = JUnitUtil.loadBootstrapper(
        "org.scalajs.testsuite.junit.JUnitAbstractClassExtended1Test")
    try {
      boot.invokeTest(boot.newInstance(), "test1")
    } catch {
      case e: Throwable =>
        fail(s"Could not invoke a test: ${e.getMessage}")
    }
  }

  @Test def testAbstractClass2(): Unit = {
    val boot = JUnitUtil.loadBootstrapper(
        "org.scalajs.testsuite.junit.JUnitAbstractClassExtended2Test")
    try {
      boot.invokeTest(boot.newInstance(), "test1")
      boot.invokeTest(boot.newInstance(), "test2")
    } catch {
      case e: Throwable =>
        fail(s"Could not invoke a test: ${e.getMessage}")
    }
  }
}
