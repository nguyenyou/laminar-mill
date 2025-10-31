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
      var mainAxisCoord = mainAxis match {
        case Axis.X => coords.x
        case Axis.Y => coords.y
      }
      var crossAxisCoord = crossAxis match {
        case Axis.X => coords.x
        case Axis.Y => coords.y
      }

      if (checkMainAxis) {
        val minSide = mainAxis match {
          case Axis.Y => Side.Top
          case Axis.X => Side.Left
        }
        val maxSide = mainAxis match {
          case Axis.Y => Side.Bottom
          case Axis.X => Side.Right
        }

        val minValue = if (minSide == Side.Top) overflow.top else overflow.left
        val maxValue = if (maxSide == Side.Bottom) overflow.bottom else overflow.right

        val min = mainAxisCoord + minValue
        val max = mainAxisCoord - maxValue

        mainAxisCoord = clamp(min, mainAxisCoord, max)
      }

      if (checkCrossAxis) {
        val minSide = crossAxis match {
          case Axis.Y => Side.Top
          case Axis.X => Side.Left
        }
        val maxSide = crossAxis match {
          case Axis.Y => Side.Bottom
          case Axis.X => Side.Right
        }

        val minValue = if (minSide == Side.Top) overflow.top else overflow.left
        val maxValue = if (maxSide == Side.Bottom) overflow.bottom else overflow.right

        val min = crossAxisCoord + minValue
        val max = crossAxisCoord - maxValue

        crossAxisCoord = clamp(min, crossAxisCoord, max)
      }

      // Apply limiter function with updated coordinates
      val updatedState = state.copy(
        x = mainAxis match {
          case Axis.X => mainAxisCoord
          case Axis.Y => crossAxisCoord
        },
        y = mainAxis match {
          case Axis.Y => mainAxisCoord
          case Axis.X => crossAxisCoord
        }
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
              "x" -> (mainAxis match {
                case Axis.X => checkMainAxis
                case Axis.Y => checkCrossAxis
              }),
              "y" -> (mainAxis match {
                case Axis.Y => checkMainAxis
                case Axis.X => checkCrossAxis
              })
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

        var mainAxisCoord = mainAxis match {
          case Axis.X => coords.x
          case Axis.Y => coords.y
        }
        var crossAxisCoord = crossAxis match {
          case Axis.X => coords.x
          case Axis.Y => coords.y
        }

        if (evaluatedOptions.mainAxis) {
          val len = mainAxis match {
            case Axis.Y => Length.Height
            case Axis.X => Length.Width
          }
          val refMainAxis = mainAxis match {
            case Axis.X => rects.reference.x
            case Axis.Y => rects.reference.y
          }
          val floatingLen = len match {
            case Length.Width  => rects.floating.width
            case Length.Height => rects.floating.height
          }
          val refLen = len match {
            case Length.Width  => rects.reference.width
            case Length.Height => rects.reference.height
          }

          val limitMin = refMainAxis - floatingLen + computedOffset.mainAxis
          val limitMax = refMainAxis + refLen - computedOffset.mainAxis

          if (mainAxisCoord < limitMin) {
            mainAxisCoord = limitMin
          } else if (mainAxisCoord > limitMax) {
            mainAxisCoord = limitMax
          }
        }

        if (evaluatedOptions.crossAxis) {
          val len = mainAxis match {
            case Axis.Y => Length.Width
            case Axis.X => Length.Height
          }
          val isOriginSide = originSides.contains(getSide(placement))
          val refCrossAxis = crossAxis match {
            case Axis.X => rects.reference.x
            case Axis.Y => rects.reference.y
          }
          val floatingLen = len match {
            case Length.Width  => rects.floating.width
            case Length.Height => rects.floating.height
          }
          val refLen = len match {
            case Length.Width  => rects.reference.width
            case Length.Height => rects.reference.height
          }

          // Get offset from middleware data if available
          val offsetValue = middlewareData.offset
            .flatMap { offsetData =>
              crossAxis match {
                case Axis.X => Some(offsetData.x)
                case Axis.Y => Some(offsetData.y)
              }
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

        mainAxis match {
          case Axis.X => Coords(x = mainAxisCoord, y = crossAxisCoord)
          case Axis.Y => Coords(x = crossAxisCoord, y = mainAxisCoord)
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
