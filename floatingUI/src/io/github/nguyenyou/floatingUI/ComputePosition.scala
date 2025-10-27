package io.github.nguyenyou.floatingUI

import Types.*
import ComputeCoordsFromPlacement.*
import org.scalajs.dom

/** Main computePosition function for floating element positioning.
  *
  * Ported from @floating-ui/core/src/computePosition.ts
  */
object ComputePosition {

  def computePosition(
    reference: ReferenceElement,
    floating: dom.HTMLElement,
    config: ComputePositionConfig
  ): ComputePositionReturn = {
    val validMiddleware = config.middleware.filter(_ != null)

    // Check RTL direction from the floating element
    val rtl = config.platform.isRTL(floating)

    var rects = config.platform.getElementRects(reference, floating, config.strategy)
    var coords = computeCoordsFromPlacement(rects, config.placement, rtl)
    var statefulPlacement = config.placement
    var middlewareData = MiddlewareData()
    var resetCount = 0

    val elements = Elements(reference, floating)

    var i = 0
    while (i < validMiddleware.length && resetCount <= 50) {
      val middleware = validMiddleware(i)

      val state = MiddlewareState(
        x = coords.x,
        y = coords.y,
        initialPlacement = config.placement,
        placement = statefulPlacement,
        strategy = config.strategy,
        middlewareData = middlewareData,
        elements = elements,
        rects = rects,
        platform = config.platform
      )

      val result = middleware.fn(state)

      coords = Coords(
        x = result.x.getOrElse(coords.x),
        y = result.y.getOrElse(coords.y)
      )

      // Update middleware data
      result.data.foreach { data =>
        middlewareData = middleware.name match {
          case "arrow" =>
            val arrowData = ArrowData(
              x = data.get("x").map(_.asInstanceOf[Double]),
              y = data.get("y").map(_.asInstanceOf[Double]),
              centerOffset = data.getOrElse("centerOffset", 0.0).asInstanceOf[Double],
              alignmentOffset = data.get("alignmentOffset").map(_.asInstanceOf[Double])
            )
            middlewareData.copy(arrow = Some(arrowData))
          case "offset" =>
            val offsetData = OffsetData(
              x = data.getOrElse("x", 0.0).asInstanceOf[Double],
              y = data.getOrElse("y", 0.0).asInstanceOf[Double],
              placement = data.getOrElse("placement", statefulPlacement).asInstanceOf[Placement]
            )
            middlewareData.copy(offset = Some(offsetData))
          case "shift" =>
            val shiftData = ShiftData(
              x = data.getOrElse("x", 0.0).asInstanceOf[Double],
              y = data.getOrElse("y", 0.0).asInstanceOf[Double]
            )
            middlewareData.copy(shift = Some(shiftData))
          case "flip" =>
            val flipData = FlipData(
              index = data.get("index").map(_.asInstanceOf[Int]),
              overflows = data
                .get("overflows")
                .map(_.asInstanceOf[Seq[PlacementOverflow]])
                .getOrElse(Seq.empty)
            )
            middlewareData.copy(flip = Some(flipData))
          case _ =>
            middlewareData
        }
      }

      // Handle reset
      result.reset.foreach { reset =>
        resetCount += 1

        reset.placement.foreach { newPlacement =>
          statefulPlacement = newPlacement
        }

        // Recalculate rects if requested
        reset.rects.foreach {
          case Left(true) =>
            // Recalculate rects from platform
            rects = config.platform.getElementRects(reference, floating, config.strategy)
          case Right(newRects) =>
            // Use provided rects
            rects = newRects
          case Left(false) =>
            // Don't recalculate rects
            ()
        }

        // Recalculate coordinates with new placement and/or rects
        coords = computeCoordsFromPlacement(rects, statefulPlacement, rtl)

        // Restart middleware loop
        i = -1
      }

      i += 1
    }

    ComputePositionReturn(
      x = coords.x,
      y = coords.y,
      placement = statefulPlacement,
      strategy = config.strategy,
      middlewareData = middlewareData
    )
  }
}
