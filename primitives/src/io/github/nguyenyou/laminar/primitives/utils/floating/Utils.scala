package io.github.nguyenyou.laminar.primitives.utils.floating

import Types.*
import scala.math.{min, max}

/** Utility functions for floating element positioning.
  *
  * Ported from @floating-ui/utils
  */
object Utils {

  // ============================================================================
  // Constants
  // ============================================================================

  val sides: Seq[Side] = Seq("top", "right", "bottom", "left")
  val alignments: Seq[Alignment] = Seq("start", "end")
  val originSides: Set[String] = Set("left", "top")

  private val oppositeSideMap: Map[String, String] = Map(
    "left" -> "right",
    "right" -> "left",
    "bottom" -> "top",
    "top" -> "bottom"
  )

  private val oppositeAlignmentMap: Map[String, String] = Map(
    "start" -> "end",
    "end" -> "start"
  )

  // ============================================================================
  // Basic Utilities
  // ============================================================================

  def clamp(start: Double, value: Double, end: Double): Double = {
    max(start, min(value, end))
  }

  def createCoords(v: Double): Coords = Coords(v, v)

  // ============================================================================
  // Placement Utilities
  // ============================================================================

  def getSide(placement: Placement): Side = {
    placement.split("-")(0).asInstanceOf[Side]
  }

  def getAlignment(placement: Placement): Option[Alignment] = {
    val parts = placement.split("-")
    if (parts.length > 1) Some(parts(1).asInstanceOf[Alignment])
    else None
  }

  def getOppositeAxis(axis: Axis): Axis = {
    if (axis == "x") "y" else "x"
  }

  def getAxisLength(axis: Axis): Length = {
    if (axis == "y") "height" else "width"
  }

  def getSideAxis(placement: Placement): Axis = {
    val side = getSide(placement)
    if (side == "top" || side == "bottom") "y" else "x"
  }

  def getAlignmentAxis(placement: Placement): Axis = {
    getOppositeAxis(getSideAxis(placement))
  }

  def getOppositePlacement(placement: Placement): Placement = {
    val pattern = "(left|right|bottom|top)".r
    pattern.replaceAllIn(placement, m => oppositeSideMap(m.matched))
  }

  def getOppositeAlignmentPlacement(placement: Placement): Placement = {
    val pattern = "(start|end)".r
    pattern.replaceAllIn(placement, m => oppositeAlignmentMap(m.matched))
  }

  def getExpandedPlacements(placement: Placement): Seq[Placement] = {
    val oppositePlacement = getOppositePlacement(placement)
    Seq(
      getOppositeAlignmentPlacement(placement),
      oppositePlacement,
      getOppositeAlignmentPlacement(oppositePlacement)
    )
  }

  def getAlignmentSides(
    placement: Placement,
    rects: ElementRects,
    rtl: Boolean = false
  ): (Side, Side) = {
    val alignment = getAlignment(placement)
    val alignmentAxis = getAlignmentAxis(placement)
    val length = getAxisLength(alignmentAxis)

    val refLength = if (length == "width") rects.reference.width else rects.reference.height
    val floatLength = if (length == "width") rects.floating.width else rects.floating.height

    var mainAlignmentSide: Side =
      if (alignmentAxis == "x") {
        if (alignment.contains(if (rtl) "end" else "start")) "right" else "left"
      } else {
        if (alignment.contains("start")) "bottom" else "top"
      }

    if (refLength > floatLength) {
      mainAlignmentSide = getOppositePlacement(mainAlignmentSide).asInstanceOf[Side]
    }

    (mainAlignmentSide, getOppositePlacement(mainAlignmentSide).asInstanceOf[Side])
  }

  // ============================================================================
  // Padding Utilities
  // ============================================================================

  def expandPaddingObject(padding: PartialSideObject): SideObject = {
    SideObject(
      top = padding.top.getOrElse(0),
      right = padding.right.getOrElse(0),
      bottom = padding.bottom.getOrElse(0),
      left = padding.left.getOrElse(0)
    )
  }

  def getPaddingObject(padding: Padding): SideObject = {
    padding match {
      case d: Double            => SideObject(d, d, d, d)
      case p: PartialSideObject => expandPaddingObject(p)
    }
  }

  // ============================================================================
  // Rect Utilities
  // ============================================================================

  def rectToClientRect(rect: Rect): ClientRectObject = {
    ClientRectObject(
      x = rect.x,
      y = rect.y,
      width = rect.width,
      height = rect.height,
      top = rect.y,
      left = rect.x,
      right = rect.x + rect.width,
      bottom = rect.y + rect.height
    )
  }

  // ============================================================================
  // Rect Access Helpers
  // ============================================================================

  def getRectValue(rect: Rect, key: String): Double = key match {
    case "x"      => rect.x
    case "y"      => rect.y
    case "width"  => rect.width
    case "height" => rect.height
    case _        => 0
  }

  def getCoordsValue(coords: Coords, axis: Axis): Double = axis match {
    case "x" => coords.x
    case "y" => coords.y
  }

  def updateCoords(coords: Coords, axis: Axis, value: Double): Coords = axis match {
    case "x" => coords.copy(x = value)
    case "y" => coords.copy(y = value)
  }
}
