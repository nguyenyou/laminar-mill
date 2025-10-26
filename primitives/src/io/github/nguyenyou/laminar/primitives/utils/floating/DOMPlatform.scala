package io.github.nguyenyou.laminar.primitives.utils.floating

import Types.*
import org.scalajs.dom
import scala.scalajs.js

/** DOM platform implementation for floating element positioning.
  * 
  * Ported from @floating-ui/dom/src/platform
  */
object DOMPlatform extends Platform {
  
  override def getElementRects(
    reference: dom.Element,
    floating: dom.HTMLElement,
    strategy: Strategy
  ): ElementRects = {
    val floatingRect = floating.getBoundingClientRect()
    val referenceRect = getRectRelativeToOffsetParent(reference, getOffsetParent(floating), strategy)
    
    ElementRects(
      reference = referenceRect,
      floating = Rect(
        x = 0,
        y = 0,
        width = floatingRect.width,
        height = floatingRect.height
      )
    )
  }
  
  override def getDimensions(element: dom.Element): Dimensions = {
    val rect = element.getBoundingClientRect()
    Dimensions(width = rect.width, height = rect.height)
  }
  
  override def getClippingRect(
    element: dom.Element,
    boundary: String,
    rootBoundary: String,
    strategy: Strategy
  ): Rect = {
    // Simplified implementation - returns viewport rect
    // Full implementation would handle clipping ancestors
    if (rootBoundary == "viewport") {
      val win = dom.window
      Rect(
        x = 0,
        y = 0,
        width = win.innerWidth,
        height = win.innerHeight
      )
    } else {
      val doc = dom.document.documentElement
      Rect(
        x = 0,
        y = 0,
        width = doc.scrollWidth,
        height = doc.scrollHeight
      )
    }
  }
  
  // ============================================================================
  // Helper Functions
  // ============================================================================
  
  private def getOffsetParent(element: dom.HTMLElement): dom.Element = {
    var offsetParent = element.offsetParent
    
    // Handle null offsetParent (e.g., fixed positioned elements)
    if (offsetParent == null) {
      return dom.document.documentElement
    }
    
    offsetParent
  }
  
  private def getRectRelativeToOffsetParent(
    reference: dom.Element,
    offsetParent: dom.Element,
    strategy: Strategy
  ): Rect = {
    val referenceRect = reference.getBoundingClientRect()
    
    if (strategy == "fixed") {
      // For fixed strategy, use viewport-relative coordinates
      Rect(
        x = referenceRect.left,
        y = referenceRect.top,
        width = referenceRect.width,
        height = referenceRect.height
      )
    } else {
      // For absolute strategy, calculate relative to offset parent
      val offsetParentRect = offsetParent.getBoundingClientRect()
      
      Rect(
        x = referenceRect.left - offsetParentRect.left,
        y = referenceRect.top - offsetParentRect.top,
        width = referenceRect.width,
        height = referenceRect.height
      )
    }
  }
}

