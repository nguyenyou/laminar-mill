package io.github.nguyenyou.floatingUI.middleware

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Types.*

/** Tests for AutoPlacement middleware helper functions.
  *
  * Ported from floating-ui/packages/core/test/middleware/autoPlacement.test.ts
  *
  * These tests verify the getPlacementList function which filters and orders placements based on alignment and auto-alignment
  * settings.
  */
class AutoPlacementMiddlewareTest extends AnyFunSpec with Matchers {

  describe("getPlacementList") {

    it("base placement") {
      val result = AutoPlacementMiddleware.getPlacementList(
        alignment = None,
        autoAlignment = false,
        allowedPlacements = Seq(
          "top",
          "bottom",
          "left",
          "right",
          "top-start",
          "right-end"
        )
      )
      result `shouldBe` Seq("top", "bottom", "left", "right")
    }

    it("start alignment without auto alignment") {
      val result = AutoPlacementMiddleware.getPlacementList(
        alignment = Some("start"),
        autoAlignment = false,
        allowedPlacements = Seq(
          "top",
          "bottom",
          "left",
          "right",
          "top-start",
          "right-end",
          "left-start"
        )
      )
      result `shouldBe` Seq("top-start", "left-start")
    }

    it("start alignment with auto alignment") {
      val result = AutoPlacementMiddleware.getPlacementList(
        alignment = Some("start"),
        autoAlignment = true,
        allowedPlacements = Seq(
          "top",
          "bottom",
          "left",
          "right",
          "top-start",
          "right-end",
          "left-start"
        )
      )
      result `shouldBe` Seq("top-start", "left-start", "right-end")
    }

    it("end alignment without auto alignment") {
      val result = AutoPlacementMiddleware.getPlacementList(
        alignment = Some("end"),
        autoAlignment = false,
        allowedPlacements = Seq(
          "top",
          "bottom",
          "left",
          "right",
          "top-start",
          "right-end",
          "left-start"
        )
      )
      result `shouldBe` Seq("right-end")
    }

    it("end alignment with auto alignment") {
      val result = AutoPlacementMiddleware.getPlacementList(
        alignment = Some("end"),
        autoAlignment = true,
        allowedPlacements = Seq(
          "top",
          "bottom",
          "left",
          "right",
          "top-start",
          "right-end",
          "left-start"
        )
      )
      result `shouldBe` Seq("right-end", "top-start", "left-start")
    }
  }
}

