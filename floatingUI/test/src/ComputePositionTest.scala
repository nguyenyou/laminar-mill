package io.github.nguyenyou.floatingUI

import org.scalajs.dom
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.FloatingUI

/** Tests for core computePosition functionality.
  *
  * Tests the main positioning algorithm with various configurations.
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

        // Should return a valid result
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
        // Test top placement
        val topResult = FloatingUI.computePosition(reference, floating, placement = "top")
        topResult.placement shouldBe "top"

        // Test right placement
        val rightResult = FloatingUI.computePosition(reference, floating, placement = "right")
        rightResult.placement shouldBe "right"

        // Test left placement
        val leftResult = FloatingUI.computePosition(reference, floating, placement = "left")
        leftResult.placement shouldBe "left"

        // Test bottom-start placement
        val bottomStartResult = FloatingUI.computePosition(reference, floating, placement = "bottom-start")
        bottomStartResult.placement shouldBe "bottom-start"
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }
  }
}
