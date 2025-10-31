package io.github.nguyenyou.floatingUI

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
    val isVertical = sideAxis == Axis.Y

    val reference = rects.reference
    val floating = rects.floating

    val commonX = reference.x + reference.width / 2 - floating.width / 2
    val commonY = reference.y + reference.height / 2 - floating.height / 2

    val refAlignLength = alignLength match {
      case Length.Width  => reference.width
      case Length.Height => reference.height
    }
    val floatAlignLength = alignLength match {
      case Length.Width  => floating.width
      case Length.Height => floating.height
    }
    val commonAlign = refAlignLength / 2 - floatAlignLength / 2

    var coords: Coords = side match {
      case Side.Top =>
        Coords(x = commonX, y = reference.y - floating.height)
      case Side.Bottom =>
        Coords(x = commonX, y = reference.y + reference.height)
      case Side.Right =>
        Coords(x = reference.x + reference.width, y = commonY)
      case Side.Left =>
        Coords(x = reference.x - floating.width, y = commonY)
    }

    getAlignment(placement) match {
      case Some(Alignment.Start) =>
        val adjustment = commonAlign * (if (rtl && isVertical) -1 else 1)
        coords = alignmentAxis match {
          case Axis.X => coords.copy(x = coords.x - adjustment)
          case Axis.Y => coords.copy(y = coords.y - adjustment)
        }
      case Some(Alignment.End) =>
        val adjustment = commonAlign * (if (rtl && isVertical) -1 else 1)
        coords = alignmentAxis match {
          case Axis.X => coords.copy(x = coords.x + adjustment)
          case Axis.Y => coords.copy(y = coords.y + adjustment)
        }
      case _ => // No alignment, keep coords as is
    }

    coords
  }
}
