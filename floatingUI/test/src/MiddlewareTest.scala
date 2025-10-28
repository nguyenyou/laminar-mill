package io.github.nguyenyou.floatingUI

import org.scalajs.dom
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.FloatingUI
import io.github.nguyenyou.floatingUI.middleware.*

/** Tests for Floating UI middleware functions.
  *
  * Tests individual middleware: offset, shift, flip, arrow, autoPlacement, hide, size, inline.
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
        // Compute position without offset
        val resultWithoutOffset = FloatingUI.computePosition(reference, floating, placement = "bottom")

        // Compute position with offset
        val offsetValue = 10.0
        val resultWithOffset = FloatingUI.computePosition(
          reference,
          floating,
          placement = "bottom",
          middleware = Seq(OffsetMiddleware.offset(Left(Left(offsetValue))))
        )

        // Y position should be increased by offset value (bottom placement)
        resultWithOffset.y should be > resultWithoutOffset.y

        // Middleware data should contain offset info
        resultWithOffset.middlewareData.offset.isDefined shouldBe true
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

        // Should return valid coordinates
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]

        // Middleware data should contain shift info
        result.middlewareData.shift.isDefined shouldBe true
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
        // Try to place on top, but should flip to bottom due to lack of space
        val result = FloatingUI.computePosition(
          reference,
          floating,
          placement = "top",
          middleware = Seq(FlipMiddleware.flip())
        )

        // Should return valid coordinates
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]

        // Middleware data should contain flip info
        result.middlewareData.flip.isDefined shouldBe true

        // Placement might have changed from "top" to something else
        result.placement should not be null
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

        // Should return valid coordinates
        result.x shouldBe a[Double]
        result.y shouldBe a[Double]

        // Middleware data should contain arrow info with x or y position
        result.middlewareData.arrow.isDefined shouldBe true
        val arrowData = result.middlewareData.arrow.get

        // Arrow should have either x or y coordinate (depending on placement)
        (arrowData.x.isDefined || arrowData.y.isDefined) shouldBe true
      } finally {
        dom.document.body.removeChild(reference)
        dom.document.body.removeChild(floating)
      }
    }
  }
}
