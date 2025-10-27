package io.github.nguyenyou.floatingUI.middleware

import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.Utils.*
import io.github.nguyenyou.floatingUI.DetectOverflow

/** AutoPlacement middleware - automatically chooses best placement based on available space.
  *
  * Ported from @floating-ui/core/src/middleware/autoPlacement.ts
  */
object AutoPlacementMiddleware {

  // All possible placements
  private val allPlacements: Seq[Placement] = Seq(
    "top",
    "top-start",
    "top-end",
    "right",
    "right-start",
    "right-end",
    "bottom",
    "bottom-start",
    "bottom-end",
    "left",
    "left-start",
    "left-end"
  )

  /** Get list of placements to try based on alignment and allowed placements. */
  def getPlacementList(
    alignment: Option[Alignment],
    autoAlignment: Boolean,
    allowedPlacements: Seq[Placement]
  ): Seq[Placement] = {
    val allowedPlacementsSortedByAlignment = alignment match {
      case Some(align) =>
        // Put placements with matching alignment first
        val matching = allowedPlacements.filter(p => getAlignment(p) == Some(align))
        val nonMatching = allowedPlacements.filter(p => getAlignment(p) != Some(align))
        matching ++ nonMatching
      case None =>
        // Only include base placements (no alignment)
        allowedPlacements.filter(p => getSide(p) == p)
    }

    allowedPlacementsSortedByAlignment.filter { placement =>
      alignment match {
        case Some(align) =>
          getAlignment(placement) == Some(align) ||
          (autoAlignment && getOppositeAlignmentPlacement(placement) != placement)
        case None => true
      }
    }
  }

  /** Create autoPlacement middleware. */
  def autoPlacement(options: AutoPlacementOptions = AutoPlacementOptions()): Middleware = new Middleware {
    override def name: String = "autoPlacement"

    override def fn(state: MiddlewareState): MiddlewareReturn = {
      val crossAxis = false // Default value
      val alignment = options.alignment
      val allowedPlacements = if (options.allowedPlacements.isEmpty) {
        allPlacements
      } else {
        options.allowedPlacements
      }
      val autoAlignment = options.autoAlignment

      val placements = if (alignment.isDefined || allowedPlacements == allPlacements) {
        getPlacementList(alignment, autoAlignment, allowedPlacements)
      } else {
        allowedPlacements
      }

      // Evaluate derivable padding
      val padding = evaluate(options.padding, state)

      val detectOverflowOptions = DetectOverflowOptions(
        boundary = options.boundary,
        rootBoundary = options.rootBoundary,
        padding = padding
      )

      val overflow = DetectOverflow.detectOverflow(state, Left(detectOverflowOptions))

      val currentIndex = state.middlewareData.autoPlacement.flatMap(_.index).getOrElse(0)
      val currentPlacement = if (currentIndex < placements.length) {
        Some(placements(currentIndex))
      } else {
        None
      }

      currentPlacement match {
        case None => MiddlewareReturn()
        case Some(current) =>
          val alignmentSides = getAlignmentSides(
            current,
            state.rects,
            state.platform.isRTL(state.elements.floating)
          )

          // Make computeCoords start from the right place
          if (state.placement != current) {
            return MiddlewareReturn(
              reset = Some(Right(ResetValue(placement = Some(placements.head))))
            )
          }

          // Get overflow values for current placement
          val side = getSide(current)
          val sideValue = side match {
            case "top"    => overflow.top
            case "right"  => overflow.right
            case "bottom" => overflow.bottom
            case "left"   => overflow.left
          }
          val side1Value = alignmentSides._1 match {
            case "top"    => overflow.top
            case "right"  => overflow.right
            case "bottom" => overflow.bottom
            case "left"   => overflow.left
          }
          val side2Value = alignmentSides._2 match {
            case "top"    => overflow.top
            case "right"  => overflow.right
            case "bottom" => overflow.bottom
            case "left"   => overflow.left
          }

          val currentOverflows = Seq(sideValue, side1Value, side2Value)

          val previousOverflows = state.middlewareData.autoPlacement
            .map(_.overflows)
            .getOrElse(Seq.empty)

          val allOverflows = previousOverflows :+ PlacementOverflow(
            placement = current,
            overflows = currentOverflows
          )

          val nextPlacement = if (currentIndex + 1 < placements.length) {
            Some(placements(currentIndex + 1))
          } else {
            None
          }

          // There are more placements to check
          nextPlacement match {
            case Some(next) =>
              MiddlewareReturn(
                data = Some(
                  Map(
                    "index" -> (currentIndex + 1),
                    "overflows" -> allOverflows
                  )
                ),
                reset = Some(Right(ResetValue(placement = Some(next))))
              )

            case None =>
              // All placements checked, find the best one
              val placementsSortedByMostSpace = allOverflows
                .map { d =>
                  val align = getAlignment(d.placement)
                  val overflowValue = if (align.isDefined && crossAxis) {
                    // Check along mainAxis and main crossAxis side
                    d.overflows.take(2).sum
                  } else {
                    // Check only mainAxis
                    d.overflows.head
                  }
                  (d.placement, overflowValue, d.overflows)
                }
                .sortBy(_._2)

              val placementsThatFitOnEachSide = placementsSortedByMostSpace.filter { case (placement, _, overflows) =>
                val checkCount = if (getAlignment(placement).isDefined) 2 else 3
                overflows.take(checkCount).forall(_ <= 0)
              }

              val resetPlacement = placementsThatFitOnEachSide.headOption
                .map(_._1)
                .getOrElse(placementsSortedByMostSpace.head._1)

              if (resetPlacement != state.placement) {
                MiddlewareReturn(
                  data = Some(
                    Map(
                      "index" -> (currentIndex + 1),
                      "overflows" -> allOverflows
                    )
                  ),
                  reset = Some(Right(ResetValue(placement = Some(resetPlacement))))
                )
              } else {
                MiddlewareReturn()
              }
          }
      }
    }
  }
}
