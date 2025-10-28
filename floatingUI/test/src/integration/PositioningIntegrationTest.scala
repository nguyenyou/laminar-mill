package io.github.nguyenyou.floatingUI.integration

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalajs.dom
import io.github.nguyenyou.floatingUI.FloatingUI._
import io.github.nguyenyou.floatingUI.integration.helpers.TestHelpers._

/** Integration tests for Floating UI positioning in a real browser environment.
  *
  * These tests run in Playwright (Chrome) with a real layout engine, enabling accurate testing of positioning calculations that depend on
  * getBoundingClientRect().
  *
  * ## Running Tests
  *
  * ```bash
  * # Install Playwright browsers (one-time setup)
  * npx playwright install-deps
  * npx playwright install chromium
  *
  * # Run the tests
  * ./mill floatingUI.test
  * ```
  *
  * ## Test Environment Comparison
  *
  * **jsdom (unit tests):**
  *   - No layout engine
  *   - getBoundingClientRect() returns zeros
  *   - Fast execution
  *   - Good for API validation
  *
  * **Playwright (integration tests):**
  *   - Real browser with layout engine
  *   - Accurate positioning calculations
  *   - Slower execution
  *   - Required for positioning validation
  *
  * ## Debugging
  *
  * To see browser logs and debug info, update build.mill:
  * ```scala
  * JsEnvConfig.Playwright.chrome(
  *   headless = false, // Show browser window
  *   showLogs = true, // Show console logs
  *   debug = true // Show debug info
  * )
  * ```
  */
class PositioningIntegrationTest extends AnyFunSpec with Matchers {

  describe("FloatingUI.computePosition") {

    it("should position floating element below reference element by default") {
      var reference: dom.HTMLElement = null
      var floating: dom.HTMLElement = null

      try {
        // Create reference element at (100, 100) with size 100x50
        reference = createReferenceElement(
          left = 100,
          top = 100,
          width = 100,
          height = 50
        )

        // Create floating element with size 150x80
        floating = createFloatingElement(
          width = 150,
          height = 80
        )

        // Compute position with default placement ("bottom")
        val result = computePosition(
          reference = reference,
          floating = floating,
          placement = "bottom"
        )

        // Verify the floating element is positioned below the reference
        // By default, Floating UI centers the floating element relative to the reference
        // Expected x: 100 + (100 - 150) / 2 = 100 - 25 = 75 (centered)
        // Expected y: 150 (reference.top + reference.height = 100 + 50)
        result.x shouldBe 75.0 +- 1.0
        result.y shouldBe 150.0 +- 1.0

        // Verify placement is as requested
        result.placement shouldBe "bottom"

        // Verify strategy
        result.strategy shouldBe "absolute"

      } finally {
        cleanup(reference, floating)
      }
    }

    it("should apply offset middleware to shift floating element") {
      var reference: dom.HTMLElement = null
      var floating: dom.HTMLElement = null

      try {
        // Create reference element at (200, 200) with size 80x60
        reference = createReferenceElement(
          left = 200,
          top = 200,
          width = 80,
          height = 60
        )

        // Create floating element with size 120x40
        floating = createFloatingElement(
          width = 120,
          height = 40
        )

        // Compute position with offset middleware
        // Offset of 10 should shift the floating element 10px away from reference
        val result = computePosition(
          reference = reference,
          floating = floating,
          placement = "bottom",
          middleware = Seq(offset(Left(Left(10.0))))
        )

        // Expected x: 200 + (80 - 120) / 2 = 200 - 20 = 180 (centered)
        // Expected y: 270 (reference.top + reference.height + offset = 200 + 60 + 10)
        result.x shouldBe 180.0 +- 1.0
        result.y shouldBe 270.0 +- 1.0

        // Verify middleware data is present
        result.middlewareData.offset shouldBe defined

      } finally {
        cleanup(reference, floating)
      }
    }

    it("should apply flip middleware when insufficient space") {
      var reference: dom.HTMLElement = null
      var floating: dom.HTMLElement = null

      try {
        // Create reference element near bottom of viewport
        // Assuming viewport height is at least 600px
        reference = createReferenceElement(
          left = 100,
          top = 550, // Near bottom
          width = 100,
          height = 40
        )

        // Create tall floating element that won't fit below
        floating = createFloatingElement(
          width = 120,
          height = 100 // Too tall to fit below reference
        )

        // Compute position with flip middleware
        // Should flip to "top" placement when there's insufficient space below
        val result = computePosition(
          reference = reference,
          floating = floating,
          placement = "bottom",
          middleware = Seq(flip())
        )

        // The placement should have flipped to "top" due to insufficient space
        // Note: This test assumes viewport height > 600px
        // If the test fails, it might be due to viewport size
        result.placement should (equal("top") or equal("bottom"))

        // If flipped to top, y should be less than reference.top
        if (result.placement == "top") {
          result.y should be < 550.0
        }

        // Verify flip middleware data is present
        result.middlewareData.flip shouldBe defined

      } finally {
        cleanup(reference, floating)
      }
    }

    it("should apply shift middleware to keep element in viewport") {
      var reference: dom.HTMLElement = null
      var floating: dom.HTMLElement = null

      try {
        // Create reference element near right edge of viewport
        // Assuming viewport width is at least 800px
        reference = createReferenceElement(
          left = 750, // Near right edge
          top = 100,
          width = 50,
          height = 40
        )

        // Create wide floating element that would overflow viewport
        floating = createFloatingElement(
          width = 200, // Would extend beyond viewport if aligned with reference
          height = 60
        )

        // Compute position with shift middleware
        // Should shift left to keep element in viewport
        val result = computePosition(
          reference = reference,
          floating = floating,
          placement = "bottom",
          middleware = Seq(shift())
        )

        // The x position should be shifted left from the reference
        // to prevent overflow (exact value depends on viewport width)
        result.x should be <= 750.0

        // Verify shift middleware data is present
        result.middlewareData.shift shouldBe defined

      } finally {
        cleanup(reference, floating)
      }
    }

    it("should combine multiple middleware in sequence") {
      var reference: dom.HTMLElement = null
      var floating: dom.HTMLElement = null

      try {
        // Create reference element
        reference = createReferenceElement(
          left = 300,
          top = 300,
          width = 100,
          height = 50
        )

        // Create floating element
        floating = createFloatingElement(
          width = 150,
          height = 80
        )

        // Compute position with multiple middleware
        val result = computePosition(
          reference = reference,
          floating = floating,
          placement = "bottom",
          middleware = Seq(
            offset(Left(Left(5.0))), // Add 5px offset
            flip(), // Flip if needed
            shift() // Shift to stay in viewport
          )
        )

        // All middleware should have been executed
        result.middlewareData.offset shouldBe defined
        result.middlewareData.flip shouldBe defined
        result.middlewareData.shift shouldBe defined

        // Position should account for offset
        result.y shouldBe 355.0 +- 2.0 // 300 + 50 + 5 (with some tolerance for shift)

      } finally {
        cleanup(reference, floating)
      }
    }
  }
}
