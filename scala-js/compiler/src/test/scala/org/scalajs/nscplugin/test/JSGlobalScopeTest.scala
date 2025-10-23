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
import org.scalajs.nscplugin.test.util.VersionDependentUtils.scalaSupportsNoWarn

import org.junit.Test
import org.junit.Ignore
import org.junit.Assume._

// scalastyle:off line.size.limit

class JSGlobalScopeTest extends DirectTest with TestHelpers {

  override def extraArgs: List[String] =
    super.extraArgs :+ "-deprecation"

  override def preamble: String = {
    """
    import scala.scalajs.js
    import scala.scalajs.js.annotation._

    object Symbols {
      val sym: js.Symbol = js.Symbol()
    }

    @js.native
    @JSGlobalScope
    object SomeGlobalScope extends js.Any {
      var validVar: Int = js.native
      def validDef(): Int = js.native

      var `not-a-valid-identifier-var`: Int = js.native
      def `not-a-valid-identifier-def`(): Int = js.native

      @JSOperator def +(that: Int): Int = js.native

      def apply(x: Int): Int = js.native

      @JSBracketAccess
      def bracketSelect(name: String): Int = js.native
      @JSBracketAccess
      def bracketUpdate(name: String, v: Int): Unit = js.native

      @JSBracketCall
      def bracketCall(name: String)(arg: Int): Int = js.native

      @JSName(Symbols.sym)
      var symbolVar: Int = js.native
      @JSName(Symbols.sym)
      def symbolDef(): Int = js.native

      var arguments: js.Array[Any] = js.native
      @JSName("arguments") def arguments2(x: Int): Int = js.native
    }
    """
  }

  @Test
  def canAccessLegitMembers(): Unit = {
    s"""
    object Main {
      def main(): Unit = {
        val a = js.Dynamic.global.validVar
        js.Dynamic.global.validVar = 3
        val b = js.Dynamic.global.validDef()

        val c = SomeGlobalScope.validVar
        SomeGlobalScope.validVar = 3
        val d = SomeGlobalScope.validDef()

        val e = SomeGlobalScope.bracketSelect("validVar")
        SomeGlobalScope.bracketUpdate("validVar", 3)
        val f = SomeGlobalScope.bracketCall("validDef")(4)
      }
    }
    """.hasNoWarns()
  }

  @Test
  def noLoadGlobalValue(): Unit = {
    s"""
    object Main {
      def main(): Unit = {
        val g1 = js.Dynamic.global
        val g2 = SomeGlobalScope
      }
    }
    """ hasErrors
    s"""
      |newSource1.scala:41: error: Loading the global scope as a value (anywhere but as the left-hand-side of a `.`-selection) is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val g1 = js.Dynamic.global
      |                            ^
      |newSource1.scala:42: error: Loading the global scope as a value (anywhere but as the left-hand-side of a `.`-selection) is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val g2 = SomeGlobalScope
      |                 ^
    """
  }

  @Test
  def rejectInvalidJSIdentifiers(): Unit = {
    s"""
    object Main {
      def main(): Unit = {
        val a = js.Dynamic.global.`not-a-valid-identifier-var`
        js.Dynamic.global.`not-a-valid-identifier-var` = 3
        val b = js.Dynamic.global.`not-a-valid-identifier-def`()

        val c = SomeGlobalScope.`not-a-valid-identifier-var`
        SomeGlobalScope.`not-a-valid-identifier-var` = 3
        val d = SomeGlobalScope.`not-a-valid-identifier-def`()

        val e = SomeGlobalScope.bracketSelect("not-a-valid-identifier-var")
        SomeGlobalScope.bracketUpdate("not-a-valid-identifier-var", 3)
        val f = SomeGlobalScope.bracketCall("not-a-valid-identifier-def")(4)
      }
    }
    """ hasErrors
    s"""
      |newSource1.scala:41: error: Selecting a field of the global scope whose name is not a valid JavaScript identifier is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val a = js.Dynamic.global.`not-a-valid-identifier-var`
      |                           ^
      |newSource1.scala:42: error: Selecting a field of the global scope whose name is not a valid JavaScript identifier is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        js.Dynamic.global.`not-a-valid-identifier-var` = 3
      |                   ^
      |newSource1.scala:43: error: Calling a method of the global scope whose name is not a valid JavaScript identifier is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val b = js.Dynamic.global.`not-a-valid-identifier-def`()
      |                                                              ^
      |newSource1.scala:45: error: Selecting a field of the global scope whose name is not a valid JavaScript identifier is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val c = SomeGlobalScope.`not-a-valid-identifier-var`
      |                                ^
      |newSource1.scala:46: error: Selecting a field of the global scope whose name is not a valid JavaScript identifier is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        SomeGlobalScope.`not-a-valid-identifier-var` = 3
      |                                                     ^
      |newSource1.scala:47: error: Calling a method of the global scope whose name is not a valid JavaScript identifier is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val d = SomeGlobalScope.`not-a-valid-identifier-def`()
      |                                                            ^
      |newSource1.scala:49: error: Selecting a field of the global scope whose name is not a valid JavaScript identifier is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val e = SomeGlobalScope.bracketSelect("not-a-valid-identifier-var")
      |                                             ^
      |newSource1.scala:50: error: Selecting a field of the global scope whose name is not a valid JavaScript identifier is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        SomeGlobalScope.bracketUpdate("not-a-valid-identifier-var", 3)
      |                                     ^
      |newSource1.scala:51: error: Calling a method of the global scope whose name is not a valid JavaScript identifier is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val f = SomeGlobalScope.bracketCall("not-a-valid-identifier-def")(4)
      |                                                                         ^
    """
  }

  @Test
  def rejectInvalidJSIdentifiersInNestedObjectClass(): Unit = {
    """
    @js.native
    @JSGlobalScope
    object EnclosingGlobalScope extends js.Any {
      @js.native
      class `not-a-valid-JS-identifier` extends js.Object

      @js.native
      @JSName("not-a-valid-JS-identifier")
      object A extends js.Object

      @js.native
      @JSName("foo.bar")
      object B extends js.Object

      @js.native
      @JSName("")
      object C extends js.Object
    }
    """ hasErrors
    """
      |newSource1.scala:43: error: The name of a JS global variable must be a valid JS identifier (got 'not-a-valid-JS-identifier')
      |      class `not-a-valid-JS-identifier` extends js.Object
      |            ^
      |newSource1.scala:47: error: The name of a JS global variable must be a valid JS identifier (got 'not-a-valid-JS-identifier')
      |      object A extends js.Object
      |             ^
      |newSource1.scala:51: error: The name of a JS global variable must be a valid JS identifier (got 'foo.bar')
      |      object B extends js.Object
      |             ^
      |newSource1.scala:55: error: The name of a JS global variable must be a valid JS identifier (got '')
      |      object C extends js.Object
      |             ^
    """
  }

  @Test
  def rejectJSOperators(): Unit = {
    """
    object Main {
      def main(): Unit = {
        val a = js.Dynamic.global + 3.asInstanceOf[js.Dynamic]
      }
    }
    """ hasErrors
    s"""
      |newSource1.scala:41: error: type mismatch;
      | found   : scala.scalajs.js.Dynamic
      | required: String
      |        val a = js.Dynamic.global + 3.asInstanceOf[js.Dynamic]
      |                                                  ^
    """

    """
    object Main {
      def main(): Unit = {
        val a = SomeGlobalScope + 3
      }
    }
    """ hasErrors
    s"""
      |newSource1.scala:41: error: Loading the global scope as a value (anywhere but as the left-hand-side of a `.`-selection) is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val a = SomeGlobalScope + 3
      |                ^
    """
  }

  @Test
  def rejectApply(): Unit = {
    """
    object Main {
      def main(): Unit = {
        val a = js.Dynamic.global(3)
      }
    }
    """ hasErrors
    s"""
      |newSource1.scala:41: warning: method apply in object global is deprecated (since forever): The global scope cannot be called as function.
      |        val a = js.Dynamic.global(3)
      |                           ^
      |newSource1.scala:41: error: Loading the global scope as a value (anywhere but as the left-hand-side of a `.`-selection) is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val a = js.Dynamic.global(3)
      |                           ^
    """

    """
    object Main {
      def main(): Unit = {
        val a = SomeGlobalScope(3)
      }
    }
    """ hasErrors
    s"""
      |newSource1.scala:41: error: Loading the global scope as a value (anywhere but as the left-hand-side of a `.`-selection) is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val a = SomeGlobalScope(3)
      |                ^
    """
  }

  @Test
  def rejectDynamicNames(): Unit = {
    s"""
    object Main {
      def dynName: String = "foo"

      def main(): Unit = {
        val a = js.Dynamic.global.selectDynamic(dynName)
        js.Dynamic.global.updateDynamic(dynName)(3)
        val b = js.Dynamic.global.applyDynamic(dynName)(3)

        val e = SomeGlobalScope.bracketSelect(dynName)
        SomeGlobalScope.bracketUpdate(dynName, 3)
        val f = SomeGlobalScope.bracketCall(dynName)(4)

        val i = SomeGlobalScope.symbolVar
        SomeGlobalScope.symbolVar = 3
        val k = SomeGlobalScope.symbolDef()
      }
    }
    """ hasErrors
    s"""
      |newSource1.scala:43: error: Selecting a field of the global scope with a dynamic name is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val a = js.Dynamic.global.selectDynamic(dynName)
      |                                               ^
      |newSource1.scala:44: error: Selecting a field of the global scope with a dynamic name is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        js.Dynamic.global.updateDynamic(dynName)(3)
      |                                                ^
      |newSource1.scala:45: error: Calling a method of the global scope with a dynamic name is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val b = js.Dynamic.global.applyDynamic(dynName)(3)
      |                                                       ^
      |newSource1.scala:47: error: Selecting a field of the global scope with a dynamic name is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val e = SomeGlobalScope.bracketSelect(dynName)
      |                                             ^
      |newSource1.scala:48: error: Selecting a field of the global scope with a dynamic name is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        SomeGlobalScope.bracketUpdate(dynName, 3)
      |                                     ^
      |newSource1.scala:49: error: Calling a method of the global scope with a dynamic name is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val f = SomeGlobalScope.bracketCall(dynName)(4)
      |                                                    ^
      |newSource1.scala:51: error: Selecting a field of the global scope with a dynamic name is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val i = SomeGlobalScope.symbolVar
      |                                ^
      |newSource1.scala:52: error: Selecting a field of the global scope with a dynamic name is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        SomeGlobalScope.symbolVar = 3
      |                                  ^
      |newSource1.scala:53: error: Calling a method of the global scope with a dynamic name is not allowed.
      |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
      |        val k = SomeGlobalScope.symbolDef()
      |                                         ^
    """
  }

  @Test
  def rejectAllReservedIdentifiers(): Unit = {
    val reservedIdentifiers = List(
        "arguments", "break", "case", "catch", "class", "const", "continue",
        "debugger", "default", "delete", "do", "else", "enum", "export",
        "extends", "false", "finally", "for", "function", "if", "implements",
        "import", "in", "instanceof", "interface", "let", "new", "null",
        "package", "private", "protected", "public", "return", "static",
        "super", "switch", "throw", "true", "try", "typeof", "var",
        "void", "while", "with", "yield")

    for (reservedIdentifier <- reservedIdentifiers) {
      val spaces = " " * reservedIdentifier.length()

      s"""
      @js.native
      @JSGlobalScope
      object CustomGlobalScope extends js.Any {
        var `$reservedIdentifier`: Int = js.native
        @JSName("$reservedIdentifier")
        def `${reservedIdentifier}2`(x: Int): Int = js.native
      }

      object Main {
        def main(): Unit = {
          val a = js.Dynamic.global.`$reservedIdentifier`
          js.Dynamic.global.`$reservedIdentifier` = 5
          val b = js.Dynamic.global.`$reservedIdentifier`(5)

          val c = CustomGlobalScope.`$reservedIdentifier`
          CustomGlobalScope.`$reservedIdentifier` = 5
          val d = CustomGlobalScope.`${reservedIdentifier}2`(5)
        }
      }
      """ hasErrors
      s"""
        |newSource1.scala:49: error: Invalid selection in the global scope of the reserved identifier name `$reservedIdentifier`.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          val a = js.Dynamic.global.`$reservedIdentifier`
        |                             ^
        |newSource1.scala:50: error: Invalid selection in the global scope of the reserved identifier name `$reservedIdentifier`.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          js.Dynamic.global.`$reservedIdentifier` = 5
        |                     ^
        |newSource1.scala:51: error: Invalid call in the global scope of the reserved identifier name `$reservedIdentifier`.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          val b = js.Dynamic.global.`$reservedIdentifier`(5)
        |                                      $spaces^
        |newSource1.scala:53: error: Invalid selection in the global scope of the reserved identifier name `$reservedIdentifier`.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          val c = CustomGlobalScope.`$reservedIdentifier`
        |                                    ^
        |newSource1.scala:54: error: Invalid selection in the global scope of the reserved identifier name `$reservedIdentifier`.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          CustomGlobalScope.`$reservedIdentifier` = 5
        |                               $spaces^
        |newSource1.scala:55: error: Invalid call in the global scope of the reserved identifier name `$reservedIdentifier`.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          val d = CustomGlobalScope.`${reservedIdentifier}2`(5)
        |                                       $spaces^
      """
    }
  }

  @Test
  def warnAboutAwaitReservedWord_Issue4705(): Unit = {
    val reservedIdentifiers = List("await")

    for (reservedIdentifier <- reservedIdentifiers) {
      val spaces = " " * reservedIdentifier.length()

      s"""
      @js.native
      @JSGlobalScope
      object CustomGlobalScope extends js.Any {
        var `$reservedIdentifier`: Int = js.native
        @JSName("$reservedIdentifier")
        def `${reservedIdentifier}2`(x: Int): Int = js.native
      }

      object Main {
        def main(): Unit = {
          val a = js.Dynamic.global.`$reservedIdentifier`
          js.Dynamic.global.`$reservedIdentifier` = 5
          val b = js.Dynamic.global.`$reservedIdentifier`(5)

          val c = CustomGlobalScope.`$reservedIdentifier`
          CustomGlobalScope.`$reservedIdentifier` = 5
          val d = CustomGlobalScope.`${reservedIdentifier}2`(5)
        }
      }
      """ hasWarns
      s"""
        |newSource1.scala:49: warning: Selecting a field of the global scope with the name '$reservedIdentifier' is deprecated.
        |  It may produce invalid JavaScript code causing a SyntaxError in some environments.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          val a = js.Dynamic.global.`$reservedIdentifier`
        |                             ^
        |newSource1.scala:50: warning: Selecting a field of the global scope with the name '$reservedIdentifier' is deprecated.
        |  It may produce invalid JavaScript code causing a SyntaxError in some environments.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          js.Dynamic.global.`$reservedIdentifier` = 5
        |                     ^
        |newSource1.scala:51: warning: Calling a method of the global scope with the name '$reservedIdentifier' is deprecated.
        |  It may produce invalid JavaScript code causing a SyntaxError in some environments.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          val b = js.Dynamic.global.`$reservedIdentifier`(5)
        |                                      $spaces^
        |newSource1.scala:53: warning: Selecting a field of the global scope with the name '$reservedIdentifier' is deprecated.
        |  It may produce invalid JavaScript code causing a SyntaxError in some environments.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          val c = CustomGlobalScope.`$reservedIdentifier`
        |                                    ^
        |newSource1.scala:54: warning: Selecting a field of the global scope with the name '$reservedIdentifier' is deprecated.
        |  It may produce invalid JavaScript code causing a SyntaxError in some environments.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          CustomGlobalScope.`$reservedIdentifier` = 5
        |                               $spaces^
        |newSource1.scala:55: warning: Calling a method of the global scope with the name '$reservedIdentifier' is deprecated.
        |  It may produce invalid JavaScript code causing a SyntaxError in some environments.
        |  See https://www.scala-js.org/doc/interoperability/global-scope.html for further information.
        |          val d = CustomGlobalScope.`${reservedIdentifier}2`(5)
        |                                       $spaces^
      """
    }
  }

  @Test
  def noWarnAboutAwaitReservedWordIfSelectivelyDisabled(): Unit = {
    assumeTrue(scalaSupportsNoWarn)

    val reservedIdentifiers = List("await")

    for (reservedIdentifier <- reservedIdentifiers) {
      val spaces = " " * reservedIdentifier.length()

      s"""
      import scala.annotation.nowarn

      @js.native
      @JSGlobalScope
      object CustomGlobalScope extends js.Any {
        var `$reservedIdentifier`: Int = js.native
        @JSName("$reservedIdentifier")
        def `${reservedIdentifier}2`(x: Int): Int = js.native
      }

      object Main {
        @nowarn("cat=deprecation")
        def main(): Unit = {
          val a = js.Dynamic.global.`$reservedIdentifier`
          js.Dynamic.global.`$reservedIdentifier` = 5
          val b = js.Dynamic.global.`$reservedIdentifier`(5)

          val c = CustomGlobalScope.`$reservedIdentifier`
          CustomGlobalScope.`$reservedIdentifier` = 5
          val d = CustomGlobalScope.`${reservedIdentifier}2`(5)
        }
      }
      """.hasNoWarns()
    }
  }

  @Test
  def rejectAssignmentToGlobalThis(): Unit = {
    """
    import scala.scalajs.js
    import scala.scalajs.js.annotation._

    object Main {
      def main(): Unit = {
        js.Dynamic.global.`this` = 0
        GlobalScope.globalThis = 0
      }
    }

    @js.native
    @JSGlobalScope
    object GlobalScope extends js.Any {
      @JSName("this")
      var globalThis: Any = js.native
    }
    """ hasErrors
    s"""
      |newSource1.scala:44: error: Illegal assignment to global this.
      |        js.Dynamic.global.`this` = 0
      |                   ^
      |newSource1.scala:45: error: Illegal assignment to global this.
      |        GlobalScope.globalThis = 0
      |                               ^
    """
  }

}
