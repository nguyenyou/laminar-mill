package io.github.nguyenyou.floatingUI

import org.scalajs.dom
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.FloatingUI
import io.github.nguyenyou.floatingUI.middleware.*

/** Tests for Floating UI middleware functions.
  *
  * IMPORTANT: These tests run in jsdom, which has NO layout engine.
  *
  * What jsdom CANNOT do:
  *   - Calculate element positions or dimensions (getBoundingClientRect always returns zeros)
  *   - Perform layout calculations (offsetWidth, offsetHeight, clientWidth, etc. are 0 or undefined)
  *   - Compute viewport dimensions that affect layout
  *
  * What these tests DO validate:
  *   - Middleware functions execute without crashing
  *   - Middleware data is populated in the result
  *   - Return values have correct structure and types
  *   - Middleware can be composed together
  *   - API contracts are respected
  *
  * What these tests DO NOT validate:
  *   - Actual positioning effects of middleware (requires real browser)
  *   - Middleware calculations based on element dimensions
  *   - Layout-dependent behavior (overflow detection, flipping, shifting, etc.)
  *
  * For real middleware behavior tests, use browser-based integration tests (Playwright/Puppeteer).
  *
  * References:
  *   - jsdom issue #135: https://github.com/jsdom/jsdom/issues/135
  *   - jsdom issue #1590: https://github.com/jsdom/jsdom/issues/1590
  */
class MiddlewareTest extends AnyFunSpec with Matchers {

  describe("offset middleware") {

    it("applies offset to floating element position") {
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      reference.style.width = "100px"
      reference.style.height = "100px"
      reference.style.position = "absolute"
      reference.style.left = "100px"
      reference.style.top = "100px"
      dom.document.body.appendChild(reference)

      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      floating.style.width = "50px"
      floating.style.height = "50px"
      dom.document.body.appendChild(floating)

      try {
        // Compute position with offset middleware
        val offsetValue = 10.0
        val result = FloatingUI.computePosition(
          reference,
          floating,
          placement = "bottom",
          middleware = Seq(OffsetMiddleware.offset(Left(Left(offsetValue))))
        )

        // Validates that offset middleware executes and populates data
        // jsdom has no layout engine, so we can't test actual position changes
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]

        // Middleware data should contain offset info
        result.middlewareData.offset.isDefined shouldBe true

        // In a real browser, result.y would be increased by offsetValue for bottom placement
        // In jsdom, both x and y are 0 because getBoundingClientRect returns zeros
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }
  }

  describe("shift middleware") {

    it("shifts floating element to stay within boundary") {
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      reference.style.width = "100px"
      reference.style.height = "100px"
      reference.style.position = "absolute"
      reference.style.left = "10px" // Near edge
      reference.style.top = "10px"
      dom.document.body.appendChild(reference)

      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      floating.style.width = "200px" // Larger than reference, will overflow
      floating.style.height = "50px"
      dom.document.body.appendChild(floating)

      try {
        // Compute position with shift middleware
        val result = FloatingUI.computePosition(
          reference,
          floating,
          placement = "bottom",
          middleware = Seq(ShiftMiddleware.shift())
        )

        // Validates that shift middleware executes and populates data
        // jsdom has no layout engine, so we can't test actual shifting behavior
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]

        // Middleware data should contain shift info
        result.middlewareData.shift.isDefined shouldBe true

        // In a real browser, the floating element would be shifted to stay within viewport
        // In jsdom, coordinates are 0 because getBoundingClientRect returns zeros
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }
  }

  describe("flip middleware") {

    it("flips placement when there is not enough space") {
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      reference.style.width = "100px"
      reference.style.height = "100px"
      reference.style.position = "absolute"
      reference.style.left = "50px"
      reference.style.top = "10px" // Near top edge
      dom.document.body.appendChild(reference)

      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      floating.style.width = "50px"
      floating.style.height = "200px" // Tall element that won't fit above
      dom.document.body.appendChild(floating)

      try {
        // Try to place on top - in a real browser, would flip to bottom due to lack of space
        val result = FloatingUI.computePosition(
          reference,
          floating,
          placement = "top",
          middleware = Seq(FlipMiddleware.flip())
        )

        // Validates that flip middleware executes and populates data
        // jsdom has no layout engine, so we can't test actual flipping behavior
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]

        // Middleware data should contain flip info
        result.middlewareData.flip.isDefined shouldBe true

        // Placement is returned (may or may not have flipped in jsdom)
        result.placement should not be null

        // In a real browser, placement would flip from "top" to "bottom" due to insufficient space
        // In jsdom, flipping logic may not work correctly because overflow detection requires layout
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }
  }

  describe("arrow middleware") {

    it("computes arrow position") {
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      reference.style.width = "100px"
      reference.style.height = "100px"
      reference.style.position = "absolute"
      reference.style.left = "100px"
      reference.style.top = "100px"
      dom.document.body.appendChild(reference)

      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      floating.style.width = "80px"
      floating.style.height = "60px"
      dom.document.body.appendChild(floating)

      val arrow = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      arrow.style.width = "10px"
      arrow.style.height = "10px"
      floating.appendChild(arrow)

      try {
        // Compute position with arrow middleware
        val result = FloatingUI.computePosition(
          reference,
          floating,
          placement = "bottom",
          middleware = Seq(ArrowMiddleware.arrow(Left(ArrowOptions(element = arrow))))
        )

        // Validates that arrow middleware executes and populates data
        // jsdom has no layout engine, so we can't test actual arrow positioning
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]

        // Middleware data should contain arrow info with x or y position
        result.middlewareData.arrow.isDefined shouldBe true
        val arrowData = result.middlewareData.arrow.get

        // Arrow should have either x or y coordinate (depending on placement)
        (arrowData.x.isDefined || arrowData.y.isDefined) shouldBe true

        // In a real browser, arrow coordinates would position the arrow to point at reference
        // In jsdom, coordinates are 0 because getBoundingClientRect returns zeros
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }
  }

  describe("autoPlacement middleware") {

    it("automatically chooses best placement") {
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      reference.style.width = "100px"
      reference.style.height = "100px"
      reference.style.position = "absolute"
      reference.style.left = "50px"
      reference.style.top = "50px"
      dom.document.body.appendChild(reference)

      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      floating.style.width = "60px"
      floating.style.height = "60px"
      dom.document.body.appendChild(floating)

      try {
        // Use autoPlacement to automatically choose the best placement
        val result = FloatingUI.computePosition(
          reference,
          floating,
          middleware = Seq(AutoPlacementMiddleware.autoPlacement())
        )

        // Validates that autoPlacement middleware executes and populates data
        // jsdom has no layout engine, so we can't test actual placement selection logic
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]

        // Middleware data should contain autoPlacement info
        result.middlewareData.autoPlacement.isDefined shouldBe true

        // Should have chosen a placement
        result.placement should not be null

        // In a real browser, autoPlacement would analyze available space and choose optimal placement
        // In jsdom, placement selection may not work correctly because it requires layout calculations
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }
  }

  describe("hide middleware") {

    it("detects when floating element should be hidden") {
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      reference.style.width = "100px"
      reference.style.height = "100px"
      reference.style.position = "absolute"
      reference.style.left = "50px"
      reference.style.top = "50px"
      dom.document.body.appendChild(reference)

      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      floating.style.width = "60px"
      floating.style.height = "60px"
      dom.document.body.appendChild(floating)

      try {
        // Use hide middleware to detect visibility
        val result = FloatingUI.computePosition(
          reference,
          floating,
          placement = "bottom",
          middleware = Seq(HideMiddleware.hide())
        )

        // Validates that hide middleware executes and populates data
        // jsdom has no layout engine, so we can't test actual visibility detection
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]

        // Middleware data should contain hide info
        result.middlewareData.hide.isDefined shouldBe true
        val hideData = result.middlewareData.hide.get

        // Hide data should exist (fields may or may not be populated depending on scenario)
        hideData should not be null

        // In a real browser, hide middleware would detect if reference/floating is clipped or escaped
        // In jsdom, visibility detection may not work correctly because it requires layout calculations
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }
  }

  describe("size middleware") {

    it("provides size information for floating element") {
      val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      reference.style.width = "100px"
      reference.style.height = "100px"
      reference.style.position = "absolute"
      reference.style.left = "50px"
      reference.style.top = "50px"
      dom.document.body.appendChild(reference)

      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      floating.style.width = "60px"
      floating.style.height = "60px"
      dom.document.body.appendChild(floating)

      try {
        // Use size middleware
        val result = FloatingUI.computePosition(
          reference,
          floating,
          placement = "bottom",
          middleware = Seq(SizeMiddleware.size())
        )

        // Validates that size middleware executes without error
        // jsdom has no layout engine, so we can't test actual size calculations
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]

        // Size middleware doesn't add data to middlewareData, but should execute without error
        result.placement should not be null

        // In a real browser, size middleware would provide available width/height for responsive sizing
        // In jsdom, size calculations may not work correctly because they require layout
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }
  }

  describe("inline middleware") {

    it("handles inline reference elements") {
      val reference = dom.document.createElement("span").asInstanceOf[dom.HTMLElement]
      reference.textContent = "Inline reference"
      reference.style.position = "absolute"
      reference.style.left = "50px"
      reference.style.top = "50px"
      dom.document.body.appendChild(reference)

      val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
      floating.style.width = "60px"
      floating.style.height = "60px"
      dom.document.body.appendChild(floating)

      try {
        // Use inline middleware
        val result = FloatingUI.computePosition(
          reference,
          floating,
          placement = "bottom",
          middleware = Seq(InlineMiddleware.inline())
        )

        // Validates that inline middleware executes without error
        // jsdom has no layout engine, so we can't test actual inline element handling
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]

        // Inline middleware doesn't add data to middlewareData, but should execute without error
        result.placement should not be null

        // In a real browser, inline middleware would handle positioning relative to inline elements
        // In jsdom, inline element calculations may not work correctly because they require layout
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }
  }
}
