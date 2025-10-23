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

import scala.scalajs.js
import scala.scalajs.js.annotation._

import org.junit.Assert._
import org.junit.Test

import org.scalajs.testsuite.utils.JSAssert._

/** 2.12+ tests for `@JSOptional`.
 *
 *  This class is basically a copy-paste of `JSOptionalTest`, where
 *  `override val/def`s do not have an explicit result type. Instead, they
 *  are inferred from the superclasses.
 */
class JSOptionalTest212 {
  import JSOptionalTest212._

  @Test def classImplementsTraitWithOptional(): Unit = {
    val obj = new ClassImplementsTraitWithOptional

    assertEquals(js.undefined, obj.x)
    assertFalse(obj.hasOwnProperty("x"))

    assertEquals(js.undefined, obj.y)
    assertFalse(js.Object.hasProperty(obj, "y"))

    assertEquals(js.undefined, obj.y2)
    assertFalse(js.Object.hasProperty(obj, "y2"))

    assertEquals(js.undefined, obj.z)
    assertFalse(obj.hasOwnProperty("z"))
    obj.z = Some(3)
    assertEquals(Some(3), obj.z)
  }

  @Test def anonClassImplementsTraitWithOptional(): Unit = {
    val obj = new TraitWithOptional {}

    assertEquals(js.undefined, obj.x)
    assertFalse(obj.hasOwnProperty("x"))

    assertEquals(js.undefined, obj.y)
    assertFalse(js.Object.hasProperty(obj, "y"))

    assertEquals(js.undefined, obj.y2)
    assertFalse(js.Object.hasProperty(obj, "y2"))

    assertEquals(js.undefined, obj.z)
    assertFalse(obj.hasOwnProperty("z"))
    obj.z = Some(3)
    assertEquals(Some(3), obj.z)
  }

  @Test def undefinedInClassIsNotOptional(): Unit = {
    val obj = new UndefinedInClassIsNotOptional

    assertEquals(js.undefined, obj.x)
    assertTrue(obj.hasOwnProperty("x"))

    assertEquals(js.undefined, obj.y)
    assertTrue(js.Object.hasProperty(obj, "y"))

    assertEquals(js.undefined, obj.y2)
    assertTrue(js.Object.hasProperty(obj, "y2"))

    assertEquals(js.undefined, obj.z)
    assertTrue(obj.hasOwnProperty("z"))
    obj.z = Some(3)
    assertEquals(Some(3), obj.z)
  }

  @Test def overrideWithUndefinedInClassIsNotOptional(): Unit = {
    val obj = new OverrideWithUndefinedInClassIsNotOptional

    assertEquals(js.undefined, obj.x)
    assertTrue(obj.hasOwnProperty("x"))

    assertEquals(js.undefined, obj.y)
    assertTrue(js.Object.hasProperty(obj, "y"))

    assertEquals(js.undefined, obj.y2)
    assertTrue(js.Object.hasProperty(obj, "y2"))

    assertEquals(js.undefined, obj.z)
    assertTrue(obj.hasOwnProperty("z"))
    obj.z = Some(3)
    assertEquals(Some(3), obj.z)
  }

  @Test def classOverrideOptionalWithConcrete(): Unit = {
    val obj = new ClassImplementsTraitWithOptionalOverrideWithConcrete

    assertEquals(42, obj.x)
    assertTrue(obj.hasOwnProperty("x"))

    assertEquals("hello", obj.y)
    assertTrue(obj.hasOwnProperty("y"))

    assertEquals("world", obj.y2)
    assertTrue(js.Object.hasProperty(obj, "y2"))

    assertEquals(Some(5), obj.z)
    assertTrue(obj.hasOwnProperty("z"))
    obj.z = Some(3)
    assertEquals(Some(3), obj.z)
  }

  @Test def anonClassOverrideOptionalWithConcrete(): Unit = {
    val obj = new TraitWithOptional {
      override val x = 42
      override val y = "hello"
      override def y2 = "world" // scalastyle:ignore
      z = Some(5)
    }

    assertEquals(42, obj.x)
    assertTrue(obj.hasOwnProperty("x"))

    assertEquals("hello", obj.y)
    assertTrue(obj.hasOwnProperty("y"))

    assertEquals("world", obj.y2)
    assertTrue(js.Object.hasProperty(obj, "y2"))

    assertEquals(Some(5), obj.z)
    assertTrue(obj.hasOwnProperty("z"))
    obj.z = Some(3)
    assertEquals(Some(3), obj.z)
  }

  @Test def overrideClassAbstractWithOptional(): Unit = {
    trait OverrideClassAbstractWithOptional extends ClassWithAbstracts {
      val x = js.undefined
      def y = js.undefined
      val y2 = js.undefined
      var z: js.UndefOr[Option[Int]] = js.undefined
    }

    val obj = new OverrideClassAbstractWithOptional {}

    assertEquals(js.undefined, obj.x)
    assertFalse(obj.hasOwnProperty("x"))

    assertEquals(js.undefined, obj.y)
    assertFalse(js.Object.hasProperty(obj, "y"))

    assertEquals(js.undefined, obj.y2)
    assertFalse(js.Object.hasProperty(obj, "y2"))

    assertEquals(js.undefined, obj.z)
    assertFalse(obj.hasOwnProperty("z"))
    obj.z = Some(3)
    assertEquals(Some(3), obj.z)
  }

  @Test def overrideTraitAbstractWithOptional(): Unit = {
    trait TraitWithAbstracts extends js.Object {
      val x: js.UndefOr[Int]
      def y: js.UndefOr[String]
      def y2: js.UndefOr[String]
      var z: js.UndefOr[Option[Int]]
    }

    trait OverrideTraitAbstractWithOptional extends TraitWithAbstracts {
      val x = js.undefined
      def y = js.undefined
      val y2 = js.undefined
      var z: js.UndefOr[Option[Int]] = js.undefined
    }

    val obj = new OverrideTraitAbstractWithOptional {}

    assertEquals(js.undefined, obj.x)
    assertFalse(obj.hasOwnProperty("x"))

    assertEquals(js.undefined, obj.y)
    assertFalse(js.Object.hasProperty(obj, "y"))

    assertEquals(js.undefined, obj.y2)
    assertFalse(js.Object.hasProperty(obj, "y2"))

    assertEquals(js.undefined, obj.z)
    assertFalse(obj.hasOwnProperty("z"))
    obj.z = Some(3)
    assertEquals(Some(3), obj.z)
  }

  /* @Test def traitWithOptionalFunction(): Unit
   * Moved to JSOptionalTest212FunParamInference.scala because Scala 3 cannot
   * infer the parameter type yet when using true union types.
   *
   * See https://github.com/lampepfl/dotty/issues/11694
   */
}

object JSOptionalTest212 {
  trait TraitWithOptional extends js.Object {
    val x: js.UndefOr[Int] = js.undefined
    def y: js.UndefOr[String] = js.undefined
    def y2: js.UndefOr[String] = js.undefined
    var z: js.UndefOr[Option[Int]] = js.undefined
  }

  class ClassImplementsTraitWithOptional extends TraitWithOptional

  class UndefinedInClassIsNotOptional extends js.Object {
    val x: js.UndefOr[Int] = js.undefined
    def y: js.UndefOr[String] = js.undefined
    def y2: js.UndefOr[String] = js.undefined
    var z: js.UndefOr[Option[Int]] = js.undefined
  }

  class OverrideWithUndefinedInClassIsNotOptional extends TraitWithOptional {
    override val x = js.undefined
    override def y = js.undefined // scalastyle:ignore
    override def y2 = js.undefined // scalastyle:ignore
    z = js.undefined
  }

  class ClassImplementsTraitWithOptionalOverrideWithConcrete
      extends TraitWithOptional {
    override val x = 42
    override val y = "hello"
    override def y2 = "world" // scalastyle:ignore
    z = Some(5)
  }

  abstract class ClassWithAbstracts extends js.Object {
    val x: js.UndefOr[Int]
    def y: js.UndefOr[String]
    def y2: js.UndefOr[String]
    var z: js.UndefOr[Option[Int]]
  }
}
