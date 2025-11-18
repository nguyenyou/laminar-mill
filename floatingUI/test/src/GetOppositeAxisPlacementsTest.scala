package io.github.nguyenyou.floatingUI

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Utils.*
import io.github.nguyenyou.floatingUI.Types.{FallbackAxisSideDirection, Placement}
import io.github.nguyenyou.floatingUI.Types.Placement.*

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
  * The function is used for fallbackAxisSideDirection in flip middleware to determine alternative placements when the primary placement
  * doesn't fit.
  */
class GetOppositeAxisPlacementsTest extends AnyFunSpec with Matchers {

  describe("side") {

    it("top") {
      getOppositeAxisPlacements(Top, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        Left,
        Right
      )
      getOppositeAxisPlacements(Top, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        Right,
        Left
      )
    }

    it("bottom") {
      getOppositeAxisPlacements(Bottom, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        Left,
        Right
      )
      getOppositeAxisPlacements(Bottom, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        Right,
        Left
      )
    }

    it("left") {
      getOppositeAxisPlacements(Left, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        Top,
        Bottom
      )
      getOppositeAxisPlacements(Left, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        Bottom,
        Top
      )
    }

    it("right") {
      getOppositeAxisPlacements(Right, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        Top,
        Bottom
      )
      getOppositeAxisPlacements(Right, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        Bottom,
        Top
      )
    }
  }

  describe("start alignment") {

    it("top-start") {
      getOppositeAxisPlacements(TopStart, flipAlignment = false, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        LeftStart,
        RightStart
      )
      getOppositeAxisPlacements(TopStart, flipAlignment = false, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        RightStart,
        LeftStart
      )
      getOppositeAxisPlacements(TopStart, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        LeftStart,
        RightStart,
        LeftEnd,
        RightEnd
      )
      getOppositeAxisPlacements(TopStart, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        RightStart,
        LeftStart,
        RightEnd,
        LeftEnd
      )
    }

    it("bottom-start") {
      getOppositeAxisPlacements(BottomStart, flipAlignment = false, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        LeftStart,
        RightStart
      )
      getOppositeAxisPlacements(BottomStart, flipAlignment = false, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        RightStart,
        LeftStart
      )
      getOppositeAxisPlacements(BottomStart, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        LeftStart,
        RightStart,
        LeftEnd,
        RightEnd
      )
      getOppositeAxisPlacements(BottomStart, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        RightStart,
        LeftStart,
        RightEnd,
        LeftEnd
      )
    }

    it("left-start") {
      getOppositeAxisPlacements(LeftStart, flipAlignment = false, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        TopStart,
        BottomStart
      )
      getOppositeAxisPlacements(LeftStart, flipAlignment = false, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        BottomStart,
        TopStart
      )
      getOppositeAxisPlacements(LeftStart, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        TopStart,
        BottomStart,
        TopEnd,
        BottomEnd
      )
      getOppositeAxisPlacements(LeftStart, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        BottomStart,
        TopStart,
        BottomEnd,
        TopEnd
      )
    }

    it("right-start") {
      getOppositeAxisPlacements(RightStart, flipAlignment = false, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        TopStart,
        BottomStart
      )
      getOppositeAxisPlacements(RightStart, flipAlignment = false, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        BottomStart,
        TopStart
      )
      getOppositeAxisPlacements(RightStart, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        TopStart,
        BottomStart,
        TopEnd,
        BottomEnd
      )
      getOppositeAxisPlacements(RightStart, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        BottomStart,
        TopStart,
        BottomEnd,
        TopEnd
      )
    }
  }

  describe("end alignment") {

    it("top-end") {
      getOppositeAxisPlacements(TopEnd, flipAlignment = false, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        LeftEnd,
        RightEnd
      )
      getOppositeAxisPlacements(TopEnd, flipAlignment = false, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        RightEnd,
        LeftEnd
      )
      getOppositeAxisPlacements(TopEnd, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        LeftEnd,
        RightEnd,
        LeftStart,
        RightStart
      )
      getOppositeAxisPlacements(TopEnd, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        RightEnd,
        LeftEnd,
        RightStart,
        LeftStart
      )
    }

    it("bottom-end") {
      getOppositeAxisPlacements(BottomEnd, flipAlignment = false, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        LeftEnd,
        RightEnd
      )
      getOppositeAxisPlacements(BottomEnd, flipAlignment = false, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        RightEnd,
        LeftEnd
      )
      getOppositeAxisPlacements(BottomEnd, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        LeftEnd,
        RightEnd,
        LeftStart,
        RightStart
      )
      getOppositeAxisPlacements(BottomEnd, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        RightEnd,
        LeftEnd,
        RightStart,
        LeftStart
      )
    }

    it("left-end") {
      getOppositeAxisPlacements(LeftEnd, flipAlignment = false, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        TopEnd,
        BottomEnd
      )
      getOppositeAxisPlacements(LeftEnd, flipAlignment = false, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        BottomEnd,
        TopEnd
      )
      // Note: The TypeScript test has a bug here - it tests LeftStart instead of LeftEnd
      // We'll test LeftEnd as the test name suggests
      getOppositeAxisPlacements(LeftEnd, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        TopEnd,
        BottomEnd,
        TopStart,
        BottomStart
      )
      getOppositeAxisPlacements(LeftEnd, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        BottomEnd,
        TopEnd,
        BottomStart,
        TopStart
      )
    }

    it("right-end") {
      getOppositeAxisPlacements(RightEnd, flipAlignment = false, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        TopEnd,
        BottomEnd
      )
      getOppositeAxisPlacements(RightEnd, flipAlignment = false, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        BottomEnd,
        TopEnd
      )
      getOppositeAxisPlacements(RightEnd, flipAlignment = true, direction = FallbackAxisSideDirection.Start) shouldBe Seq(
        TopEnd,
        BottomEnd,
        TopStart,
        BottomStart
      )
      getOppositeAxisPlacements(RightEnd, flipAlignment = true, direction = FallbackAxisSideDirection.End) shouldBe Seq(
        BottomEnd,
        TopEnd,
        BottomStart,
        TopStart
      )
    }
  }

  describe("rtl") {

    it("top") {
      getOppositeAxisPlacements(Top, flipAlignment = true, direction = FallbackAxisSideDirection.Start, rtl = true) shouldBe Seq(
        Right,
        Left
      )
      getOppositeAxisPlacements(Top, flipAlignment = true, direction = FallbackAxisSideDirection.End, rtl = true) shouldBe Seq(
        Left,
        Right
      )
    }

    it("bottom") {
      getOppositeAxisPlacements(Bottom, flipAlignment = true, direction = FallbackAxisSideDirection.Start, rtl = true) shouldBe Seq(
        Right,
        Left
      )
      getOppositeAxisPlacements(Bottom, flipAlignment = true, direction = FallbackAxisSideDirection.End, rtl = true) shouldBe Seq(
        Left,
        Right
      )
    }

    it("left") {
      getOppositeAxisPlacements(Left, flipAlignment = true, direction = FallbackAxisSideDirection.Start, rtl = true) shouldBe Seq(
        Top,
        Bottom
      )
      getOppositeAxisPlacements(Left, flipAlignment = true, direction = FallbackAxisSideDirection.End, rtl = true) shouldBe Seq(
        Bottom,
        Top
      )
    }

    it("right") {
      getOppositeAxisPlacements(Right, flipAlignment = true, direction = FallbackAxisSideDirection.Start, rtl = true) shouldBe Seq(
        Top,
        Bottom
      )
      getOppositeAxisPlacements(Right, flipAlignment = true, direction = FallbackAxisSideDirection.End, rtl = true) shouldBe Seq(
        Bottom,
        Top
      )
    }
  }
}
