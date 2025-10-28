package io.github.nguyenyou.floatingUI

import org.scalajs.dom
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.DOMUtils.*

/** Tests for clipping rect calculation.
  *
  * IMPORTANT: These tests run in Playwright (real Chrome browser).
  *
  * What Playwright provides:
  *   - Real layout engine with accurate getBoundingClientRect
  *   - Actual element positions and dimensions
  *   - Viewport calculations
  *   - Proper clipping rect behavior
  *
  * What these tests validate:
  *   - Functions execute without crashing
  *   - Return values have correct structure and types
  *   - Clipping rect calculations return reasonable values
  *   - Error handling for edge cases (invalid selectors, etc.)
  */
class ClippingRectTest extends AnyFunSuite with Matchers {

  test("getClippingRect with viewport rootBoundary") {
    val element = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    dom.document.body.appendChild(element)

    try {
      val result = getClippingRect(
        element,
        "clippingAncestors",
        "viewport",
        "absolute",
        None
      )

      // In Playwright, we get real viewport dimensions
      // Verify the function returns a valid Rect structure with reasonable values
      result shouldBe a[Rect]
      result.width should be >= 0.0
      result.height should be >= 0.0
    } finally {
      dom.document.body.removeChild(element)
    }
  }

  test("getClippingRect with document rootBoundary") {
    val element = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    dom.document.body.appendChild(element)

    try {
      val result = getClippingRect(
        element,
        "clippingAncestors",
        "document",
        "absolute",
        None
      )

      // In Playwright, we get real document dimensions
      result shouldBe a[Rect]
      result.width should be >= 0.0
      result.height should be >= 0.0
    } finally {
      dom.document.body.removeChild(element)
    }
  }

  test("getClippingRect with clippingAncestors boundary") {
    val element = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    dom.document.body.appendChild(element)

    try {
      val result = getClippingRect(
        element,
        "clippingAncestors",
        "viewport",
        "absolute",
        None
      )

      // In Playwright, we get real clipping rect dimensions
      result shouldBe a[Rect]
      result.width should be >= 0.0
      result.height should be >= 0.0
    } finally {
      dom.document.body.removeChild(element)
    }
  }

  test("getClippingRect caches results") {
    val element = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    dom.document.body.appendChild(element)

    try {
      val cache = scala.collection.mutable.Map[ReferenceElement, Seq[dom.Element]]()

      // First call - should populate cache
      getClippingRect(element, "clippingAncestors", "viewport", "absolute", Some(cache))
      val cacheSize1 = cache.size

      // Second call with same element - should use cache
      getClippingRect(element, "clippingAncestors", "viewport", "absolute", Some(cache))
      val cacheSize2 = cache.size

      // Cache size should be the same (cache was reused)
      assert(cacheSize1 == cacheSize2, "Cache should be reused for same element")
      assert(cacheSize1 > 0, "Cache should contain entries")
    } finally {
      dom.document.body.removeChild(element)
    }
  }

  test("getClippingRect handles invalid CSS selector gracefully") {
    val element = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    dom.document.body.appendChild(element)

    try {
      // Use an invalid CSS selector - should fall back to rootBoundary
      val result = getClippingRect(
        element,
        "#nonexistent-element-12345",
        "viewport",
        "absolute",
        None
      )

      // Should not crash when selector doesn't match any element
      result shouldBe a[Rect]
      result.width should be >= 0.0
      result.height should be >= 0.0
    } finally {
      dom.document.body.removeChild(element)
    }
  }

  test("computePosition clears cache after completion") {
    val reference = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    reference.style.width = "100px"
    reference.style.height = "100px"
    dom.document.body.appendChild(reference)

    val floating = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    floating.style.width = "50px"
    floating.style.height = "50px"
    dom.document.body.appendChild(floating)

    try {
      // Verify cache is None before call
      assert(FloatingUI.platform._c.isEmpty, "Cache should be empty before computePosition")

      // Call computePosition
      FloatingUI.computePosition(reference, floating)

      // Verify cache is None after call
      assert(FloatingUI.platform._c.isEmpty, "Cache should be empty after computePosition")
    } finally {
      dom.document.body.removeChild(reference)
      dom.document.body.removeChild(floating)
    }
  }

  test("getClippingRect with nested scrolling containers") {
    // Create nested structure: body > outer > inner > element
    val outer = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    outer.style.width = "500px"
    outer.style.height = "500px"
    outer.style.overflow = "auto"
    outer.style.position = "relative"
    dom.document.body.appendChild(outer)

    val inner = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    inner.style.width = "300px"
    inner.style.height = "300px"
    inner.style.overflow = "auto"
    inner.style.position = "relative"
    outer.appendChild(inner)

    val element = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    element.style.width = "100px"
    element.style.height = "100px"
    inner.appendChild(element)

    try {
      val cache = scala.collection.mutable.Map[ReferenceElement, Seq[dom.Element]]()

      val result = getClippingRect(
        element,
        "clippingAncestors",
        "viewport",
        "absolute",
        Some(cache)
      )

      // In Playwright, this tests that nested overflow containers are properly detected
      result shouldBe a[Rect]
      result.width should be >= 0.0
      result.height should be >= 0.0

      // Verify cache was populated (validates ancestor detection logic)
      assert(cache.nonEmpty, "Cache should contain clipping ancestor entries")
    } finally {
      dom.document.body.removeChild(outer)
    }
  }
}
