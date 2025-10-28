package io.github.nguyenyou.floatingUI

import org.scalajs.dom
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.FloatingUI

/** Tests for core computePosition functionality.
  *
  * IMPORTANT: These tests run in jsdom, which has NO layout engine.
  *
  * What jsdom CANNOT do:
  *   - Calculate element positions or dimensions (getBoundingClientRect always returns zeros)
  *   - Perform layout calculations (offsetWidth, offsetHeight, clientWidth, etc. are 0 or undefined)
  *   - Compute viewport dimensions that affect layout
  *
  * What these tests DO validate:
  *   - computePosition executes without crashing
  *   - Return values have correct structure and types
  *   - Placement parameter is respected in the result
  *   - Strategy parameter is respected in the result
  *   - MiddlewareData structure is populated
  *
  * What these tests DO NOT validate:
  *   - Actual x/y coordinate calculations (requires real browser)
  *   - Positioning relative to reference element
  *   - Layout-dependent behavior
  *
  * For real positioning tests, use browser-based integration tests (Playwright/Puppeteer).
  *
  * References:
  *   - jsdom issue #135: https://github.com/jsdom/jsdom/issues/135
  *   - jsdom issue #1590: https://github.com/jsdom/jsdom/issues/1590
  */
class ComputePositionTest extends AnyFunSpec with Matchers {

  describe("computePosition") {

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

        // Validates structure and API contract, NOT positioning behavior (jsdom has no layout engine)
        result should not be null
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]
        result.placement shouldBe "bottom" // default placement
        result.strategy shouldBe "absolute" // default strategy
        result.middlewareData should not be null

        // In jsdom, x and y will be 0 because getBoundingClientRect returns zeros
        // In a real browser, these would be calculated based on reference element position
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
        // jsdom has no layout engine, so actual positioning is not tested

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
