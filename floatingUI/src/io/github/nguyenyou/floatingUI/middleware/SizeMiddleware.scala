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
  def size(options: SizeOptions = SizeOptions()): Middleware = new Middleware {
    override def name: String = "size"

    override def fn(state: MiddlewareState): MiddlewareReturn = {
      val placement = state.placement
      val rects = state.rects
      val platform = state.platform
      val elements = state.elements

      // Evaluate derivable padding
      val padding = evaluate(options.padding, state)

      val detectOverflowOptions = DetectOverflowOptions(
        boundary = options.boundary,
        rootBoundary = options.rootBoundary,
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
        val ws = if (alignment.contains("end")) {
          if (platform.isRTL(elements.floating)) "left" else "right"
        } else if (alignment.contains("start")) {
          if (platform.isRTL(elements.floating)) "right" else "left"
        } else {
          "right"
        }
        (side, ws)
      } else {
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
        if (shiftData.x != 0.0) {
          availableWidth = maximumClippingWidth
        }
        // If shift is enabled on y-axis, use maximum clipping height
        if (shiftData.y != 0.0) {
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
      options.apply.foreach { applyFn =>
        applyFn(state, availableWidth, availableHeight)
      }

      // Get new dimensions after apply
      val nextDimensions = platform.getDimensions(elements.floating)

      // If dimensions changed, trigger a reset to recalculate
      if (width != nextDimensions.width || height != nextDimensions.height) {
        MiddlewareReturn(
          reset = Some(
            ResetValue(
              rects = Some(Left(true))
            )
          )
        )
      } else {
        MiddlewareReturn()
      }
    }
  }
}
