package io.github.nguyenyou.floatingUI

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalajs.dom
import scala.scalajs.js
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.DOMUtils

/** Test suite for Boundary type and BoundaryInternal conversion.
  *
  * Tests the new Boundary union type that supports:
  *   - String "clippingAncestors"
  *   - String CSS selectors
  *   - dom.Element
  *   - js.Array[dom.Element]
  *   - Rect
  */
class BoundarySpec extends AnyFlatSpec with Matchers {

  "BoundaryInternal.fromBoundary" should "handle clippingAncestors string" in {
    val boundary: Boundary = "clippingAncestors"
    val internal = BoundaryInternal.fromBoundary(boundary)
    internal shouldBe BoundaryInternal.ClippingAncestors
  }

  it should "handle CSS selector string that matches an element" in {
    // Setup: Create a test element with ID
    val testDiv = dom.document.createElement("div")
    testDiv.id = "test-boundary-element"
    dom.document.body.appendChild(testDiv)

    try {
      val boundary: Boundary = "#test-boundary-element"
      val internal = BoundaryInternal.fromBoundary(boundary)

      internal match {
        case BoundaryInternal.Element(el) => el.id shouldBe "test-boundary-element"
        case _                            => fail("Expected Element variant")
      }
    } finally {
      // Cleanup
      dom.document.body.removeChild(testDiv)
    }
  }

  it should "fallback to ClippingAncestors for CSS selector that doesn't match" in {
    val boundary: Boundary = "#non-existent-element-12345"
    val internal = BoundaryInternal.fromBoundary(boundary)
    internal shouldBe BoundaryInternal.ClippingAncestors
  }

  it should "handle single Element" in {
    val testDiv = dom.document.createElement("div").asInstanceOf[dom.Element]
    val boundary: Boundary = testDiv
    val internal = BoundaryInternal.fromBoundary(boundary)

    internal match {
      case BoundaryInternal.Element(el) => el shouldBe testDiv
      case _                            => fail("Expected Element variant")
    }
  }

  it should "handle js.Array of Elements" in {
    val el1 = dom.document.createElement("div").asInstanceOf[dom.Element]
    val el2 = dom.document.createElement("div").asInstanceOf[dom.Element]
    val boundary: Boundary = js.Array(el1, el2)
    val internal = BoundaryInternal.fromBoundary(boundary)

    internal match {
      case BoundaryInternal.Elements(arr) =>
        arr.length shouldBe 2
        arr(0) shouldBe el1
        arr(1) shouldBe el2
      case _ => fail("Expected Elements variant")
    }
  }

  it should "handle custom Rect" in {
    val rect = Rect(x = 10, y = 20, width = 100, height = 200)
    val boundary: Boundary = rect
    val internal = BoundaryInternal.fromBoundary(boundary)

    internal match {
      case BoundaryInternal.CustomRect(r) => r shouldBe rect
      case _                              => fail("Expected CustomRect variant")
    }
  }

  "getClippingRect" should "work with clippingAncestors string" in {
    val element = dom.document.createElement("div").asInstanceOf[dom.Element]
    dom.document.body.appendChild(element)

    try {
      val rect = DOMUtils.getClippingRect(
        element,
        boundary = "clippingAncestors",
        rootBoundary = "viewport",
        strategy = Strategy.Absolute
      )

      // Should return a valid rect with positive dimensions
      rect.width should be > 0.0
      rect.height should be > 0.0
    } finally {
      dom.document.body.removeChild(element)
    }
  }

  it should "work with Element boundary" in {
    // Create a container with specific dimensions
    val container = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    container.style.position = "relative"
    container.style.width = "500px"
    container.style.height = "300px"
    container.style.overflow = "hidden"
    dom.document.body.appendChild(container)

    val element = dom.document.createElement("div").asInstanceOf[dom.Element]
    container.appendChild(element)

    try {
      val rect = DOMUtils.getClippingRect(
        element,
        boundary = container, // Pass element directly
        rootBoundary = "viewport",
        strategy = Strategy.Absolute
      )

      // Should return a valid rect
      rect.width should be >= 0.0
      rect.height should be >= 0.0
    } finally {
      dom.document.body.removeChild(container)
    }
  }

  it should "work with custom Rect boundary" in {
    val element = dom.document.createElement("div").asInstanceOf[dom.Element]
    dom.document.body.appendChild(element)

    try {
      val customRect = Rect(x = 0, y = 0, width = 200, height = 150)
      val rect = DOMUtils.getClippingRect(
        element,
        boundary = customRect,
        rootBoundary = "viewport",
        strategy = Strategy.Absolute
      )

      // Should return a valid rect
      rect.width should be >= 0.0
      rect.height should be >= 0.0
    } finally {
      dom.document.body.removeChild(element)
    }
  }

  "DetectOverflowOptions" should "accept Boundary type" in {
    // Test that all variants of Boundary work with DetectOverflowOptions

    // String variant
    val opts1 = DetectOverflowOptions(boundary = "clippingAncestors")
    opts1.boundary shouldBe "clippingAncestors"

    // Element variant
    val el = dom.document.createElement("div").asInstanceOf[dom.Element]
    val opts2 = DetectOverflowOptions(boundary = el)
    opts2.boundary shouldBe el

    // Array variant
    val arr = js.Array(el)
    val opts3 = DetectOverflowOptions(boundary = arr)
    opts3.boundary shouldBe arr

    // Rect variant
    val rect = Rect(0, 0, 100, 100)
    val opts4 = DetectOverflowOptions(boundary = rect)
    opts4.boundary shouldBe rect
  }

  "Middleware options" should "accept Boundary type" in {
    val el = dom.document.createElement("div").asInstanceOf[dom.Element]

    // FlipOptions
    val flipOpts = FlipOptions(boundary = el)
    flipOpts.boundary shouldBe el

    // ShiftOptions
    val shiftOpts = ShiftOptions(boundary = el)
    shiftOpts.boundary shouldBe el

    // AutoPlacementOptions
    val autoOpts = AutoPlacementOptions(boundary = el)
    autoOpts.boundary shouldBe el

    // HideOptions
    val hideOpts = HideOptions(boundary = el)
    hideOpts.boundary shouldBe el

    // SizeOptions
    val sizeOpts = SizeOptions(boundary = el)
    sizeOpts.boundary shouldBe el
  }

  "Backward compatibility" should "work with existing string-based code" in {
    // All existing code using strings should continue to work

    val opts1 = FlipOptions(boundary = "clippingAncestors")
    opts1.boundary shouldBe "clippingAncestors"

    val opts2 = ShiftOptions(boundary = "#container")
    opts2.boundary shouldBe "#container"

    // Default values should still work
    val opts3 = DetectOverflowOptions()
    opts3.boundary shouldBe "clippingAncestors"
  }
}
