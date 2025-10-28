package io.github.nguyenyou.floatingUI.middleware

import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.Utils.*
import io.github.nguyenyou.floatingUI.DetectOverflow

/** Size middleware - allows resizing floating element based on available space.
  *
  * Ported from @floating-ui/core/src/middleware/size.ts
  */
object SizeMiddleware {

  /** Create size middleware. */
  def size(options: Derivable[SizeOptions] = Left(SizeOptions())): Middleware = new Middleware {
    override def name: String = "size"

    override def fn(state: MiddlewareState): MiddlewareReturn = {
      val placement = state.placement
      val rects = state.rects
      val platform = state.platform
      val elements = state.elements

      // Evaluate derivable options
      val evaluatedOptions = evaluate(options, state)

      // Evaluate derivable padding
      val padding = evaluate(evaluatedOptions.padding, state)

      val detectOverflowOptions = DetectOverflowOptions(
        boundary = evaluatedOptions.boundary,
        rootBoundary = evaluatedOptions.rootBoundary,
        elementContext = evaluatedOptions.elementContext,
        altBoundary = evaluatedOptions.altBoundary,
        padding = padding
      )

      val overflow = DetectOverflow.detectOverflow(state, Left(detectOverflowOptions))
      val side = getSide(placement)
      val alignment = getAlignment(placement)
      val isYAxis = getSideAxis(placement) == "y"
      val width = rects.floating.width
      val height = rects.floating.height

      // Determine which sides to check based on placement
      val (heightSide, widthSide) = if (side == "top" || side == "bottom") {
        // For top/bottom placements:
        // heightSide is the placement side itself
        // widthSide depends on alignment and RTL
        val isRTL = platform.isRTL(elements.floating)
        val rtlAwareAlignment = if (isRTL) "start" else "end"
        val ws = if (alignment.contains(rtlAwareAlignment)) "left" else "right"
        (side, ws)
      } else {
        // For left/right placements:
        // widthSide is the placement side itself
        // heightSide depends on alignment
        val hs = if (alignment.contains("end")) "top" else "bottom"
        (hs, side)
      }

      val maximumClippingHeight = height - overflow.top - overflow.bottom
      val maximumClippingWidth = width - overflow.left - overflow.right

      // Get overflow for specific sides
      val heightSideOverflow = heightSide match {
        case "top"    => overflow.top
        case "bottom" => overflow.bottom
        case _        => 0.0
      }

      val widthSideOverflow = widthSide match {
        case "left"  => overflow.left
        case "right" => overflow.right
        case _       => 0.0
      }

      val overflowAvailableHeight = math.min(
        height - heightSideOverflow,
        maximumClippingHeight
      )

      val overflowAvailableWidth = math.min(
        width - widthSideOverflow,
        maximumClippingWidth
      )

      val noShift = state.middlewareData.shift.isEmpty

      var availableHeight = overflowAvailableHeight
      var availableWidth = overflowAvailableWidth

      // Check if shift middleware has been applied
      state.middlewareData.shift.foreach { shiftData =>
        // If shift is enabled on x-axis, use maximum clipping width
        if (shiftData.enabled.x) {
          availableWidth = maximumClippingWidth
        }
        // If shift is enabled on y-axis, use maximum clipping height
        if (shiftData.enabled.y) {
          availableHeight = maximumClippingHeight
        }
      }

      // If no shift and no alignment, center the element
      if (noShift && alignment.isEmpty) {
        val xMin = math.max(overflow.left, 0)
        val xMax = math.max(overflow.right, 0)
        val yMin = math.max(overflow.top, 0)
        val yMax = math.max(overflow.bottom, 0)

        if (isYAxis) {
          availableWidth = width - 2 * (
            if (xMin != 0 || xMax != 0) {
              xMin + xMax
            } else {
              math.max(overflow.left, overflow.right)
            }
          )
        } else {
          availableHeight = height - 2 * (
            if (yMin != 0 || yMax != 0) {
              yMin + yMax
            } else {
              math.max(overflow.top, overflow.bottom)
            }
          )
        }
      }

      // Call the apply function if provided
      evaluatedOptions.apply.foreach { applyFn =>
        val applyState = ApplyState(
          x = state.x,
          y = state.y,
          initialPlacement = state.initialPlacement,
          placement = state.placement,
          strategy = state.strategy,
          middlewareData = state.middlewareData,
          rects = state.rects,
          platform = state.platform,
          elements = state.elements,
          availableWidth = availableWidth,
          availableHeight = availableHeight
        )
        applyFn(applyState)
      }

      // Get new dimensions after apply
      val nextDimensions = platform.getDimensions(elements.floating)

      // If dimensions changed, trigger a reset to recalculate
      if (width != nextDimensions.width || height != nextDimensions.height) {
        val resetValue: Either[Boolean, ResetValue] = Right(
          ResetValue(
            rects = Some(Left(true))
          )
        )
        MiddlewareReturn(
          reset = Some(resetValue)
        )
      } else {
        MiddlewareReturn(reset = None)
      }
    }
  }
}
