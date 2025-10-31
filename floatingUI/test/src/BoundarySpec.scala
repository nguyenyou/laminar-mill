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

  // ============================================================================
  // RootBoundary Tests
  // ============================================================================

  "RootBoundaryInternal.fromRootBoundary" should "handle viewport string" in {
    val rootBoundary: RootBoundary = "viewport"
    val internal = RootBoundaryInternal.fromRootBoundary(rootBoundary)
    internal shouldBe RootBoundaryInternal.Viewport
  }

  it should "handle document string" in {
    val rootBoundary: RootBoundary = "document"
    val internal = RootBoundaryInternal.fromRootBoundary(rootBoundary)
    internal shouldBe RootBoundaryInternal.Document
  }

  it should "handle custom Rect" in {
    val rect = Rect(x = 0, y = 0, width = 1920, height = 1080)
    val rootBoundary: RootBoundary = rect
    val internal = RootBoundaryInternal.fromRootBoundary(rootBoundary)

    internal match {
      case RootBoundaryInternal.CustomRect(r) => r shouldBe rect
      case _                                  => fail("Expected CustomRect variant")
    }
  }

  it should "fallback to Viewport for unknown strings" in {
    val rootBoundary: RootBoundary = "unknown"
    val internal = RootBoundaryInternal.fromRootBoundary(rootBoundary)
    internal shouldBe RootBoundaryInternal.Viewport
  }

  "getClippingRect" should "work with viewport rootBoundary" in {
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

  it should "work with document rootBoundary" in {
    val element = dom.document.createElement("div").asInstanceOf[dom.Element]
    dom.document.body.appendChild(element)

    try {
      val rect = DOMUtils.getClippingRect(
        element,
        boundary = "clippingAncestors",
        rootBoundary = "document",
        strategy = Strategy.Absolute
      )

      // Should return a valid rect
      rect.width should be >= 0.0
      rect.height should be >= 0.0
    } finally {
      dom.document.body.removeChild(element)
    }
  }

  it should "work with custom Rect rootBoundary" in {
    val element = dom.document.createElement("div").asInstanceOf[dom.Element]
    dom.document.body.appendChild(element)

    try {
      val customRoot = Rect(x = 0, y = 0, width = 1920, height = 1080)
      val rect = DOMUtils.getClippingRect(
        element,
        boundary = "clippingAncestors",
        rootBoundary = customRoot,
        strategy = Strategy.Absolute
      )

      // Should return a valid rect
      rect.width should be >= 0.0
      rect.height should be >= 0.0
    } finally {
      dom.document.body.removeChild(element)
    }
  }

  "DetectOverflowOptions" should "accept RootBoundary type" in {
    // Test that all variants of RootBoundary work with DetectOverflowOptions

    // String "viewport" variant
    val opts1 = DetectOverflowOptions(rootBoundary = "viewport")
    opts1.rootBoundary shouldBe "viewport"

    // String "document" variant
    val opts2 = DetectOverflowOptions(rootBoundary = "document")
    opts2.rootBoundary shouldBe "document"

    // Rect variant
    val rect = Rect(0, 0, 1920, 1080)
    val opts3 = DetectOverflowOptions(rootBoundary = rect)
    opts3.rootBoundary shouldBe rect
  }

  "Middleware options" should "accept RootBoundary type" in {
    val rect = Rect(0, 0, 1920, 1080)

    // FlipOptions
    val flipOpts1 = FlipOptions(rootBoundary = "viewport")
    flipOpts1.rootBoundary shouldBe "viewport"

    val flipOpts2 = FlipOptions(rootBoundary = "document")
    flipOpts2.rootBoundary shouldBe "document"

    val flipOpts3 = FlipOptions(rootBoundary = rect)
    flipOpts3.rootBoundary shouldBe rect

    // ShiftOptions
    val shiftOpts = ShiftOptions(rootBoundary = rect)
    shiftOpts.rootBoundary shouldBe rect

    // AutoPlacementOptions
    val autoOpts = AutoPlacementOptions(rootBoundary = rect)
    autoOpts.rootBoundary shouldBe rect

    // HideOptions
    val hideOpts = HideOptions(rootBoundary = rect)
    hideOpts.rootBoundary shouldBe rect

    // SizeOptions
    val sizeOpts = SizeOptions(rootBoundary = rect)
    sizeOpts.rootBoundary shouldBe rect
  }

  "RootBoundary backward compatibility" should "work with existing string-based code" in {
    // All existing code using strings should continue to work

    val opts1 = FlipOptions(rootBoundary = "viewport")
    opts1.rootBoundary shouldBe "viewport"

    val opts2 = ShiftOptions(rootBoundary = "document")
    opts2.rootBoundary shouldBe "document"

    // Default values should still work
    val opts3 = DetectOverflowOptions()
    opts3.rootBoundary shouldBe "viewport"
  }

  // ============================================================================
  // ElementContext Tests
  // ============================================================================

  "ElementContext enum" should "have correct toValue for Reference" in {
    ElementContext.Reference.toValue shouldBe "reference"
  }

  it should "have correct toValue for Floating" in {
    ElementContext.Floating.toValue shouldBe "floating"
  }

  "ElementContext.fromString" should "parse 'reference' string" in {
    val context = ElementContext.fromString("reference")
    context shouldBe ElementContext.Reference
    context.toValue shouldBe "reference"
  }

  it should "parse 'floating' string" in {
    val context = ElementContext.fromString("floating")
    context shouldBe ElementContext.Floating
    context.toValue shouldBe "floating"
  }

  it should "throw exception for invalid string" in {
    assertThrows[IllegalArgumentException] {
      ElementContext.fromString("invalid")
    }
  }

  it should "throw exception with helpful error message" in {
    val exception = intercept[IllegalArgumentException] {
      ElementContext.fromString("unknown")
    }
    exception.getMessage should include("Invalid ElementContext")
    exception.getMessage should include("unknown")
    exception.getMessage should include("reference")
    exception.getMessage should include("floating")
  }

  "DetectOverflowOptions" should "accept ElementContext enum values" in {
    // Test Reference variant
    val opts1 = DetectOverflowOptions(elementContext = ElementContext.Reference)
    opts1.elementContext shouldBe ElementContext.Reference

    // Test Floating variant
    val opts2 = DetectOverflowOptions(elementContext = ElementContext.Floating)
    opts2.elementContext shouldBe ElementContext.Floating

    // Test default value
    val opts3 = DetectOverflowOptions()
    opts3.elementContext shouldBe ElementContext.Floating
  }

  "Middleware options" should "accept ElementContext enum values" in {
    // FlipOptions
    val flipOpts1 = FlipOptions(elementContext = ElementContext.Reference)
    flipOpts1.elementContext shouldBe ElementContext.Reference

    val flipOpts2 = FlipOptions(elementContext = ElementContext.Floating)
    flipOpts2.elementContext shouldBe ElementContext.Floating

    // ShiftOptions
    val shiftOpts = ShiftOptions(elementContext = ElementContext.Reference)
    shiftOpts.elementContext shouldBe ElementContext.Reference

    // AutoPlacementOptions
    val autoOpts = AutoPlacementOptions(elementContext = ElementContext.Floating)
    autoOpts.elementContext shouldBe ElementContext.Floating

    // HideOptions
    val hideOpts = HideOptions(elementContext = ElementContext.Reference)
    hideOpts.elementContext shouldBe ElementContext.Reference

    // SizeOptions
    val sizeOpts = SizeOptions(elementContext = ElementContext.Floating)
    sizeOpts.elementContext shouldBe ElementContext.Floating
  }

  "Middleware options" should "have correct default ElementContext" in {
    // All middleware options should default to Floating
    FlipOptions().elementContext shouldBe ElementContext.Floating
    ShiftOptions().elementContext shouldBe ElementContext.Floating
    AutoPlacementOptions().elementContext shouldBe ElementContext.Floating
    HideOptions().elementContext shouldBe ElementContext.Floating
    SizeOptions().elementContext shouldBe ElementContext.Floating
    DetectOverflowOptions().elementContext shouldBe ElementContext.Floating
  }

  "detectOverflow" should "accept ElementContext.Floating in options" in {
    // Test that detectOverflow accepts ElementContext enum values
    val options = DetectOverflowOptions(elementContext = ElementContext.Floating)
    options.elementContext shouldBe ElementContext.Floating
  }

  it should "accept ElementContext.Reference in options" in {
    // Test that detectOverflow accepts ElementContext enum values
    val options = DetectOverflowOptions(elementContext = ElementContext.Reference)
    options.elementContext shouldBe ElementContext.Reference
  }

  "ElementContext type safety" should "prevent invalid values at compile time" in {
    // This test verifies that the enum provides type safety
    // The following would not compile:
    // val opts = DetectOverflowOptions(elementContext = "invalid")

    // Only valid enum values are accepted
    val opts1 = DetectOverflowOptions(elementContext = ElementContext.Reference)
    val opts2 = DetectOverflowOptions(elementContext = ElementContext.Floating)

    opts1.elementContext shouldBe ElementContext.Reference
    opts2.elementContext shouldBe ElementContext.Floating
  }

  // ============================================================================
  // HideStrategy Tests
  // ============================================================================

  "HideStrategy enum" should "have correct toValue for ReferenceHidden" in {
    HideStrategy.ReferenceHidden.toValue shouldBe "referenceHidden"
  }

  it should "have correct toValue for Escaped" in {
    HideStrategy.Escaped.toValue shouldBe "escaped"
  }

  "HideStrategy.fromString" should "parse 'referenceHidden' string" in {
    val strategy = HideStrategy.fromString("referenceHidden")
    strategy shouldBe HideStrategy.ReferenceHidden
    strategy.toValue shouldBe "referenceHidden"
  }

  it should "parse 'escaped' string" in {
    val strategy = HideStrategy.fromString("escaped")
    strategy shouldBe HideStrategy.Escaped
    strategy.toValue shouldBe "escaped"
  }

  it should "throw exception for invalid string" in {
    assertThrows[IllegalArgumentException] {
      HideStrategy.fromString("invalid")
    }
  }

  it should "throw exception with helpful error message" in {
    val exception = intercept[IllegalArgumentException] {
      HideStrategy.fromString("hidden")
    }
    exception.getMessage should include("Invalid HideStrategy")
    exception.getMessage should include("hidden")
    exception.getMessage should include("referenceHidden")
    exception.getMessage should include("escaped")
  }

  "HideOptions" should "accept HideStrategy enum values" in {
    // Test ReferenceHidden variant
    val opts1 = HideOptions(strategy = HideStrategy.ReferenceHidden)
    opts1.strategy shouldBe HideStrategy.ReferenceHidden

    // Test Escaped variant
    val opts2 = HideOptions(strategy = HideStrategy.Escaped)
    opts2.strategy shouldBe HideStrategy.Escaped

    // Test default value
    val opts3 = HideOptions()
    opts3.strategy shouldBe HideStrategy.ReferenceHidden
  }

  it should "have correct default HideStrategy" in {
    // Default should be ReferenceHidden (matching TypeScript default)
    HideOptions().strategy shouldBe HideStrategy.ReferenceHidden
  }

  "HideStrategy type safety" should "prevent invalid values at compile time" in {
    // This test verifies that the enum provides type safety
    // The following would not compile:
    // val opts = HideOptions(strategy = "invalid")
    // val opts = HideOptions(strategy = "hidden")

    // Only valid enum values are accepted
    val opts1 = HideOptions(strategy = HideStrategy.ReferenceHidden)
    val opts2 = HideOptions(strategy = HideStrategy.Escaped)

    opts1.strategy shouldBe HideStrategy.ReferenceHidden
    opts2.strategy shouldBe HideStrategy.Escaped
  }

  "HideStrategy enum" should "support pattern matching" in {
    // Test that pattern matching works correctly
    def getStrategyName(strategy: HideStrategy): String = strategy match {
      case HideStrategy.ReferenceHidden => "referenceHidden"
      case HideStrategy.Escaped         => "escaped"
    }

    getStrategyName(HideStrategy.ReferenceHidden) shouldBe "referenceHidden"
    getStrategyName(HideStrategy.Escaped) shouldBe "escaped"
  }
}
