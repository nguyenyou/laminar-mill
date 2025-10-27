package io.github.nguyenyou.floatingUI

import Types.*
import org.scalajs.dom
import scala.scalajs.js
import DOMUtils.*

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
    val offsetParent = getOffsetParent(floating)
    val referenceRect = getRectRelativeToOffsetParent(reference, offsetParent, strategy)

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
    val (width, height, _) = getCssDimensions(element)
    Dimensions(width = width, height = height)
  }

  override def getClippingRect(
    element: dom.Element,
    boundary: String,
    rootBoundary: String,
    strategy: Strategy
  ): Rect = {
    DOMUtils.getClippingRect(element, boundary, rootBoundary, strategy)
  }
}
