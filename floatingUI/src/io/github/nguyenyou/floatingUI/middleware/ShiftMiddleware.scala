package io.github.nguyenyou.floatingUI.middleware

import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.Utils.*
import io.github.nguyenyou.floatingUI.DetectOverflow.*

/** Shift middleware - shifts the floating element to keep it in view.
  *
  * Ported from @floating-ui/core/src/middleware/shift.ts
  */
object ShiftMiddleware {

  def shift(options: Derivable[ShiftOptions] = Left(ShiftOptions())): Middleware = new Middleware {
    override def name: String = "shift"

    override def fn(state: MiddlewareState): MiddlewareReturn = {
      val x = state.x
      val y = state.y
      val placement = state.placement

      // Evaluate derivable options
      val evaluatedOptions = evaluate(options, state)

      // Extract shift-specific options with defaults
      val checkMainAxis = evaluatedOptions.mainAxis
      val checkCrossAxis = evaluatedOptions.crossAxis
      val limiter = evaluatedOptions.limiter.getOrElse(
        // Default limiter: identity function
        Limiter(
          options = (),
          fn = (s: MiddlewareState) => Coords(s.x, s.y)
        )
      )

      // Evaluate derivable padding
      val padding = evaluate(evaluatedOptions.padding, state)

      // Build DetectOverflowOptions from all the options
      val detectOverflowOptions = DetectOverflowOptions(
        boundary = evaluatedOptions.boundary,
        rootBoundary = evaluatedOptions.rootBoundary,
        elementContext = evaluatedOptions.elementContext,
        altBoundary = evaluatedOptions.altBoundary,
        padding = padding
      )

      val overflow = detectOverflow(state, Left(detectOverflowOptions))

      val crossAxis = getSideAxis(placement)
      val mainAxis = getOppositeAxis(crossAxis)

      val coords = Coords(x, y)
      var mainAxisCoord = if (mainAxis == "x") coords.x else coords.y
      var crossAxisCoord = if (crossAxis == "x") coords.x else coords.y

      if (checkMainAxis) {
        val minSide = if (mainAxis == "y") Side.Top else Side.Left
        val maxSide = if (mainAxis == "y") Side.Bottom else Side.Right

        val minValue = if (minSide == Side.Top) overflow.top else overflow.left
        val maxValue = if (maxSide == Side.Bottom) overflow.bottom else overflow.right

        val min = mainAxisCoord + minValue
        val max = mainAxisCoord - maxValue

        mainAxisCoord = clamp(min, mainAxisCoord, max)
      }

      if (checkCrossAxis) {
        val minSide = if (crossAxis == "y") Side.Top else Side.Left
        val maxSide = if (crossAxis == "y") Side.Bottom else Side.Right

        val minValue = if (minSide == Side.Top) overflow.top else overflow.left
        val maxValue = if (maxSide == Side.Bottom) overflow.bottom else overflow.right

        val min = crossAxisCoord + minValue
        val max = crossAxisCoord - maxValue

        crossAxisCoord = clamp(min, crossAxisCoord, max)
      }

      // Apply limiter function with updated coordinates
      val updatedState = state.copy(
        x = if (mainAxis == "x") mainAxisCoord else crossAxisCoord,
        y = if (mainAxis == "y") mainAxisCoord else crossAxisCoord
      )
      val limitedCoords = limiter.fn(updatedState)

      MiddlewareReturn(
        x = Some(limitedCoords.x),
        y = Some(limitedCoords.y),
        data = Some(
          Map(
            "x" -> (limitedCoords.x - x),
            "y" -> (limitedCoords.y - y),
            "enabled" -> Map(
              "x" -> (if (mainAxis == "x") checkMainAxis else checkCrossAxis),
              "y" -> (if (mainAxis == "y") checkMainAxis else checkCrossAxis)
            )
          )
        ),
        reset = None
      )
    }
  }

  /** Built-in limiter that will stop shift() at a certain point. */
  def limitShift(options: Derivable[LimitShiftOptions] = Left(LimitShiftOptions())): Limiter = {
    Limiter(
      options = options, // Store original unevaluated options
      fn = (state: MiddlewareState) => {
        // Evaluate derivable options inside fn
        val evaluatedOptions = evaluate(options, state)

        val coords = Coords(state.x, state.y)
        val placement = state.placement
        val rects = state.rects
        val middlewareData = state.middlewareData

        // Extract offset from evaluated options (already plain type, not Derivable)
        val rawOffset = evaluatedOptions.offset

        // Convert to offset values with defaults
        val computedOffset = rawOffset match {
          case Left(num) =>
            // If it's a number, use it for mainAxis and 0 for crossAxis
            LimitShiftOffsetValues(mainAxis = num, crossAxis = 0.0)
          case Right(opts) =>
            // If it's an object, merge with defaults (mainAxis: 0, crossAxis: 0)
            LimitShiftOffsetValues(
              mainAxis = opts.mainAxis,
              crossAxis = opts.crossAxis
            )
        }

        val crossAxis = getSideAxis(placement)
        val mainAxis = getOppositeAxis(crossAxis)

        var mainAxisCoord = if (mainAxis == "x") coords.x else coords.y
        var crossAxisCoord = if (crossAxis == "x") coords.x else coords.y

        if (evaluatedOptions.mainAxis) {
          val len = if (mainAxis == "y") "height" else "width"
          val refMainAxis = if (mainAxis == "x") rects.reference.x else rects.reference.y
          val floatingLen = if (len == "width") rects.floating.width else rects.floating.height
          val refLen = if (len == "width") rects.reference.width else rects.reference.height

          val limitMin = refMainAxis - floatingLen + computedOffset.mainAxis
          val limitMax = refMainAxis + refLen - computedOffset.mainAxis

          if (mainAxisCoord < limitMin) {
            mainAxisCoord = limitMin
          } else if (mainAxisCoord > limitMax) {
            mainAxisCoord = limitMax
          }
        }

        if (evaluatedOptions.crossAxis) {
          val len = if (mainAxis == "y") "width" else "height"
          val isOriginSide = originSides.contains(getSide(placement))
          val refCrossAxis = if (crossAxis == "x") rects.reference.x else rects.reference.y
          val floatingLen = if (len == "width") rects.floating.width else rects.floating.height
          val refLen = if (len == "width") rects.reference.width else rects.reference.height

          // Get offset from middleware data if available
          val offsetValue = middlewareData.offset
            .flatMap { offsetData =>
              if (crossAxis == "x") Some(offsetData.x) else Some(offsetData.y)
            }
            .getOrElse(0.0)

          val limitMin = refCrossAxis - floatingLen +
            (if (isOriginSide) offsetValue else 0.0) +
            (if (isOriginSide) 0.0 else computedOffset.crossAxis)

          val limitMax = refCrossAxis + refLen +
            (if (isOriginSide) 0.0 else offsetValue) -
            (if (isOriginSide) computedOffset.crossAxis else 0.0)

          if (crossAxisCoord < limitMin) {
            crossAxisCoord = limitMin
          } else if (crossAxisCoord > limitMax) {
            crossAxisCoord = limitMax
          }
        }

        if (mainAxis == "x") {
          Coords(x = mainAxisCoord, y = crossAxisCoord)
        } else {
          Coords(x = crossAxisCoord, y = mainAxisCoord)
        }
      }
    )
  }

  /** Helper case class for limit shift offset values. */
  private case class LimitShiftOffsetValues(
    mainAxis: Double,
    crossAxis: Double
  )
}
