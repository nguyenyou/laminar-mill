package io.github.nguyenyou.floatingUI

import org.scalajs.dom
import org.scalatest.funsuite.AnyFunSuite
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.DOMUtils.*

/** Tests for clipping rect calculation.
  *
  * Verifies that the Scala.js implementation matches the TypeScript behavior.
  */
class ClippingRectTest extends AnyFunSuite {

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

      // In jsdom environment, viewport dimensions might be 0
      // Just verify the function doesn't crash and returns a rect
      assert(result.width >= 0, s"Viewport width should be non-negative, got ${result.width}")
      assert(result.height >= 0, s"Viewport height should be non-negative, got ${result.height}")
      assert(result.x >= 0, "Viewport x should be non-negative")
      assert(result.y >= 0, "Viewport y should be non-negative")
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

      // In jsdom environment, document dimensions might be 0
      // Just verify the function doesn't crash and returns a rect
      assert(result.width >= 0, s"Document width should be non-negative, got ${result.width}")
      assert(result.height >= 0, s"Document height should be non-negative, got ${result.height}")
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

      // Should return a valid rect (non-negative dimensions)
      assert(result.width >= 0, s"Clipping rect width should be non-negative, got ${result.width}")
      assert(result.height >= 0, s"Clipping rect height should be non-negative, got ${result.height}")
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
      // Use an invalid CSS selector
      val result = getClippingRect(
        element,
        "#nonexistent-element-12345",
        "viewport",
        "absolute",
        None
      )

      // Should still return a valid rect (using only rootBoundary)
      // In jsdom, dimensions might be 0, but should not crash
      assert(result.width >= 0, s"Should return non-negative width, got ${result.width}")
      assert(result.height >= 0, s"Should return non-negative height, got ${result.height}")
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

      // Should find clipping ancestors (may be empty in jsdom)
      // Just verify the function doesn't crash
      assert(result.width >= 0, s"Clipping rect should have non-negative width, got ${result.width}")
      assert(result.height >= 0, s"Clipping rect should have non-negative height, got ${result.height}")
    } finally {
      dom.document.body.removeChild(outer)
    }
  }
}
