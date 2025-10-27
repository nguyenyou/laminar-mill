package io.github.nguyenyou.floatingUI.middleware

import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.Utils.*
import io.github.nguyenyou.floatingUI.DetectOverflow.*

/** Shift middleware - shifts the floating element to keep it in view.
  *
  * Ported from @floating-ui/core/src/middleware/shift.ts
  */
object ShiftMiddleware {

  def shift(options: ShiftOptions = ShiftOptions()): Middleware = new Middleware {
    override def name: String = "shift"

    override def fn(state: MiddlewareState): MiddlewareReturn = {
      val coords = Coords(state.x, state.y)
      val overflow = detectOverflow(
        state,
        DetectOverflowOptions(padding = options.padding)
      )

      val crossAxis = getSideAxis(getSide(state.placement))
      val mainAxis = getOppositeAxis(crossAxis)

      var mainAxisCoord = if (mainAxis == "x") coords.x else coords.y
      var crossAxisCoord = if (crossAxis == "x") coords.x else coords.y

      if (options.mainAxis) {
        val minSide = if (mainAxis == "y") "top" else "left"
        val maxSide = if (mainAxis == "y") "bottom" else "right"

        val minValue = if (minSide == "top") overflow.top else overflow.left
        val maxValue = if (maxSide == "bottom") overflow.bottom else overflow.right

        val min = mainAxisCoord + minValue
        val max = mainAxisCoord - maxValue

        mainAxisCoord = clamp(min, mainAxisCoord, max)
      }

      if (options.crossAxis) {
        val minSide = if (crossAxis == "y") "top" else "left"
        val maxSide = if (crossAxis == "y") "bottom" else "right"

        val minValue = if (minSide == "top") overflow.top else overflow.left
        val maxValue = if (maxSide == "bottom") overflow.bottom else overflow.right

        val min = crossAxisCoord + minValue
        val max = crossAxisCoord - maxValue

        crossAxisCoord = clamp(min, crossAxisCoord, max)
      }

      val limitedCoords = if (mainAxis == "x") {
        Coords(x = mainAxisCoord, y = crossAxisCoord)
      } else {
        Coords(x = crossAxisCoord, y = mainAxisCoord)
      }

      MiddlewareReturn(
        x = Some(limitedCoords.x),
        y = Some(limitedCoords.y),
        data = Some(
          Map(
            "x" -> (limitedCoords.x - state.x),
            "y" -> (limitedCoords.y - state.y),
            "enabled" -> Map(
              mainAxis -> options.mainAxis,
              crossAxis -> options.crossAxis
            )
          )
        )
      )
    }
  }
}
