package io.github.nguyenyou.laminar.primitives.utils.floating

import Types.*
import scala.math.{min, max}
import org.scalajs.dom

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

  /** Get placements on the opposite axis.
    *
    * Used for fallbackAxisSideDirection in flip middleware.
    */
  def getOppositeAxisPlacements(
    placement: Placement,
    flipAlignment: Boolean,
    direction: String,
    rtl: Boolean = false
  ): Seq[Placement] = {
    val alignment = getAlignment(placement)
    val side = getSide(placement)

    // Get the list of sides on the perpendicular axis
    val sideList = getSideList(side, direction == "start", rtl)

    // If there's an alignment, add it to each side
    val list = alignment match {
      case Some(align) =>
        val withAlignment = sideList.map(s => s"$s-$align")
        if (flipAlignment) {
          // Also include opposite alignment variants
          withAlignment ++ withAlignment.map(getOppositeAlignmentPlacement)
        } else {
          withAlignment
        }
      case None =>
        sideList
    }

    list
  }

  /** Helper to get the list of sides for opposite axis placements. */
  private def getSideList(side: Side, isStart: Boolean, rtl: Boolean): Seq[Placement] = {
    side match {
      case "top" | "bottom" =>
        if (rtl) {
          if (isStart) Seq("right", "left") else Seq("left", "right")
        } else {
          if (isStart) Seq("left", "right") else Seq("right", "left")
        }
      case "left" | "right" =>
        if (isStart) Seq("top", "bottom") else Seq("bottom", "top")
      case _ =>
        Seq.empty
    }
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

  // ============================================================================
  // AutoUpdate Utilities
  // ============================================================================

  /** Get bounding client rect for an element (simplified version for autoUpdate). */
  def getBoundingClientRect(element: dom.Element): ClientRectObject = {
    val rect = element.getBoundingClientRect()
    ClientRectObject(
      x = rect.x,
      y = rect.y,
      width = rect.width,
      height = rect.height,
      top = rect.top,
      right = rect.right,
      bottom = rect.bottom,
      left = rect.left
    )
  }

  /** Check if two client rects are equal. */
  def rectsAreEqual(a: ClientRectObject, b: ClientRectObject): Boolean = {
    a.x == b.x && a.y == b.y && a.width == b.width && a.height == b.height
  }

  /** Get the document element for a node. */
  def getDocumentElement(node: dom.Node): dom.Element = {
    node.ownerDocument.documentElement
  }

  /** Get the window for a node. */
  def getWindow(node: dom.Node): dom.Window = {
    dom.window
  }

  /** Check if a node is an HTML element. */
  def isHTMLElement(node: dom.Node): Boolean = {
    node.isInstanceOf[dom.HTMLElement]
  }

  /** Check if an element has overflow. */
  def isOverflowElement(element: dom.Element): Boolean = {
    if (!element.isInstanceOf[dom.HTMLElement]) return false
    val htmlElement = element.asInstanceOf[dom.HTMLElement]
    val style = dom.window.getComputedStyle(htmlElement)
    val overflow = style.overflow
    val overflowX = style.overflowX
    val overflowY = style.overflowY

    (overflow == "auto" || overflow == "scroll" || overflow == "overlay" ||
    overflowX == "auto" || overflowX == "scroll" || overflowX == "overlay" ||
    overflowY == "auto" || overflowY == "scroll" || overflowY == "overlay")
  }

  /** Get parent node, handling shadow DOM. */
  def getParentNode(node: dom.Node): dom.Node = {
    if (node.nodeName.toLowerCase == "html") return node

    // Try parent node or fallback to document element
    Option(node.parentNode).getOrElse(getDocumentElement(node))
  }

  /** Check if we've reached the last traversable node. */
  def isLastTraversableNode(node: dom.Node): Boolean = {
    val nodeName = node.nodeName.toLowerCase
    nodeName == "html" || nodeName == "body" || nodeName == "#document"
  }

  /** Get the nearest overflow ancestor. */
  def getNearestOverflowAncestor(node: dom.Node): dom.HTMLElement = {
    val parentNode = getParentNode(node)

    if (isLastTraversableNode(parentNode)) {
      return dom.document.body
    }

    if (isHTMLElement(parentNode) && isOverflowElement(parentNode.asInstanceOf[dom.Element])) {
      return parentNode.asInstanceOf[dom.HTMLElement]
    }

    getNearestOverflowAncestor(parentNode)
  }

  /** Get all overflow ancestors for a node. */
  def getOverflowAncestors(node: dom.Node): Seq[dom.EventTarget] = {
    def collect(currentNode: dom.Node, acc: Seq[dom.EventTarget]): Seq[dom.EventTarget] = {
      val scrollableAncestor = getNearestOverflowAncestor(currentNode)
      val isBody = scrollableAncestor == dom.document.body
      val win = getWindow(scrollableAncestor)

      if (isBody) {
        val windowSeq = Seq[dom.EventTarget](win)
        // visualViewport is not available in all browsers, skip it for simplicity
        val bodySeq = if (isOverflowElement(scrollableAncestor)) Seq(scrollableAncestor) else Seq.empty
        acc ++ windowSeq ++ bodySeq
      } else {
        acc ++ Seq(scrollableAncestor) ++ collect(scrollableAncestor, Seq.empty)
      }
    }

    collect(node, Seq.empty)
  }
}
