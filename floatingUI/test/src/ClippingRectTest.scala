package io.github.nguyenyou.floatingUI

import org.scalajs.dom
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.DOMUtils.*

/** Tests for clipping rect calculation.
  *
  * IMPORTANT: These tests run in jsdom, which has NO layout engine.
  *
  * What jsdom CANNOT do:
  *   - Calculate element positions or dimensions (getBoundingClientRect always returns zeros)
  *   - Perform layout calculations (offsetWidth, offsetHeight, clientWidth, etc. are 0 or undefined)
  *   - Compute viewport dimensions that affect layout
  *
  * What these tests DO validate:
  *   - Functions execute without crashing
  *   - Return values have correct structure and types
  *   - Cache behavior works correctly
  *   - Error handling for edge cases (invalid selectors, etc.)
  *
  * What these tests DO NOT validate:
  *   - Actual positioning calculations (requires real browser)
  *   - Clipping rect dimensions match expected values
  *   - Layout-dependent behavior
  *
  * For real positioning tests, use browser-based integration tests (Playwright/Puppeteer).
  *
  * References:
  *   - jsdom issue #135: https://github.com/jsdom/jsdom/issues/135
  *   - jsdom issue #1590: https://github.com/jsdom/jsdom/issues/1590
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

      // jsdom has no layout engine - getBoundingClientRect always returns zeros
      // This test validates structure and error-free execution, NOT positioning behavior
      assert(result.width == 0, "jsdom always returns 0 for width (no layout engine)")
      assert(result.height == 0, "jsdom always returns 0 for height (no layout engine)")
      assert(result.x == 0, "jsdom always returns 0 for x")
      assert(result.y == 0, "jsdom always returns 0 for y")

      // Verify the function returns a valid Rect structure
      result shouldBe a[Rect]
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

      // jsdom has no layout engine - dimensions are always 0
      // This validates the function executes without error and returns correct structure
      assert(result.width == 0, "jsdom always returns 0 for width (no layout engine)")
      assert(result.height == 0, "jsdom always returns 0 for height (no layout engine)")
      result shouldBe a[Rect]
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

      // jsdom has no layout engine - dimensions are always 0
      // This validates the function executes without error and returns correct structure
      assert(result.width == 0, "jsdom always returns 0 for width (no layout engine)")
      assert(result.height == 0, "jsdom always returns 0 for height (no layout engine)")
      result shouldBe a[Rect]
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
      // This validates error handling, not positioning (jsdom has no layout engine)
      assert(result.width == 0, "jsdom always returns 0 for width")
      assert(result.height == 0, "jsdom always returns 0 for height")
      result shouldBe a[Rect]
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

      // This validates the algorithm for finding clipping ancestors executes without error
      // jsdom has no layout engine, so dimensions are always 0
      // In a real browser, this would test that nested overflow containers are properly detected
      assert(result.width == 0, "jsdom always returns 0 for width (no layout engine)")
      assert(result.height == 0, "jsdom always returns 0 for height (no layout engine)")
      result shouldBe a[Rect]

      // Verify cache was populated (validates ancestor detection logic)
      assert(cache.nonEmpty, "Cache should contain clipping ancestor entries")
    } finally {
      dom.document.body.removeChild(outer)
    }
  }
}
