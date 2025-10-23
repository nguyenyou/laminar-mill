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

package org.scalajs.nscplugin.test

import org.scalajs.nscplugin.test.util._
import org.scalajs.nscplugin.test.util.VersionDependentUtils.methodSig

import org.junit.Test
import org.junit.Ignore

// scalastyle:off line.size.limit

class NonNativeJSTypeTest extends DirectTest with TestHelpers {

  override def preamble: String =
    """
    import scala.scalajs.js
    import scala.scalajs.js.annotation._
    """

  @Test
  def noExtendAnyRef: Unit = {
    """
    class A extends js.Any
    """ hasErrors
    """
      |newSource1.scala:5: error: Non-native JS classes and objects cannot directly extend AnyRef. They must extend a JS class (native or not).
      |    class A extends js.Any
      |          ^
    """

    """
    object A extends js.Any
    """ hasErrors
    """
      |newSource1.scala:5: error: Non-native JS classes and objects cannot directly extend AnyRef. They must extend a JS class (native or not).
      |    object A extends js.Any
      |           ^
    """
  }

  @Test
  def noExtendNativeTrait: Unit = {
    """
    @js.native
    trait NativeTrait extends js.Object

    class A extends NativeTrait

    trait B extends NativeTrait

    object C extends NativeTrait

    object Container {
      val x = new NativeTrait {}
    }
    """ hasErrors
    """
      |newSource1.scala:8: error: Non-native JS types cannot directly extend native JS traits.
      |    class A extends NativeTrait
      |          ^
      |newSource1.scala:10: error: Non-native JS types cannot directly extend native JS traits.
      |    trait B extends NativeTrait
      |          ^
      |newSource1.scala:12: error: Non-native JS types cannot directly extend native JS traits.
      |    object C extends NativeTrait
      |           ^
      |newSource1.scala:15: error: Non-native JS types cannot directly extend native JS traits.
      |      val x = new NativeTrait {}
      |                  ^
    """
  }

  @Test
  def noConcreteApplyMethod: Unit = {
    """
    class A extends js.Object {
      def apply(arg: Int): Int = arg
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: A non-native JS class cannot declare a concrete method named `apply` without `@JSName`
      |      def apply(arg: Int): Int = arg
      |          ^
    """

    """
    trait B extends js.Object {
      def apply(arg: Int): Int
    }

    class A extends B {
      def apply(arg: Int): Int = arg
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: A non-native JS type can only declare an abstract method named `apply` without `@JSName` if it is the SAM of a trait that extends js.Function
      |      def apply(arg: Int): Int
      |          ^
      |newSource1.scala:10: error: A non-native JS class cannot declare a concrete method named `apply` without `@JSName`
      |      def apply(arg: Int): Int = arg
      |          ^
    """

    """
    abstract class B extends js.Object {
      def apply(arg: Int): Int
    }

    class A extends B {
      def apply(arg: Int): Int = arg
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: A non-native JS type can only declare an abstract method named `apply` without `@JSName` if it is the SAM of a trait that extends js.Function
      |      def apply(arg: Int): Int
      |          ^
      |newSource1.scala:10: error: A non-native JS class cannot declare a concrete method named `apply` without `@JSName`
      |      def apply(arg: Int): Int = arg
      |          ^
    """

    """
    object Enclosing {
      val f = new js.Object {
        def apply(arg: Int): Int = arg
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: A non-native JS class cannot declare a concrete method named `apply` without `@JSName`
      |        def apply(arg: Int): Int = arg
      |            ^
    """

    """
    object Enclosing {
      val f = new js.Function {
        def apply(arg: Int): Int = arg
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: A non-native JS class cannot declare a concrete method named `apply` without `@JSName`
      |        def apply(arg: Int): Int = arg
      |            ^
    """

    """
    object Enclosing {
      val f = new js.Function1[Int, Int] {
        def apply(arg: Int): Int = arg
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: A non-native JS class cannot declare a concrete method named `apply` without `@JSName`
      |        def apply(arg: Int): Int = arg
      |            ^
    """
  }

  @Test
  def noUnaryOp: Unit = {
    """
    class A extends js.Object {
      def unary_+ : Int = 1
      def unary_-() : Int = 1
    }
    """ hasErrors
    """
      |newSource1.scala:6: warning: Method 'unary_+' should have an explicit @JSName or @JSOperator annotation because its name is one of the JavaScript operators
      |      def unary_+ : Int = 1
      |          ^
      |newSource1.scala:6: error: A non-native JS class cannot declare a method named like a unary operation without `@JSName`
      |      def unary_+ : Int = 1
      |          ^
      |newSource1.scala:7: warning: Method 'unary_-' should have an explicit @JSName or @JSOperator annotation because its name is one of the JavaScript operators
      |      def unary_-() : Int = 1
      |          ^
      |newSource1.scala:7: error: A non-native JS class cannot declare a method named like a unary operation without `@JSName`
      |      def unary_-() : Int = 1
      |          ^
    """

    """
    class A extends js.Object {
      def unary_+(x: Int): Int = 2

      @JSName("unary_-")
      def unary_-() : Int = 1
    }
    """.succeeds()
  }

  @Test
  def noBinaryOp: Unit = {
    """
    class A extends js.Object {
      def +(x: Int): Int = x
      def &&(x: String): String = x
    }
    """ hasErrors
    """
      |newSource1.scala:6: warning: Method '+' should have an explicit @JSName or @JSOperator annotation because its name is one of the JavaScript operators
      |      def +(x: Int): Int = x
      |          ^
      |newSource1.scala:6: error: A non-native JS class cannot declare a method named like a binary operation without `@JSName`
      |      def +(x: Int): Int = x
      |          ^
      |newSource1.scala:7: warning: Method '&&' should have an explicit @JSName or @JSOperator annotation because its name is one of the JavaScript operators
      |      def &&(x: String): String = x
      |          ^
      |newSource1.scala:7: error: A non-native JS class cannot declare a method named like a binary operation without `@JSName`
      |      def &&(x: String): String = x
      |          ^
    """

    """
    class A extends js.Object {
      def + : Int = 2

      def -(x: Int, y: Int): Int = 7

      @JSName("&&")
      def &&(x: String): String = x
    }
    """ hasWarns
    """
      |newSource1.scala:6: warning: Method '+' should have an explicit @JSName or @JSOperator annotation because its name is one of the JavaScript operators
      |      def + : Int = 2
      |          ^
      |newSource1.scala:8: warning: Method '-' should have an explicit @JSName or @JSOperator annotation because its name is one of the JavaScript operators
      |      def -(x: Int, y: Int): Int = 7
      |          ^
    """
  }

  @Test // #4281
  def noExtendJSFunctionAnon: Unit = {
    """
    @js.native
    @JSGlobal("bad")
    abstract class BadFunction extends js.Function1[Int, String]

    object Test {
      new BadFunction {
        def apply(x: Int): String = "f"
      }
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: A non-native JS class cannot declare a concrete method named `apply` without `@JSName`
      |        def apply(x: Int): String = "f"
      |            ^
    """

    """
    class $anonfun extends js.Function1[Int, String] {
      def apply(x: Int): String = "f"
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: A non-native JS class cannot declare a concrete method named `apply` without `@JSName`
      |      def apply(x: Int): String = "f"
      |          ^
    """
  }

  @Test
  def noBracketAccess: Unit = {
    """
    class A extends js.Object {
      @JSBracketAccess
      def foo(index: Int, arg: Int): Int = arg
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: @JSBracketAccess is not allowed in non-native JS classes
      |      def foo(index: Int, arg: Int): Int = arg
      |          ^
    """
  }

  @Test
  def noBracketCall: Unit = {
    """
    class A extends js.Object {
      @JSBracketCall
      def foo(m: String, arg: Int): Int = arg
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: @JSBracketCall is not allowed in non-native JS classes
      |      def foo(m: String, arg: Int): Int = arg
      |          ^
    """
  }

  @Test
  def noCollapseOverloadsOnJSName: Unit = {
    """
    class A extends js.Object {
      @JSName("bar")
      def foo(): Int = 42

      def bar(): Int = 24
    }
    """ hasErrors
    s"""
      |newSource1.scala:9: error: Cannot disambiguate overloads for method bar with types
      |  ${methodSig("()", "Int")}
      |  ${methodSig("()", "Int")}
      |      def bar(): Int = 24
      |          ^
    """

    """
    class A extends js.Object {
      def bar(): Int = 24

      @JSName("bar")
      def foo(): Int = 42
    }
    """ hasErrors
    s"""
      |newSource1.scala:9: error: Cannot disambiguate overloads for method bar with types
      |  ${methodSig("()", "Int")}
      |  ${methodSig("()", "Int")}
      |      def foo(): Int = 42
      |          ^
    """

    """
    class A extends js.Object {
      @JSName("bar")
      def foo(): Int = 42
    }

    class B extends A {
      def bar(): Int = 24
    }
    """ hasErrors
    s"""
      |newSource1.scala:11: error: Cannot disambiguate overloads for method bar with types
      |  ${methodSig("()", "Int")}
      |  ${methodSig("()", "Int")}
      |      def bar(): Int = 24
      |          ^
    """

    """
    @js.native
    @JSGlobal
    class A extends js.Object {
      @JSName("bar")
      def foo(): Int = js.native
    }

    class B extends A {
      def bar(): Int = 24
    }
    """ hasErrors
    s"""
      |newSource1.scala:13: error: Cannot disambiguate overloads for method bar with types
      |  ${methodSig("()", "Int")}
      |  ${methodSig("()", "Int")}
      |      def bar(): Int = 24
      |          ^
    """

    """
    @js.native
    @JSGlobal
    class Foo extends js.Object {
      def foo(x: Int): Int = js.native

      @JSName("foo")
      def bar(x: Int): Int = js.native
    }

    class Bar extends Foo {
      def foo(): Int = 42
    }
    """ hasErrors
    s"""
      |newSource1.scala:14: error: Cannot disambiguate overloads for method foo with types
      |  ${methodSig("(x: Int)", "Int")}
      |  ${methodSig("(x: Int)", "Int")}
      |    class Bar extends Foo {
      |          ^
    """
  }

  @Test
  def noOverloadedPrivate: Unit = {
    """
    class A extends js.Object {
      private def foo(i: Int): Int = i
      private def foo(s: String): String = s
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: Private methods in non-native JS classes cannot be overloaded. Use different names instead.
      |      private def foo(i: Int): Int = i
      |                  ^
      |newSource1.scala:7: error: Private methods in non-native JS classes cannot be overloaded. Use different names instead.
      |      private def foo(s: String): String = s
      |                  ^
    """

    """
    object A extends js.Object {
      private def foo(i: Int): Int = i
      private def foo(s: String): String = s
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: Private methods in non-native JS classes cannot be overloaded. Use different names instead.
      |      private def foo(i: Int): Int = i
      |                  ^
      |newSource1.scala:7: error: Private methods in non-native JS classes cannot be overloaded. Use different names instead.
      |      private def foo(s: String): String = s
      |                  ^
    """

    """
    object Enclosing {
      class A extends js.Object {
        private[Enclosing] def foo(i: Int): Int = i
        private def foo(s: String): String = s
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: Private methods in non-native JS classes cannot be overloaded. Use different names instead.
      |        private[Enclosing] def foo(i: Int): Int = i
      |                               ^
      |newSource1.scala:8: error: Private methods in non-native JS classes cannot be overloaded. Use different names instead.
      |        private def foo(s: String): String = s
      |                    ^
    """

    """
    class A extends js.Object {
      private def foo(i: Int): Int = i
      def foo(s: String): String = s
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: Private methods in non-native JS classes cannot be overloaded. Use different names instead.
      |      private def foo(i: Int): Int = i
      |                  ^
    """

    """
    object Enclosing {
      class A extends js.Object {
        private[Enclosing] def foo(i: Int): Int = i
        def foo(s: String): String = s
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: Private methods in non-native JS classes cannot be overloaded. Use different names instead.
      |        private[Enclosing] def foo(i: Int): Int = i
      |                               ^
    """
  }

  @Test
  def noVirtualQualifiedPrivate: Unit = {
    """
    object Enclosing {
      class A extends js.Object {
        private[Enclosing] def foo(i: Int): Int = i
        private[Enclosing] val x: Int = 3
        private[Enclosing] var y: Int = 5
      }

      class B extends A {
        override private[Enclosing] final def foo(i: Int): Int = i + 1
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] def foo(i: Int): Int = i
      |                               ^
      |newSource1.scala:8: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] val x: Int = 3
      |                               ^
      |newSource1.scala:9: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] var y: Int = 5
      |                               ^
    """

    """
    object Enclosing {
      object A extends js.Object {
        private[Enclosing] def foo(i: Int): Int = i
        private[Enclosing] val x: Int = 3
        private[Enclosing] var y: Int = 5
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] def foo(i: Int): Int = i
      |                               ^
      |newSource1.scala:8: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] val x: Int = 3
      |                               ^
      |newSource1.scala:9: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] var y: Int = 5
      |                               ^
    """

    """
    object Enclosing {
      abstract class A extends js.Object {
        private[Enclosing] def foo(i: Int): Int
        private[Enclosing] val x: Int
        private[Enclosing] var y: Int
      }

      class B extends A {
        override private[Enclosing] final def foo(i: Int): Int = i + 1
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] def foo(i: Int): Int
      |                               ^
      |newSource1.scala:8: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] val x: Int
      |                               ^
      |newSource1.scala:9: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] var y: Int
      |                               ^
    """

    """
    object Enclosing {
      trait A extends js.Object {
        private[Enclosing] def foo(i: Int): Int
        private[Enclosing] val x: Int
        private[Enclosing] var y: Int
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] def foo(i: Int): Int
      |                               ^
      |newSource1.scala:8: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] val x: Int
      |                               ^
      |newSource1.scala:9: error: Qualified private members in non-native JS classes must be final
      |        private[Enclosing] var y: Int
      |                               ^
    """

    """
    object Enclosing {
      class A private () extends js.Object

      class B private[this] () extends js.Object

      class C private[Enclosing] () extends js.Object
    }
    """.succeeds()

    """
    object Enclosing {
      class A extends js.Object {
        final private[Enclosing] def foo(i: Int): Int = i
      }
    }
    """.succeeds()

    """
    object Enclosing {
      class A extends js.Object {
        private def foo(i: Int): Int = i
        private[this] def bar(i: Int): Int = i + 1
      }
    }
    """.succeeds()

    """
    object Enclosing {
      object A extends js.Object {
        final private[Enclosing] def foo(i: Int): Int = i
      }
    }
    """.succeeds()

    """
    object Enclosing {
      object A extends js.Object {
        private def foo(i: Int): Int = i
        private[this] def bar(i: Int): Int = i + 1
      }
    }
    """.succeeds()

    """
    object Enclosing {
      abstract class A extends js.Object {
        final private[Enclosing] def foo(i: Int): Int
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: abstract member may not have final modifier
      |        final private[Enclosing] def foo(i: Int): Int
      |                                     ^
    """

    """
    object Enclosing {
      trait A extends js.Object {
        final private[Enclosing] def foo(i: Int): Int
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: abstract member may not have final modifier
      |        final private[Enclosing] def foo(i: Int): Int
      |                                     ^
    """
  }

  @Test
  def noUseJsNative: Unit = {
    """
    class A extends js.Object {
      def foo = js.native
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: js.native may only be used as stub implementation in facade types
      |      def foo = js.native
      |                   ^
    """

    """
    object A extends js.Object {
      def foo = js.native
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: js.native may only be used as stub implementation in facade types
      |      def foo = js.native
      |                   ^
    """

    """
    class A {
      val x = new js.Object {
        def a: Int = js.native
      }
    }
    """ hasErrors
    """
      |newSource1.scala:7: error: js.native may only be used as stub implementation in facade types
      |        def a: Int = js.native
      |                        ^
    """
  }

  @Test
  def noNonLiteralJSName: Unit = {
    """
    object A {
      val a = "Hello"
      final val b = "World"
    }

    class B extends js.Object {
      @JSName(A.a)
      def foo: Int = 5
      @JSName(A.b)
      def bar: Int = 5
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: A string argument to JSName must be a literal string
      |      @JSName(A.a)
      |                ^
    """

    """
    object A {
      val a = "Hello"
      final val b = "World"
    }

    object B extends js.Object {
      @JSName(A.a)
      def foo: Int = 5
      @JSName(A.b)
      def bar: Int = 5
    }
    """ hasErrors
    """
      |newSource1.scala:11: error: A string argument to JSName must be a literal string
      |      @JSName(A.a)
      |                ^
    """
  }

  @Test
  def noApplyProperty: Unit = {
    // def apply

    """
    class A extends js.Object {
      def apply: Int = 42
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: A member named apply represents function application in JavaScript. A parameterless member should be exported as a property. You must add @JSName("apply")
      |      def apply: Int = 42
      |          ^
    """

    """
    class A extends js.Object {
      @JSName("apply")
      def apply: Int = 42
    }
    """.succeeds()

    // val apply

    """
    class A extends js.Object {
      val apply: Int = 42
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: A member named apply represents function application in JavaScript. A parameterless member should be exported as a property. You must add @JSName("apply")
      |      val apply: Int = 42
      |          ^
    """

    """
    class A extends js.Object {
      @JSName("apply")
      val apply: Int = 42
    }
    """.succeeds()

    // var apply

    """
    class A extends js.Object {
      var apply: Int = 42
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: A member named apply represents function application in JavaScript. A parameterless member should be exported as a property. You must add @JSName("apply")
      |      var apply: Int = 42
      |          ^
    """

    """
    class A extends js.Object {
      @JSName("apply")
      var apply: Int = 42
    }
    """.succeeds()
  }

  @Test
  def noExportClassWithOnlyPrivateCtors: Unit = {
    """
    @JSExportTopLevel("A")
    class A private () extends js.Object
    """ hasErrors
    """
      |newSource1.scala:5: error: You may not export a class that has only private constructors
      |    @JSExportTopLevel("A")
      |     ^
    """

    """
    @JSExportTopLevel("A")
    class A private[this] () extends js.Object
    """ hasErrors
    """
      |newSource1.scala:5: error: You may not export a class that has only private constructors
      |    @JSExportTopLevel("A")
      |     ^
    """

    """
    @JSExportTopLevel("A")
    class A private[A] () extends js.Object

    object A
    """ hasErrors
    """
      |newSource1.scala:5: error: You may not export a class that has only private constructors
      |    @JSExportTopLevel("A")
      |     ^
    """
  }

  @Test
  def noConcreteMemberInTrait: Unit = {
    """
    trait A extends js.Object {
      def foo(x: Int): Int = x + 1
      def bar[A](x: A): A = x

      object InnerScalaObject
      object InnerJSObject extends js.Object
      @js.native object InnerNativeJSObject extends js.Object

      class InnerScalaClass
      class InnerJSClass extends js.Object
      @js.native class InnerNativeJSClass extends js.Object
    }
    """ hasErrors
    """
      |newSource1.scala:6: error: In non-native JS traits, defs with parentheses must be abstract.
      |      def foo(x: Int): Int = x + 1
      |                               ^
      |newSource1.scala:7: error: In non-native JS traits, defs with parentheses must be abstract.
      |      def bar[A](x: A): A = x
      |                            ^
      |newSource1.scala:9: error: Non-native JS traits cannot contain inner classes or objects
      |      object InnerScalaObject
      |             ^
      |newSource1.scala:10: error: Non-native JS traits cannot contain inner classes or objects
      |      object InnerJSObject extends js.Object
      |             ^
      |newSource1.scala:11: error: non-native JS classes, traits and objects may not have native JS members
      |      @js.native object InnerNativeJSObject extends js.Object
      |                        ^
      |newSource1.scala:13: error: Non-native JS traits cannot contain inner classes or objects
      |      class InnerScalaClass
      |            ^
      |newSource1.scala:14: error: Non-native JS traits cannot contain inner classes or objects
      |      class InnerJSClass extends js.Object
      |            ^
      |newSource1.scala:15: error: non-native JS classes, traits and objects may not have native JS members
      |      @js.native class InnerNativeJSClass extends js.Object
      |                       ^
    """
  }

  @Test // #4511
  def noConflictingProperties: Unit = {
    """
    class A extends js.Object {
      def a: Unit = ()

      @JSName("a")
      def b: Unit = ()
    }
    """ hasErrors
    s"""
      |newSource1.scala:9: error: Cannot disambiguate overloads for getter a with types
      |  ${methodSig("()", "Unit")}
      |  ${methodSig("()", "Unit")}
      |      def b: Unit = ()
      |          ^
    """

    """
    class A extends js.Object {
      class B extends js.Object

      object B
    }
    """ hasErrors
    s"""
      |newSource1.scala:8: error: Cannot disambiguate overloads for getter B with types
      |  ${methodSig("()", "A$B.type")}
      |  ${methodSig("()", "Object")}
      |      object B
      |             ^
    """
  }

}
