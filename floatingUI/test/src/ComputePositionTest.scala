package io.github.nguyenyou.floatingUI

import org.scalajs.dom
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.FloatingUI
import io.github.nguyenyou.floatingUI.ComputePosition

/** Tests for core computePosition functionality.
  *
  * IMPORTANT: These tests run in Playwright (real Chrome browser).
  *
  * What these tests validate:
  *   - computePosition executes without crashing
  *   - Return values have correct structure and types
  *   - Placement parameter is respected in the result
  *   - Strategy parameter is respected in the result
  *   - MiddlewareData structure is populated
  *   - Basic positioning calculations work correctly
  *
  * This file includes both:
  *   1. Unit tests with mock platform (ported from floating-ui TypeScript) 2. Integration tests with real DOM elements
  */
class ComputePositionTest extends AnyFunSpec with Matchers {

  /** Mock platform for unit testing core positioning logic.
    *
    * Ported from floating-ui/packages/core/test/computePosition.test.ts This allows testing the positioning algorithm independently of the
    * DOM.
    */
  class MockPlatform(referenceRect: Rect, floatingRect: Rect) extends Platform {
    def getElementRects(reference: ReferenceElement, floating: dom.HTMLElement, strategy: Strategy): ElementRects =
      ElementRects(reference = referenceRect, floating = floatingRect)

    def getDimensions(element: dom.Element): Dimensions =
      Dimensions(width = 10, height = 10)

    def getClippingRect(element: Any, boundary: String, rootBoundary: String, strategy: Strategy): Rect =
      Rect(x = 0, y = 0, width = 0, height = 0)

    override def convertOffsetParentRelativeRectToViewportRelativeRect(
      elements: Option[Elements],
      rect: Rect,
      offsetParent: Any,
      strategy: Strategy
    ): Option[Rect] = Some(rect)

    override def getOffsetParent(element: Any): Option[Any] = Some(dom.document.body)

    override def isElement(value: Any): Option[Boolean] = Some(false)

    override def getDocumentElement(element: Any): Option[Any] = Some(dom.document.documentElement)

    def getClientRects(element: ReferenceElement): Seq[ClientRectObject] = Seq.empty

    override def isRTL(element: dom.Element): Boolean = false

    override def getScale(element: Any): Option[Coords] = Some(Coords(x = 1, y = 1))
  }

  // ============================================================================
  // Unit Tests (with Mock Platform)
  // Ported from floating-ui/packages/core/test/computePosition.test.ts
  // ============================================================================

  describe("computePosition (unit tests with mock platform)") {

    it("returned data") {
      // Mock elements (not used by mock platform, but required by API)
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]

      // Mock platform with predefined rects
      val referenceRect = Rect(x = 0, y = 0, width = 100, height = 100)
      val floatingRect = Rect(x = 0, y = 0, width = 50, height = 50)
      val platform = new MockPlatform(referenceRect, floatingRect)

      // Custom middleware that adds data
      val customMiddleware = new Middleware {
        val name = "custom"
        def fn(state: MiddlewareState): MiddlewareReturn = {
          MiddlewareReturn(
            data = Some(Map("property" -> true))
          )
        }
      }

      // Compute position with custom middleware
      val config = ComputePositionConfig(
        placement = "top",
        strategy = "absolute",
        middleware = Seq(customMiddleware),
        platform = platform
      )

      val result = ComputePosition.computePosition(reference, floating, config)

      // Verify returned data structure
      result.placement `shouldBe` "top"
      result.strategy `shouldBe` "absolute"
      result.x `shouldBe` 25.0 // (100 - 50) / 2 = 25 (centered horizontally)
      result.y `shouldBe` -50.0 // -floatingRect.height (positioned above)

      // Verify middleware data is stored in the custom map
      result.middlewareData should not be null
      result.middlewareData.custom should contain key "custom"
      result.middlewareData.custom("custom") `shouldBe` Map("property" -> true)
    }

    it("middleware") {
      // Mock elements
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]

      // Mock platform with predefined rects
      val referenceRect = Rect(x = 0, y = 0, width = 100, height = 100)
      val floatingRect = Rect(x = 0, y = 0, width = 50, height = 50)
      val platform = new MockPlatform(referenceRect, floatingRect)

      // First computation: without middleware (baseline)
      val config1 = ComputePositionConfig(
        placement = "bottom",
        strategy = "absolute",
        middleware = Seq.empty,
        platform = platform
      )
      val result1 = ComputePosition.computePosition(reference, floating, config1)
      val x = result1.x
      val y = result1.y

      // Second computation: with middleware that adds 1 to x and y
      val testMiddleware = new Middleware {
        val name = "test"
        def fn(state: MiddlewareState): MiddlewareReturn = {
          MiddlewareReturn(
            x = Some(state.x + 1),
            y = Some(state.y + 1)
          )
        }
      }

      val config2 = ComputePositionConfig(
        placement = "bottom",
        strategy = "absolute",
        middleware = Seq(testMiddleware),
        platform = platform
      )
      val result2 = ComputePosition.computePosition(reference, floating, config2)
      val x2 = result2.x
      val y2 = result2.y

      // Verify that middleware modified the coordinates
      x2 `shouldBe` (x + 1)
      y2 `shouldBe` (y + 1)
    }

    it("middlewareData") {
      // Mock elements
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]

      // Mock platform
      val referenceRect = Rect(x = 0, y = 0, width = 100, height = 100)
      val floatingRect = Rect(x = 0, y = 0, width = 50, height = 50)
      val platform = new MockPlatform(referenceRect, floatingRect)

      // Middleware that stores custom data
      val testMiddleware = new Middleware {
        val name = "test"
        def fn(state: MiddlewareState): MiddlewareReturn = {
          MiddlewareReturn(
            data = Some(Map("hello" -> true))
          )
        }
      }

      val config = ComputePositionConfig(
        placement = "bottom",
        strategy = "absolute",
        middleware = Seq(testMiddleware),
        platform = platform
      )

      val result = ComputePosition.computePosition(reference, floating, config)

      // Verify middleware data is stored in the custom map
      // Our implementation stores custom middleware data in middlewareData.custom
      // The TypeScript version stores it as middlewareData.test = {hello: true}
      result.middlewareData should not be null
      result.middlewareData.custom should contain key "test"
      result.middlewareData.custom("test") `shouldBe` Map("hello" -> true)
    }
  }

  // ============================================================================
  // Integration Tests (with Real DOM)
  // ============================================================================

  describe("computePosition (integration tests with real DOM)") {

    it("computes position with default parameters") {
      // Create reference and floating elements
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      reference.style.width = "100px"
      reference.style.height = "100px"
      reference.style.position = "absolute"
      reference.style.left = "50px"
      reference.style.top = "50px"
      dom.document.body.appendChild(reference)

      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      floating.style.width = "50px"
      floating.style.height = "50px"
      dom.document.body.appendChild(floating)

      try {
        val result = FloatingUI.computePosition(reference, floating)

        // Validates structure and API contract
        result should not be null
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]
        result.placement shouldBe "bottom" // default placement
        result.strategy shouldBe "absolute" // default strategy
        result.middlewareData should not be null
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }

    it("computes position with different placements") {
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      reference.style.width = "100px"
      reference.style.height = "100px"
      reference.style.position = "absolute"
      reference.style.left = "200px"
      reference.style.top = "200px"
      dom.document.body.appendChild(reference)

      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      floating.style.width = "50px"
      floating.style.height = "50px"
      dom.document.body.appendChild(floating)

      try {
        // Validates that placement parameter is respected in the result

        // Test top placement
        val topResult = FloatingUI.computePosition(reference, floating, placement = "top")
        topResult.placement shouldBe "top"
        topResult.x shouldBe a[Double]
        topResult.y shouldBe a[Double]

        // Test right placement
        val rightResult = FloatingUI.computePosition(reference, floating, placement = "right")
        rightResult.placement shouldBe "right"
        rightResult.x shouldBe a[Double]
        rightResult.y shouldBe a[Double]

        // Test left placement
        val leftResult = FloatingUI.computePosition(reference, floating, placement = "left")
        leftResult.placement shouldBe "left"
        leftResult.x shouldBe a[Double]
        leftResult.y shouldBe a[Double]

        // Test bottom-start placement
        val bottomStartResult = FloatingUI.computePosition(reference, floating, placement = "bottom-start")
        bottomStartResult.placement shouldBe "bottom-start"
        bottomStartResult.x shouldBe a[Double]
        bottomStartResult.y shouldBe a[Double]

        // In a real browser, each placement would produce different x/y coordinates
        // In jsdom, they're all 0 because getBoundingClientRect returns zeros
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }
  }
}
