package io.github.nguyenyou.laminar.primitives.utils.floating

import Types.*
import Utils.*

/** Computes initial coordinates from placement.
  *
  * Ported from @floating-ui/core/src/computeCoordsFromPlacement.ts
  */
object ComputeCoordsFromPlacement {

  def computeCoordsFromPlacement(
    rects: ElementRects,
    placement: Placement,
    rtl: Boolean = false
  ): Coords = {
    val sideAxis = getSideAxis(placement)
    val alignmentAxis = getAlignmentAxis(placement)
    val alignLength = getAxisLength(alignmentAxis)
    val side = getSide(placement)
    val isVertical = sideAxis == "y"

    val reference = rects.reference
    val floating = rects.floating

    val commonX = reference.x + reference.width / 2 - floating.width / 2
    val commonY = reference.y + reference.height / 2 - floating.height / 2

    val refAlignLength = if (alignLength == "width") reference.width else reference.height
    val floatAlignLength = if (alignLength == "width") floating.width else floating.height
    val commonAlign = refAlignLength / 2 - floatAlignLength / 2

    var coords: Coords = side match {
      case "top" =>
        Coords(x = commonX, y = reference.y - floating.height)
      case "bottom" =>
        Coords(x = commonX, y = reference.y + reference.height)
      case "right" =>
        Coords(x = reference.x + reference.width, y = commonY)
      case "left" =>
        Coords(x = reference.x - floating.width, y = commonY)
      case _ =>
        // Default case for any other placement (shouldn't happen in practice)
        Coords(x = reference.x, y = reference.y)
    }

    getAlignment(placement) match {
      case Some("start") =>
        val adjustment = commonAlign * (if (rtl && isVertical) -1 else 1)
        coords = if (alignmentAxis == "x") {
          coords.copy(x = coords.x - adjustment)
        } else {
          coords.copy(y = coords.y - adjustment)
        }
      case Some("end") =>
        val adjustment = commonAlign * (if (rtl && isVertical) -1 else 1)
        coords = if (alignmentAxis == "x") {
          coords.copy(x = coords.x + adjustment)
        } else {
          coords.copy(y = coords.y + adjustment)
        }
      case _ => // No alignment, keep coords as is
    }

    coords
  }
}
