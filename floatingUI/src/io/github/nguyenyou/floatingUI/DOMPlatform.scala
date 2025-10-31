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
    reference: ReferenceElement,
    floating: dom.HTMLElement,
    strategy: Strategy
  ): ElementRects = {
    val floatingRect = floating.getBoundingClientRect()
    val offsetParent = DOMUtils.getOffsetParent(floating)

    // Get reference rect - handle both DOM elements and virtual elements
    val referenceRect = reference match {
      case ve: VirtualElement =>
        // For virtual elements, use getBoundingClientRect directly
        val rect = ve.getBoundingClientRect()
        Rect(rect.x, rect.y, rect.width, rect.height)
      case e: dom.Element =>
        // For DOM elements, use the standard method
        getRectRelativeToOffsetParent(e, offsetParent, strategy)
    }

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
    element: Any,
    boundary: Boundary,
    rootBoundary: RootBoundary,
    strategy: Strategy
  ): Rect = {
    // For clipping rect, we need a DOM element
    // For virtual elements, use the context element or fall back to document.body
    val domElement = element match {
      case ve: VirtualElement =>
        ve.contextElement.getOrElse(dom.document.body)
      case e: dom.Element =>
        e
      case _ =>
        // Fallback for any other type
        dom.document.body
    }
    // Pass the cache from the platform's _c field
    DOMUtils.getClippingRect(domElement, boundary, rootBoundary, strategy, _c)
  }

  // Optional platform methods

  override def convertOffsetParentRelativeRectToViewportRelativeRect(
    elements: Option[Elements],
    rect: Rect,
    offsetParent: Any,
    strategy: Strategy
  ): Option[Rect] = {
    // Only convert if offsetParent is a DOM element
    offsetParent match {
      case elem: dom.Element =>
        Some(DOMUtils.convertOffsetParentRelativeRectToViewportRelativeRect(rect, elem, strategy))
      case _ =>
        // If offsetParent is window or not an element, no conversion needed
        None
    }
  }

  override def getOffsetParent(element: Any): Option[Any] = {
    element match {
      case elem: dom.Element =>
        Some(DOMUtils.getOffsetParent(elem))
      case _ =>
        None
    }
  }

  override def isElement(value: Any): Option[Boolean] = {
    Some(value.isInstanceOf[dom.Element])
  }

  override def getDocumentElement(element: Any): Option[Any] = {
    element match {
      case elem: dom.Node =>
        Some(Utils.getDocumentElement(elem))
      case _ =>
        None
    }
  }

  override def getScale(element: Any): Option[Coords] = {
    element match {
      case elem: dom.Element =>
        Some(DOMUtils.getScale(elem))
      case _ =>
        None
    }
  }

  override def getClientRects(element: ReferenceElement): Seq[ClientRectObject] = {
    element match {
      case ve: VirtualElement =>
        // For virtual elements, use getClientRects if available, otherwise use getBoundingClientRect
        ve.getClientRects().getOrElse(Seq(ve.getBoundingClientRect()))
      case e: dom.Element =>
        // For DOM elements, use the native getClientRects
        val rects = e.getClientRects()
        (0 until rects.length).map { i =>
          val rect = rects(i)
          ClientRectObject(
            x = rect.left,
            y = rect.top,
            width = rect.width,
            height = rect.height,
            top = rect.top,
            right = rect.right,
            bottom = rect.bottom,
            left = rect.left
          )
        }
    }
  }
}
