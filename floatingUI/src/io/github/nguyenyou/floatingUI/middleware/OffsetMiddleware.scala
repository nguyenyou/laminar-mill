package io.github.nguyenyou.floatingUI.middleware

import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.Utils.*

/** Offset middleware - shifts the floating element from its reference element.
  *
  * Ported from @floating-ui/core/src/middleware/offset.ts
  */
object OffsetMiddleware {

  def offset(options: OffsetOptions = Left(Left(0))): Middleware = new Middleware {
    override def name: String = "offset"

    override def fn(state: MiddlewareState): MiddlewareReturn = {
      val diffCoords = convertValueToCoords(state, options)

      // If the placement is the same and the arrow caused an alignment offset
      // then we don't need to change the positioning coordinates.
      val skipOffset = state.middlewareData.offset.exists(_.placement == state.placement) &&
        state.middlewareData.arrow.flatMap(_.alignmentOffset).isDefined

      if (skipOffset) {
        MiddlewareReturn(reset = None)
      } else {
        MiddlewareReturn(
          x = Some(state.x + diffCoords.x),
          y = Some(state.y + diffCoords.y),
          data = Some(
            Map(
              "x" -> diffCoords.x,
              "y" -> diffCoords.y,
              "placement" -> state.placement
            )
          ),
          reset = None
        )
      }
    }
  }

  private def convertValueToCoords(
    state: MiddlewareState,
    options: OffsetOptions
  ): Coords = {
    val side = getSide(state.placement)
    val alignment = getAlignment(state.placement)
    val isVertical = getSideAxis(state.placement) == "y"
    val rtl = state.platform.isRTL(state.elements.floating)
    val mainAxisMulti = if (originSides.contains(side)) -1 else 1
    val crossAxisMulti = if (rtl && isVertical) -1 else 1

    // Evaluate derivable options to get the raw value (number or object)
    val rawValue = evaluate(options, state)

    // Extract mainAxis, crossAxis, and alignmentAxis based on whether it's a number or object
    val (mainAxisValue, crossAxisValue, alignmentAxisValue) = rawValue match {
      case Left(number) =>
        // Number shorthand - use as mainAxis, crossAxis = 0, alignmentAxis = None
        (number, 0.0, None)
      case Right(obj) =>
        // Object form - extract fields
        (obj.mainAxis, obj.crossAxis, obj.alignmentAxis)
    }

    // Apply alignment axis override if present
    var mainAxis = mainAxisValue
    var crossAxis = crossAxisValue

    if (alignment.isDefined && alignmentAxisValue.isDefined) {
      crossAxis = if (alignment.get == "end") alignmentAxisValue.get * -1 else alignmentAxisValue.get
    }

    if (isVertical) {
      Coords(x = crossAxis * crossAxisMulti, y = mainAxis * mainAxisMulti)
    } else {
      Coords(x = mainAxis * mainAxisMulti, y = crossAxis * crossAxisMulti)
    }
  }
}
