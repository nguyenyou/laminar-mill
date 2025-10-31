package io.github.nguyenyou.floatingUI.integration

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalajs.dom
import io.github.nguyenyou.floatingUI.FloatingUI.computePosition
import io.github.nguyenyou.floatingUI.middleware.FlipMiddleware
import io.github.nguyenyou.floatingUI.Types.Placement.*

/** Integration test that mimics the Flip component structure.
  *
  * This test creates a DOM structure similar to the Flip component in www/src/www/floating/Flip.scala:
  *   - A scroll container with overflow
  *   - A reference element positioned inside the scroll container
  *   - A floating element that should be positioned relative to the reference
  *
  * The test verifies that computePosition correctly calculates coordinates accounting for:
  *   - The reference element's position within the scroll container
  *   - The scroll container's scroll position
  *   - The floating element's dimensions
  *
  * ## Running This Test
  *
  * ```bash
  * # One-time setup (if not already done)
  * npx playwright install-deps
  * npx playwright install chromium
  *
  * # Run all floatingUI tests
  * ./mill floatingUI.test
  *
  * # Run only this test
  * ./mill floatingUI.test -- -z "FlipComponentPositioningTest"
  * ```
  *
  * ## Expected Behavior
  *
  * For "bottom" placement (default), the floating element should be positioned:
  *   - X: reference.offsetLeft + (reference.offsetWidth / 2) - (floating.offsetWidth / 2)
  *   - Y: reference.offsetTop + reference.offsetHeight
  *
  * These coordinates are relative to the offset parent (the scroll container in this case).
  */
class FlipComponentPositioningTest extends AnyFunSpec with Matchers {

  describe("computePosition with Flip component structure") {

    it("should calculate correct coordinates for reference and floating elements in a scroll container") {
      var scrollContainer: dom.HTMLElement = null
      var reference: dom.HTMLElement = null
      var floating: dom.HTMLElement = null

      try {
        // Create scroll container (similar to .scroll div in Flip.scala)
        scrollContainer = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
        scrollContainer.className = "scroll"
        scrollContainer.style.position = "relative"
        scrollContainer.style.width = "450px"
        scrollContainer.style.height = "450px"
        scrollContainer.style.overflow = "auto"
        scrollContainer.style.border = "1px solid #ccc"

        // Create large content area to enable scrolling (1660px height as in Flip.scala)
        val contentArea = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
        contentArea.style.width = "1500px"
        contentArea.style.height = "1660px"
        contentArea.style.position = "relative"
        scrollContainer.appendChild(contentArea)

        // Create reference element (similar to .reference div in Flip.scala)
        // Positioned at (670, 750) within the scroll container
        reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
        reference.className = "reference"
        reference.style.position = "absolute"
        reference.style.left = "670px"
        reference.style.top = "750px"
        reference.style.width = "160px"
        reference.style.height = "160px"
        reference.style.backgroundColor = "#ddd"
        reference.textContent = "Reference"
        contentArea.appendChild(reference)

        // Create floating element (similar to .floating div in Flip.scala)
        // Initial position at (711, 910) as in the original code
        floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
        floating.className = "floating"
        floating.style.position = "absolute"
        floating.style.left = "711px"
        floating.style.top = "910px"
        floating.style.width = "80px"
        floating.style.height = "80px"
        floating.style.backgroundColor = "#333"
        floating.style.color = "#fff"
        floating.textContent = "Floating"
        contentArea.appendChild(floating)

        // Append scroll container to body
        dom.document.body.appendChild(scrollContainer)

        // Center the scroll container (as done in Flip.scala lines 84-87)
        val scrollY = scrollContainer.scrollHeight / 2.0 - scrollContainer.offsetHeight / 2.0
        val scrollX = scrollContainer.scrollWidth / 2.0 - scrollContainer.offsetWidth / 2.0
        scrollContainer.scrollTop = scrollY
        scrollContainer.scrollLeft = scrollX

        // Compute position using FloatingUI (as done in Flip.scala lines 49-53)
        val result = computePosition(
          reference = reference,
          floating = floating,
          placement = Bottom,
          middleware = Seq(FlipMiddleware.flip())
        )

        // Expected coordinates for "bottom" placement:
        // X = reference.offsetLeft + (reference.width / 2) - (floating.width / 2)
        //   = 670 + (160 / 2) - (80 / 2)
        //   = 670 + 80 - 40
        //   = 710
        // Y = reference.offsetTop + reference.height
        //   = 750 + 160
        //   = 910

        val expectedX = 710.0
        val expectedY = 910.0

        // Verify the calculated coordinates match expected values
        result.x shouldBe expectedX +- 1.0 // Allow 1px tolerance for rounding
        result.y shouldBe expectedY +- 1.0

        // Verify placement and strategy
        result.placement shouldBe Bottom
        result.strategy shouldBe "absolute"

        // Log the results for debugging (similar to Flip.scala line 54)
        println(s"✓ Calculated coordinates: X: ${result.x}, Y: ${result.y}")
        println(s"✓ Expected coordinates: X: $expectedX, Y: $expectedY")
        println(s"✓ Reference position: offsetLeft=${reference.offsetLeft}, offsetTop=${reference.offsetTop}")
        println(s"✓ Scroll position: scrollLeft=${scrollContainer.scrollLeft}, scrollTop=${scrollContainer.scrollTop}")

      } finally {
        // Cleanup
        if (scrollContainer != null && scrollContainer.parentNode != null) {
          scrollContainer.parentNode.removeChild(scrollContainer)
        }
      }
    }

    it("should handle scroll position changes correctly") {
      var scrollContainer: dom.HTMLElement = null
      var reference: dom.HTMLElement = null
      var floating: dom.HTMLElement = null

      try {
        // Create scroll container
        scrollContainer = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
        scrollContainer.style.position = "relative"
        scrollContainer.style.width = "450px"
        scrollContainer.style.height = "450px"
        scrollContainer.style.overflow = "auto"

        val contentArea = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
        contentArea.style.width = "1500px"
        contentArea.style.height = "1660px"
        contentArea.style.position = "relative"
        scrollContainer.appendChild(contentArea)

        // Create reference element
        reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
        reference.style.position = "absolute"
        reference.style.left = "670px"
        reference.style.top = "750px"
        reference.style.width = "160px"
        reference.style.height = "160px"
        contentArea.appendChild(reference)

        // Create floating element
        floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
        floating.style.position = "absolute"
        floating.style.width = "80px"
        floating.style.height = "80px"
        contentArea.appendChild(floating)

        dom.document.body.appendChild(scrollContainer)

        // Test 1: No scroll
        scrollContainer.scrollTop = 0
        scrollContainer.scrollLeft = 0

        val result1 = computePosition(
          reference = reference,
          floating = floating,
          placement = Bottom
        )

        // Expected: X=710, Y=910 (same as before, coordinates are relative to offset parent)
        result1.x shouldBe 710.0 +- 1.0
        result1.y shouldBe 910.0 +- 1.0

        println(s"✓ No scroll - X: ${result1.x}, Y: ${result1.y}")

        // Test 2: Scroll to center (as in Flip component)
        val scrollY = scrollContainer.scrollHeight / 2.0 - scrollContainer.offsetHeight / 2.0
        val scrollX = scrollContainer.scrollWidth / 2.0 - scrollContainer.offsetWidth / 2.0
        scrollContainer.scrollTop = scrollY
        scrollContainer.scrollLeft = scrollX

        val result2 = computePosition(
          reference = reference,
          floating = floating,
          placement = Bottom
        )

        // Coordinates should remain the same because they're relative to offset parent
        result2.x shouldBe 710.0 +- 1.0
        result2.y shouldBe 910.0 +- 1.0

        println(s"✓ Centered scroll - X: ${result2.x}, Y: ${result2.y}")
        println(s"  Scroll position: scrollLeft=${scrollContainer.scrollLeft.toInt}, scrollTop=${scrollContainer.scrollTop.toInt}")

      } finally {
        if (scrollContainer != null && scrollContainer.parentNode != null) {
          scrollContainer.parentNode.removeChild(scrollContainer)
        }
      }
    }

    it("should position floating element correctly with different reference positions") {
      var scrollContainer: dom.HTMLElement = null
      var reference: dom.HTMLElement = null
      var floating: dom.HTMLElement = null

      try {
        // Create scroll container
        scrollContainer = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
        scrollContainer.style.position = "relative"
        scrollContainer.style.width = "450px"
        scrollContainer.style.height = "450px"
        scrollContainer.style.overflow = "auto"

        val contentArea = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
        contentArea.style.width = "1000px"
        contentArea.style.height = "1000px"
        contentArea.style.position = "relative"
        scrollContainer.appendChild(contentArea)

        dom.document.body.appendChild(scrollContainer)

        // Test different reference positions
        val testCases = Seq(
          (100.0, 100.0, 100.0, 50.0, 150.0, 80.0), // (refLeft, refTop, refWidth, refHeight, floatWidth, floatHeight)
          (300.0, 200.0, 120.0, 60.0, 100.0, 70.0),
          (500.0, 400.0, 80.0, 80.0, 60.0, 60.0)
        )

        testCases.foreach { case (refLeft, refTop, refWidth, refHeight, floatWidth, floatHeight) =>
          // Create reference element
          reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
          reference.style.position = "absolute"
          reference.style.left = s"${refLeft}px"
          reference.style.top = s"${refTop}px"
          reference.style.width = s"${refWidth}px"
          reference.style.height = s"${refHeight}px"
          contentArea.appendChild(reference)

          // Create floating element
          floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
          floating.style.position = "absolute"
          floating.style.width = s"${floatWidth}px"
          floating.style.height = s"${floatHeight}px"
          contentArea.appendChild(floating)

          // Compute position
          val result = computePosition(
            reference = reference,
            floating = floating,
            placement = Bottom
          )

          // Calculate expected coordinates
          val expectedX = refLeft + (refWidth / 2.0) - (floatWidth / 2.0)
          val expectedY = refTop + refHeight

          // Verify
          result.x shouldBe expectedX +- 1.0
          result.y shouldBe expectedY +- 1.0

          println(s"✓ Reference at ($refLeft, $refTop) ${refWidth}x${refHeight}, Floating ${floatWidth}x${floatHeight}")
          println(s"  Calculated: X=${result.x}, Y=${result.y}, Expected: X=$expectedX, Y=$expectedY")

          // Cleanup for next iteration
          contentArea.removeChild(reference)
          contentArea.removeChild(floating)
        }

      } finally {
        if (scrollContainer != null && scrollContainer.parentNode != null) {
          scrollContainer.parentNode.removeChild(scrollContainer)
        }
      }
    }
  }
}
