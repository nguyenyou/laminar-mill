package io.github.nguyenyou.floatingUI

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.ComputeCoordsFromPlacement

/** Tests for computeCoordsFromPlacement function.
  *
  * Ported from floating-ui/packages/core/test/computeCoordsFromPlacement.test.ts
  *
  * These tests verify that the coordinate calculation for different placements is correct. All tests use the same reference and
  * floating rectangles and verify the computed x, y coordinates for each placement.
  */
class ComputeCoordsFromPlacementTest extends AnyFunSpec with Matchers {

  val reference = Rect(x = 0, y = 0, width = 100, height = 100)
  val floating = Rect(x = 0, y = 0, width = 50, height = 50)
  val rects = ElementRects(reference = reference, floating = floating)

  describe("computeCoordsFromPlacement") {

    it("bottom") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "bottom", rtl = false)
      result.x `shouldBe` 25.0
      result.y `shouldBe` 100.0
    }

    it("bottom-start") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "bottom-start", rtl = false)
      result.x `shouldBe` 0.0
      result.y `shouldBe` 100.0
    }

    it("bottom-end") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "bottom-end", rtl = false)
      result.x `shouldBe` 50.0
      result.y `shouldBe` 100.0
    }

    it("top") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "top", rtl = false)
      result.x `shouldBe` 25.0
      result.y `shouldBe` -50.0
    }

    it("top-start") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "top-start", rtl = false)
      result.x `shouldBe` 0.0
      result.y `shouldBe` -50.0
    }

    it("top-end") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "top-end", rtl = false)
      result.x `shouldBe` 50.0
      result.y `shouldBe` -50.0
    }

    it("right") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "right", rtl = false)
      result.x `shouldBe` 100.0
      result.y `shouldBe` 25.0
    }

    it("right-start") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "right-start", rtl = false)
      result.x `shouldBe` 100.0
      result.y `shouldBe` 0.0
    }

    it("right-end") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "right-end", rtl = false)
      result.x `shouldBe` 100.0
      result.y `shouldBe` 50.0
    }

    it("left") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "left", rtl = false)
      result.x `shouldBe` -50.0
      result.y `shouldBe` 25.0
    }

    it("left-start") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "left-start", rtl = false)
      result.x `shouldBe` -50.0
      result.y `shouldBe` 0.0
    }

    it("left-end") {
      val result = ComputeCoordsFromPlacement.computeCoordsFromPlacement(rects, "left-end", rtl = false)
      result.x `shouldBe` -50.0
      result.y `shouldBe` 50.0
    }
  }
}

