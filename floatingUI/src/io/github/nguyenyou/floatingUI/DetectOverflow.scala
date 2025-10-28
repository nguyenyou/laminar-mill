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
    // Matches TypeScript: evaluate(options, state)
    val evaluatedOptions = evaluate(options, state)

    val boundary = evaluatedOptions.boundary
    val rootBoundary = evaluatedOptions.rootBoundary
    val elementContext = evaluatedOptions.elementContext
    val altBoundary = evaluatedOptions.altBoundary
    val padding = evaluatedOptions.padding

    val paddingObject = getPaddingObject(padding)
    val altContext = if (elementContext == "floating") "reference" else "floating"

    // Determine which element to use based on altBoundary and elementContext
    // Matches TypeScript: elements[altBoundary ? altContext : elementContext]
    val element = if (altBoundary) {
      if (altContext == "reference") elements.reference else elements.floating
    } else {
      if (elementContext == "floating") elements.floating else elements.reference
    }

    // Determine the actual element to use for clipping rect calculation
    // Matches TypeScript: (await platform.isElement?.(element)) ?? true ? element : element.contextElement || (await
    // platform.getDocumentElement?.(elements.floating))
    val isElementResult = platform.isElement(element).getOrElse(true)
    val clippingElement: Any = if (isElementResult) {
      element
    } else {
      element match {
        case ve: VirtualElement =>
          ve.contextElement.getOrElse(
            platform.getDocumentElement(elements.floating).getOrElse(elements.floating)
          )
        case _ =>
          platform.getDocumentElement(elements.floating).getOrElse(elements.floating)
      }
    }

    // Get the clipping rect
    // Matches TypeScript: platform.getClippingRect({element, boundary, rootBoundary, strategy})
    val clippingClientRect = rectToClientRect(
      platform.getClippingRect(
        clippingElement,
        boundary,
        rootBoundary,
        strategy
      )
    )

    // Determine the rect to use based on elementContext
    // Matches TypeScript: elementContext === 'floating' ? {x, y, width: rects.floating.width, height: rects.floating.height} :
    // rects.reference
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
    // Matches TypeScript: await platform.getOffsetParent?.(elements.floating)
    val offsetParent = platform.getOffsetParent(elements.floating).getOrElse(null)

    // Calculate offset scale
    // Matches TypeScript: (await platform.isElement?.(offsetParent)) ? (await platform.getScale?.(offsetParent)) || {x: 1, y: 1} : {x: 1, y:
    // 1}
    val offsetScale = {
      val isOffsetParentElement = platform.isElement(offsetParent).getOrElse(false)
      if (isOffsetParentElement) {
        platform.getScale(offsetParent).getOrElse(Coords(1, 1))
      } else {
        Coords(1, 1)
      }
    }

    // Convert rect using convertOffsetParentRelativeRectToViewportRelativeRect if available
    // Matches TypeScript: platform.convertOffsetParentRelativeRectToViewportRelativeRect ? await
    // platform.convertOffsetParentRelativeRectToViewportRelativeRect({elements, rect, offsetParent, strategy}) : rect
    val convertedRect = platform
      .convertOffsetParentRelativeRectToViewportRelativeRect(
        Some(elements),
        rect,
        offsetParent,
        strategy
      )
      .getOrElse(rect)

    val elementClientRect = rectToClientRect(convertedRect)

    // Calculate overflow with scale adjustments
    // Matches TypeScript lines 101-118
    SideObject(
      top = (clippingClientRect.top - elementClientRect.top + paddingObject.top) / offsetScale.y,
      bottom = (elementClientRect.bottom - clippingClientRect.bottom + paddingObject.bottom) / offsetScale.y,
      left = (clippingClientRect.left - elementClientRect.left + paddingObject.left) / offsetScale.x,
      right = (elementClientRect.right - clippingClientRect.right + paddingObject.right) / offsetScale.x
    )
  }
}
