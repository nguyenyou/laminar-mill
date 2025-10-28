package io.github.nguyenyou.floatingUI.middleware

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.Utils

/** Tests for Inline middleware helper functions.
  *
  * Ported from floating-ui/packages/core/test/middleware/inline.test.ts
  *
  * These tests verify the getRectsByLine function which groups client rects by line (based on y-coordinate overlap).
  */
class InlineMiddlewareTest extends AnyFunSpec with Matchers {

  describe("getRectsByLine") {

    it("single line") {
      val input = Seq(
        Utils.rectToClientRect(Rect(x = 0, y = 0, width = 10, height = 10)),
        Utils.rectToClientRect(Rect(x = 10, y = 0, width = 10, height = 10)),
        Utils.rectToClientRect(Rect(x = 20, y = 0, width = 10, height = 10))
      )

      val result = InlineMiddleware.getRectsByLine(input)

      result `shouldBe` Seq(
        Utils.rectToClientRect(Rect(x = 0, y = 0, width = 30, height = 10))
      )
    }

    it("multiple lines") {
      val input = Seq(
        Utils.rectToClientRect(Rect(x = 0, y = 0, width = 10, height = 10)),
        Utils.rectToClientRect(Rect(x = 10, y = 0, width = 10, height = 10)),
        Utils.rectToClientRect(Rect(x = 20, y = 0, width = 10, height = 10)),
        Utils.rectToClientRect(Rect(x = 20, y = 10, width = 100, height = 10))
      )

      val result = InlineMiddleware.getRectsByLine(input)

      result `shouldBe` Seq(
        Utils.rectToClientRect(Rect(x = 0, y = 0, width = 30, height = 10)),
        Utils.rectToClientRect(Rect(x = 20, y = 10, width = 100, height = 10))
      )
    }

    it("multiple lines, different heights and y coords") {
      val input = Seq(
        Utils.rectToClientRect(Rect(x = 0, y = 0, width = 10, height = 10)),
        Utils.rectToClientRect(Rect(x = 10, y = 3, width = 10, height = 8)),
        Utils.rectToClientRect(Rect(x = 20, y = 1, width = 10, height = 5))
      )

      val result = InlineMiddleware.getRectsByLine(input)

      result `shouldBe` Seq(
        Utils.rectToClientRect(Rect(x = 0, y = 0, width = 30, height = 11))
      )
    }

    it("multiple lines, different heights and y coords, with a gap") {
      val input = Seq(
        Utils.rectToClientRect(Rect(x = 0, y = 0, width = 10, height = 10)),
        Utils.rectToClientRect(Rect(x = 10, y = 3, width = 10, height = 8)),
        Utils.rectToClientRect(Rect(x = 20, y = 1, width = 10, height = 5)),
        Utils.rectToClientRect(Rect(x = 20, y = 20, width = 10, height = 5))
      )

      val result = InlineMiddleware.getRectsByLine(input)

      result `shouldBe` Seq(
        Utils.rectToClientRect(Rect(x = 0, y = 0, width = 30, height = 11)),
        Utils.rectToClientRect(Rect(x = 20, y = 20, width = 10, height = 5))
      )
    }
  }
}

