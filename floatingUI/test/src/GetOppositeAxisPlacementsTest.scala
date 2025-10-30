package io.github.nguyenyou.floatingUI

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Utils.*

/** Tests for getOppositeAxisPlacements function.
  *
  * Ported from: floating-ui/packages/utils/test/getOppositeAxisPlacements.test.ts
  *
  * What these tests validate:
  *   - getOppositeAxisPlacements correctly returns placements on the opposite axis
  *   - Handles side placements (top, bottom, left, right)
  *   - Handles aligned placements (top-start, bottom-end, etc.)
  *   - Respects flipAlignment parameter to include opposite alignment variants
  *   - Respects direction parameter (start/end) to determine order
  *   - Respects rtl parameter for right-to-left language support
  *
  * The function is used for fallbackAxisSideDirection in flip middleware to determine
  * alternative placements when the primary placement doesn't fit.
  */
class GetOppositeAxisPlacementsTest extends AnyFunSpec with Matchers {

  describe("side") {

    it("top") {
      getOppositeAxisPlacements("top", flipAlignment = true, direction = "start") shouldBe Seq(
        "left",
        "right"
      )
      getOppositeAxisPlacements("top", flipAlignment = true, direction = "end") shouldBe Seq(
        "right",
        "left"
      )
    }

    it("bottom") {
      getOppositeAxisPlacements("bottom", flipAlignment = true, direction = "start") shouldBe Seq(
        "left",
        "right"
      )
      getOppositeAxisPlacements("bottom", flipAlignment = true, direction = "end") shouldBe Seq(
        "right",
        "left"
      )
    }

    it("left") {
      getOppositeAxisPlacements("left", flipAlignment = true, direction = "start") shouldBe Seq(
        "top",
        "bottom"
      )
      getOppositeAxisPlacements("left", flipAlignment = true, direction = "end") shouldBe Seq(
        "bottom",
        "top"
      )
    }

    it("right") {
      getOppositeAxisPlacements("right", flipAlignment = true, direction = "start") shouldBe Seq(
        "top",
        "bottom"
      )
      getOppositeAxisPlacements("right", flipAlignment = true, direction = "end") shouldBe Seq(
        "bottom",
        "top"
      )
    }
  }

  describe("start alignment") {

    it("top-start") {
      getOppositeAxisPlacements("top-start", flipAlignment = false, direction = "start") shouldBe Seq(
        "left-start",
        "right-start"
      )
      getOppositeAxisPlacements("top-start", flipAlignment = false, direction = "end") shouldBe Seq(
        "right-start",
        "left-start"
      )
      getOppositeAxisPlacements("top-start", flipAlignment = true, direction = "start") shouldBe Seq(
        "left-start",
        "right-start",
        "left-end",
        "right-end"
      )
      getOppositeAxisPlacements("top-start", flipAlignment = true, direction = "end") shouldBe Seq(
        "right-start",
        "left-start",
        "right-end",
        "left-end"
      )
    }

    it("bottom-start") {
      getOppositeAxisPlacements("bottom-start", flipAlignment = false, direction = "start") shouldBe Seq(
        "left-start",
        "right-start"
      )
      getOppositeAxisPlacements("bottom-start", flipAlignment = false, direction = "end") shouldBe Seq(
        "right-start",
        "left-start"
      )
      getOppositeAxisPlacements("bottom-start", flipAlignment = true, direction = "start") shouldBe Seq(
        "left-start",
        "right-start",
        "left-end",
        "right-end"
      )
      getOppositeAxisPlacements("bottom-start", flipAlignment = true, direction = "end") shouldBe Seq(
        "right-start",
        "left-start",
        "right-end",
        "left-end"
      )
    }

    it("left-start") {
      getOppositeAxisPlacements("left-start", flipAlignment = false, direction = "start") shouldBe Seq(
        "top-start",
        "bottom-start"
      )
      getOppositeAxisPlacements("left-start", flipAlignment = false, direction = "end") shouldBe Seq(
        "bottom-start",
        "top-start"
      )
      getOppositeAxisPlacements("left-start", flipAlignment = true, direction = "start") shouldBe Seq(
        "top-start",
        "bottom-start",
        "top-end",
        "bottom-end"
      )
      getOppositeAxisPlacements("left-start", flipAlignment = true, direction = "end") shouldBe Seq(
        "bottom-start",
        "top-start",
        "bottom-end",
        "top-end"
      )
    }

    it("right-start") {
      getOppositeAxisPlacements("right-start", flipAlignment = false, direction = "start") shouldBe Seq(
        "top-start",
        "bottom-start"
      )
      getOppositeAxisPlacements("right-start", flipAlignment = false, direction = "end") shouldBe Seq(
        "bottom-start",
        "top-start"
      )
      getOppositeAxisPlacements("right-start", flipAlignment = true, direction = "start") shouldBe Seq(
        "top-start",
        "bottom-start",
        "top-end",
        "bottom-end"
      )
      getOppositeAxisPlacements("right-start", flipAlignment = true, direction = "end") shouldBe Seq(
        "bottom-start",
        "top-start",
        "bottom-end",
        "top-end"
      )
    }
  }

  describe("end alignment") {

    it("top-end") {
      getOppositeAxisPlacements("top-end", flipAlignment = false, direction = "start") shouldBe Seq(
        "left-end",
        "right-end"
      )
      getOppositeAxisPlacements("top-end", flipAlignment = false, direction = "end") shouldBe Seq(
        "right-end",
        "left-end"
      )
      getOppositeAxisPlacements("top-end", flipAlignment = true, direction = "start") shouldBe Seq(
        "left-end",
        "right-end",
        "left-start",
        "right-start"
      )
      getOppositeAxisPlacements("top-end", flipAlignment = true, direction = "end") shouldBe Seq(
        "right-end",
        "left-end",
        "right-start",
        "left-start"
      )
    }

    it("bottom-end") {
      getOppositeAxisPlacements("bottom-end", flipAlignment = false, direction = "start") shouldBe Seq(
        "left-end",
        "right-end"
      )
      getOppositeAxisPlacements("bottom-end", flipAlignment = false, direction = "end") shouldBe Seq(
        "right-end",
        "left-end"
      )
      getOppositeAxisPlacements("bottom-end", flipAlignment = true, direction = "start") shouldBe Seq(
        "left-end",
        "right-end",
        "left-start",
        "right-start"
      )
      getOppositeAxisPlacements("bottom-end", flipAlignment = true, direction = "end") shouldBe Seq(
        "right-end",
        "left-end",
        "right-start",
        "left-start"
      )
    }

    it("left-end") {
      getOppositeAxisPlacements("left-end", flipAlignment = false, direction = "start") shouldBe Seq(
        "top-end",
        "bottom-end"
      )
      getOppositeAxisPlacements("left-end", flipAlignment = false, direction = "end") shouldBe Seq(
        "bottom-end",
        "top-end"
      )
      // Note: The TypeScript test has a bug here - it tests "left-start" instead of "left-end"
      // We'll test "left-end" as the test name suggests
      getOppositeAxisPlacements("left-end", flipAlignment = true, direction = "start") shouldBe Seq(
        "top-end",
        "bottom-end",
        "top-start",
        "bottom-start"
      )
      getOppositeAxisPlacements("left-end", flipAlignment = true, direction = "end") shouldBe Seq(
        "bottom-end",
        "top-end",
        "bottom-start",
        "top-start"
      )
    }

    it("right-end") {
      getOppositeAxisPlacements("right-end", flipAlignment = false, direction = "start") shouldBe Seq(
        "top-end",
        "bottom-end"
      )
      getOppositeAxisPlacements("right-end", flipAlignment = false, direction = "end") shouldBe Seq(
        "bottom-end",
        "top-end"
      )
      getOppositeAxisPlacements("right-end", flipAlignment = true, direction = "start") shouldBe Seq(
        "top-end",
        "bottom-end",
        "top-start",
        "bottom-start"
      )
      getOppositeAxisPlacements("right-end", flipAlignment = true, direction = "end") shouldBe Seq(
        "bottom-end",
        "top-end",
        "bottom-start",
        "top-start"
      )
    }
  }

  describe("rtl") {

    it("top") {
      getOppositeAxisPlacements("top", flipAlignment = true, direction = "start", rtl = true) shouldBe Seq(
        "right",
        "left"
      )
      getOppositeAxisPlacements("top", flipAlignment = true, direction = "end", rtl = true) shouldBe Seq(
        "left",
        "right"
      )
    }

    it("bottom") {
      getOppositeAxisPlacements("bottom", flipAlignment = true, direction = "start", rtl = true) shouldBe Seq(
        "right",
        "left"
      )
      getOppositeAxisPlacements("bottom", flipAlignment = true, direction = "end", rtl = true) shouldBe Seq(
        "left",
        "right"
      )
    }

    it("left") {
      getOppositeAxisPlacements("left", flipAlignment = true, direction = "start", rtl = true) shouldBe Seq(
        "top",
        "bottom"
      )
      getOppositeAxisPlacements("left", flipAlignment = true, direction = "end", rtl = true) shouldBe Seq(
        "bottom",
        "top"
      )
    }

    it("right") {
      getOppositeAxisPlacements("right", flipAlignment = true, direction = "start", rtl = true) shouldBe Seq(
        "top",
        "bottom"
      )
      getOppositeAxisPlacements("right", flipAlignment = true, direction = "end", rtl = true) shouldBe Seq(
        "bottom",
        "top"
      )
    }
  }
}

