package io.github.nguyenyou.floatingUI

import Types.*
import Utils.*

/** Detects overflow of floating element relative to boundaries.
  *
  * Ported from @floating-ui/core/src/detectOverflow.ts
  */
object DetectOverflow {

  def detectOverflow(
    state: MiddlewareState,
    options: DetectOverflowOptions = DetectOverflowOptions()
  ): SideObject = {
    val paddingObject = getPaddingObject(options.padding)
    val altContext = if (options.elementContext == "floating") "reference" else "floating"
    val element = if (options.altBoundary) {
      if (altContext == "reference") state.elements.reference else state.elements.floating
    } else {
      if (options.elementContext == "floating") state.elements.floating else state.elements.reference
    }

    val clippingClientRect = rectToClientRect(
      state.platform.getClippingRect(
        element,
        options.boundary,
        options.rootBoundary,
        state.strategy
      )
    )

    val rect = if (options.elementContext == "floating") {
      Rect(
        x = state.x,
        y = state.y,
        width = state.rects.floating.width,
        height = state.rects.floating.height
      )
    } else {
      state.rects.reference
    }

    val elementClientRect = rectToClientRect(rect)

    SideObject(
      top = (clippingClientRect.top - elementClientRect.top + paddingObject.top),
      bottom = (elementClientRect.bottom - clippingClientRect.bottom + paddingObject.bottom),
      left = (clippingClientRect.left - elementClientRect.left + paddingObject.left),
      right = (elementClientRect.right - clippingClientRect.right + paddingObject.right)
    )
  }
}
