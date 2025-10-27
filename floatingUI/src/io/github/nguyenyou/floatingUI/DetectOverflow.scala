package io.github.nguyenyou.floatingUI

import Types.*
import Utils.*
import DOMUtils.*
import org.scalajs.dom

/** Detects overflow of floating element relative to boundaries.
  *
  * Ported from @floating-ui/core/src/detectOverflow.ts
  */
object DetectOverflow {

  /** Detects overflow of floating element relative to boundaries.
    *
    * Resolves with an object of overflow side offsets that determine how much the element is overflowing a given clipping boundary on each
    * side.
    *   - positive = overflowing the boundary by that number of pixels
    *   - negative = how many pixels left before it will overflow
    *   - 0 = lies flush with the boundary
    *
    * @param state
    *   The middleware state
    * @param options
    *   Detection options (can be static or derivable from state)
    * @return
    *   SideObject containing overflow amounts for each side
    * @see
    *   https://floating-ui.com/docs/detectOverflow
    */
  def detectOverflow(
    state: MiddlewareState,
    options: Derivable[DetectOverflowOptions] = Left(DetectOverflowOptions())
  ): SideObject = {
    // Extract state components
    val x = state.x
    val y = state.y
    val platform = state.platform
    val rects = state.rects
    val elements = state.elements
    val strategy = state.strategy

    // Evaluate options (handle both static and function-based options)
    val evaluatedOptions = evaluate(options, state)

    val boundary = evaluatedOptions.boundary
    val rootBoundary = evaluatedOptions.rootBoundary
    val elementContext = evaluatedOptions.elementContext
    val altBoundary = evaluatedOptions.altBoundary
    val padding = evaluatedOptions.padding

    val paddingObject = getPaddingObject(padding)
    val altContext = if (elementContext == "floating") "reference" else "floating"

    // Determine which element to use based on altBoundary and elementContext
    val element = if (altBoundary) {
      if (altContext == "reference") elements.reference else elements.floating
    } else {
      if (elementContext == "floating") elements.floating else elements.reference
    }

    // Determine the actual element to use for clipping rect calculation
    // Check if element is a valid DOM element, otherwise use fallbacks
    val clippingElement: ReferenceElement = element match {
      case ve: VirtualElement =>
        // For virtual elements, check if isElement would return true
        // If not, use contextElement or getDocumentElement as fallback
        ve.contextElement match {
          case Some(ctx) => ctx
          case None      => getDocumentElement(elements.floating)
        }
      case e: dom.Element =>
        // For DOM elements, check if it's a valid element
        // In the synchronous version, we assume all DOM elements are valid
        e
    }

    // Get the clipping rect
    val clippingClientRect = rectToClientRect(
      platform.getClippingRect(
        clippingElement,
        boundary,
        rootBoundary,
        strategy
      )
    )

    // Determine the rect to use based on elementContext
    val rect = if (elementContext == "floating") {
      Rect(
        x = x,
        y = y,
        width = rects.floating.width,
        height = rects.floating.height
      )
    } else {
      rects.reference
    }

    // Get offset parent for scale calculations
    val offsetParent = getOffsetParent(elements.floating)

    // Calculate offset scale
    // Check if offsetParent is a DOM element to get its scale
    val offsetScale = offsetParent match {
      case elem: dom.Element =>
        getScale(elem)
      case _ =>
        // If offsetParent is window or not an element, use default scale
        Coords(1, 1)
    }

    // Convert rect using convertOffsetParentRelativeRectToViewportRelativeRect if needed
    // In the synchronous Scala.js version, we use the DOMUtils implementation
    val convertedRect = offsetParent match {
      case elem: dom.Element =>
        // Use the conversion function from DOMUtils
        convertOffsetParentRelativeRectToViewportRelativeRect(
          rect,
          offsetParent,
          strategy
        )
      case _ =>
        // If offsetParent is window, no conversion needed
        rect
    }

    val elementClientRect = rectToClientRect(convertedRect)

    // Calculate overflow with scale adjustments
    SideObject(
      top = (clippingClientRect.top - elementClientRect.top + paddingObject.top) / offsetScale.y,
      bottom = (elementClientRect.bottom - clippingClientRect.bottom + paddingObject.bottom) / offsetScale.y,
      left = (clippingClientRect.left - elementClientRect.left + paddingObject.left) / offsetScale.x,
      right = (elementClientRect.right - clippingClientRect.right + paddingObject.right) / offsetScale.x
    )
  }
}
