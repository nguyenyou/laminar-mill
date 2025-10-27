package io.github.nguyenyou.floatingUI.middleware

import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.Utils.*
import io.github.nguyenyou.floatingUI.DetectOverflow.*

/** Flip middleware - flips the placement to keep it in view.
  *
  * Ported from @floating-ui/core/src/middleware/flip.ts
  */
object FlipMiddleware {

  def flip(options: FlipOptions = FlipOptions()): Middleware = new Middleware {
    override def name: String = "flip"

    override def fn(state: MiddlewareState): MiddlewareReturn = {
      // If arrow caused an alignment offset, skip flip logic
      // https://github.com/floating-ui/floating-ui/issues/2549#issuecomment-1719601643
      if (state.middlewareData.arrow.flatMap(_.alignmentOffset).isDefined) {
        return MiddlewareReturn()
      }

      val side = getSide(state.placement)
      val initialSideAxis = getSideAxis(state.initialPlacement)
      val isBasePlacement = getSide(state.initialPlacement) == state.initialPlacement
      val rtl = state.platform.isRTL(state.elements.floating)

      // Determine checkMainAxis and checkCrossAxis
      val checkMainAxis = options.mainAxis
      val checkCrossAxis = options.crossAxis

      // Build fallback placements
      val specifiedFallbackPlacements = options.fallbackPlacements
      val fallbackPlacements = specifiedFallbackPlacements.getOrElse {
        if (isBasePlacement || !options.flipAlignment) {
          Seq(getOppositePlacement(state.initialPlacement))
        } else {
          getExpandedPlacements(state.initialPlacement)
        }
      }

      // Add opposite axis placements if fallbackAxisSideDirection is set
      val hasFallbackAxisSideDirection = options.fallbackAxisSideDirection != "none"
      val allFallbackPlacements = if (specifiedFallbackPlacements.isEmpty && hasFallbackAxisSideDirection) {
        fallbackPlacements ++ getOppositeAxisPlacements(
          state.initialPlacement,
          options.flipAlignment,
          options.fallbackAxisSideDirection,
          rtl
        )
      } else {
        fallbackPlacements
      }

      val placements = state.initialPlacement +: allFallbackPlacements

      // Evaluate derivable padding
      val padding = evaluate(options.padding, state)

      // Detect overflow
      val overflow = detectOverflow(
        state,
        Left(DetectOverflowOptions(padding = padding))
      )

      // Build overflows array
      val overflows = scala.collection.mutable.ArrayBuffer[Double]()

      if (checkMainAxis) {
        val sideValue = side match {
          case "top"    => overflow.top
          case "bottom" => overflow.bottom
          case "left"   => overflow.left
          case "right"  => overflow.right
        }
        overflows += sideValue
      }

      // Handle crossAxis - can be Boolean or "alignment"
      val shouldCheckCrossAxis = checkCrossAxis match {
        case b: Boolean => b
        case s: String  => true // "alignment" or any string means true
        case _          => true
      }

      if (shouldCheckCrossAxis) {
        val sides = getAlignmentSides(state.placement, state.rects, rtl)
        val side1Value = sides._1 match {
          case "top"    => overflow.top
          case "bottom" => overflow.bottom
          case "left"   => overflow.left
          case "right"  => overflow.right
        }
        val side2Value = sides._2 match {
          case "top"    => overflow.top
          case "bottom" => overflow.bottom
          case "left"   => overflow.left
          case "right"  => overflow.right
        }
        overflows += side1Value
        overflows += side2Value
      }

      // Get existing overflowsData or create new
      var overflowsData = state.middlewareData.flip.map(_.overflows).getOrElse(Seq.empty)

      // Add current placement overflow data
      overflowsData = overflowsData :+ PlacementOverflow(
        placement = state.placement,
        overflows = overflows.toSeq
      )

      // Check if any side is overflowing
      if (!overflows.forall(_ <= 0)) {
        val nextIndex = state.middlewareData.flip.flatMap(_.index).getOrElse(0) + 1
        val nextPlacement = if (nextIndex < placements.length) Some(placements(nextIndex)) else None

        if (nextPlacement.isDefined) {
          // Check if we should ignore cross-axis overflow for alignment mode
          val ignoreCrossAxisOverflow = checkCrossAxis match {
            case s: String if s == "alignment" =>
              initialSideAxis != getSideAxis(nextPlacement.get)
            case _ => false
          }

          // Determine if we should try the next placement
          val shouldTryNext = !ignoreCrossAxisOverflow || {
            // We leave the current main axis only if every placement on that axis overflows the main axis
            overflowsData.forall { d =>
              if (getSideAxis(d.placement) == initialSideAxis) {
                d.overflows.headOption.exists(_ > 0)
              } else {
                true
              }
            }
          }

          if (shouldTryNext) {
            // Try next placement and re-run the lifecycle
            return MiddlewareReturn(
              data = Some(
                Map(
                  "index" -> nextIndex,
                  "overflows" -> overflowsData
                )
              ),
              reset = Some(Right(ResetValue(placement = nextPlacement)))
            )
          }
        }

        // No more placements to try - find best placement
        // First, find candidates that fit on the main axis, sorted by cross-axis overflow
        var resetPlacement = overflowsData
          .filter(d => d.overflows.headOption.exists(_ <= 0))
          .sortBy(d => d.overflows.lift(1).getOrElse(0.0))
          .headOption
          .map(_.placement)

        // If no placement fits on main axis, use fallback strategy
        if (resetPlacement.isEmpty) {
          options.fallbackStrategy match {
            case "bestFit" =>
              // Calculate total positive overflow for each placement
              val placementWithOverflow = overflowsData
                .filter { d =>
                  if (hasFallbackAxisSideDirection) {
                    val currentSideAxis = getSideAxis(d.placement)
                    // Create a bias to the y axis due to horizontal reading directions
                    currentSideAxis == initialSideAxis || currentSideAxis == "y"
                  } else {
                    true
                  }
                }
                .map { d =>
                  val totalOverflow = d.overflows.filter(_ > 0).sum
                  (d.placement, totalOverflow)
                }
                .sortBy(_._2)
                .headOption

              resetPlacement = placementWithOverflow.map(_._1)

            case "initialPlacement" =>
              resetPlacement = Some(state.initialPlacement)

            case _ =>
              resetPlacement = Some(state.initialPlacement)
          }
        }

        // If we found a different placement, reset to it
        if (resetPlacement.isDefined && state.placement != resetPlacement.get) {
          return MiddlewareReturn(
            data = Some(Map("overflows" -> overflowsData)),
            reset = Some(Right(ResetValue(placement = resetPlacement)))
          )
        }
      }

      // No overflow or same placement - return empty with overflow data
      MiddlewareReturn(
        data = if (overflowsData.nonEmpty) Some(Map("overflows" -> overflowsData)) else None
      )
    }
  }
}
