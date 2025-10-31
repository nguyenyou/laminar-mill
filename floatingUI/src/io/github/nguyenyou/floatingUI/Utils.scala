package io.github.nguyenyou.floatingUI

import Types.*
import scala.math.{min, max}
import org.scalajs.dom
import scala.scalajs.js

/** Utility functions for floating element positioning.
  *
  * Ported from @floating-ui/utils
  */
object Utils {

  // ============================================================================
  // Constants
  // ============================================================================

  val sides: Seq[Side] = Seq(Side.Top, Side.Right, Side.Bottom, Side.Left)
  val alignments: Seq[Alignment] = Seq(Alignment.Start, Alignment.End)
  val originSides: Set[Side] = Set(Side.Left, Side.Top)

  private val oppositeSideMap: Map[Side, Side] = Map(
    Side.Left -> Side.Right,
    Side.Right -> Side.Left,
    Side.Bottom -> Side.Top,
    Side.Top -> Side.Bottom
  )

  private val oppositeAlignmentMap: Map[Alignment, Alignment] = Map(
    Alignment.Start -> Alignment.End,
    Alignment.End -> Alignment.Start
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
    placement match {
      case Placement.Top | Placement.TopStart | Placement.TopEnd          => Side.Top
      case Placement.Right | Placement.RightStart | Placement.RightEnd    => Side.Right
      case Placement.Bottom | Placement.BottomStart | Placement.BottomEnd => Side.Bottom
      case Placement.Left | Placement.LeftStart | Placement.LeftEnd       => Side.Left
    }
  }

  def getAlignment(placement: Placement): Option[Alignment] = {
    placement match {
      case Placement.TopStart | Placement.RightStart | Placement.BottomStart | Placement.LeftStart => Some(Alignment.Start)
      case Placement.TopEnd | Placement.RightEnd | Placement.BottomEnd | Placement.LeftEnd         => Some(Alignment.End)
      case _                                                                                       => None
    }
  }

  def getOppositeAxis(axis: Axis): Axis = {
    if (axis == "x") "y" else "x"
  }

  def getAxisLength(axis: Axis): Length = {
    if (axis == "y") "height" else "width"
  }

  def getSideAxis(placement: Placement): Axis = {
    val side = getSide(placement)
    if (side == Side.Top || side == Side.Bottom) "y" else "x"
  }

  def getAlignmentAxis(placement: Placement): Axis = {
    getOppositeAxis(getSideAxis(placement))
  }

  def getOppositePlacement(placement: Placement): Placement = {
    placement match {
      case Placement.Top         => Placement.Bottom
      case Placement.TopStart    => Placement.BottomStart
      case Placement.TopEnd      => Placement.BottomEnd
      case Placement.Right       => Placement.Left
      case Placement.RightStart  => Placement.LeftStart
      case Placement.RightEnd    => Placement.LeftEnd
      case Placement.Bottom      => Placement.Top
      case Placement.BottomStart => Placement.TopStart
      case Placement.BottomEnd   => Placement.TopEnd
      case Placement.Left        => Placement.Right
      case Placement.LeftStart   => Placement.RightStart
      case Placement.LeftEnd     => Placement.RightEnd
    }
  }

  def getOppositeAlignmentPlacement(placement: Placement): Placement = {
    placement match {
      case Placement.TopStart    => Placement.TopEnd
      case Placement.TopEnd      => Placement.TopStart
      case Placement.RightStart  => Placement.RightEnd
      case Placement.RightEnd    => Placement.RightStart
      case Placement.BottomStart => Placement.BottomEnd
      case Placement.BottomEnd   => Placement.BottomStart
      case Placement.LeftStart   => Placement.LeftEnd
      case Placement.LeftEnd     => Placement.LeftStart
      case p                     => p // No alignment to flip for base placements
    }
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
        if (alignment.contains(if (rtl) Alignment.End else Alignment.Start)) Side.Right else Side.Left
      } else {
        if (alignment.contains(Alignment.Start)) Side.Bottom else Side.Top
      }

    if (refLength > floatLength) {
      mainAlignmentSide = oppositeSideMap(mainAlignmentSide)
    }

    (mainAlignmentSide, oppositeSideMap(mainAlignmentSide))
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

    // Get the list of base placements on the perpendicular axis
    val basePlacements = getSideList(side, direction == "start", rtl)

    // If there's an alignment, add it to each base placement
    val list = alignment match {
      case Some(align) =>
        val withAlignment = basePlacements.map { basePlacement =>
          // Construct aligned placement from base placement and alignment
          makeAlignedPlacement(basePlacement, align)
        }
        if (flipAlignment) {
          // Add all placements with original alignment, then all with opposite alignment
          withAlignment ++ withAlignment.map(getOppositeAlignmentPlacement)
        } else {
          withAlignment
        }
      case None =>
        basePlacements
    }

    list
  }

  /** Helper to construct an aligned placement from a base placement and alignment. */
  private def makeAlignedPlacement(basePlacement: Placement, alignment: Alignment): Placement = {
    (basePlacement, alignment) match {
      case (Placement.Top, Alignment.Start)    => Placement.TopStart
      case (Placement.Top, Alignment.End)      => Placement.TopEnd
      case (Placement.Right, Alignment.Start)  => Placement.RightStart
      case (Placement.Right, Alignment.End)    => Placement.RightEnd
      case (Placement.Bottom, Alignment.Start) => Placement.BottomStart
      case (Placement.Bottom, Alignment.End)   => Placement.BottomEnd
      case (Placement.Left, Alignment.Start)   => Placement.LeftStart
      case (Placement.Left, Alignment.End)     => Placement.LeftEnd
      case _                                   => basePlacement // Shouldn't happen, but return base placement as fallback
    }
  }

  /** Helper to get the list of base placements for opposite axis. */
  private def getSideList(side: Side, isStart: Boolean, rtl: Boolean): Seq[Placement] = {
    side match {
      case Side.Top | Side.Bottom =>
        if (rtl) {
          if (isStart) Seq(Placement.Right, Placement.Left) else Seq(Placement.Left, Placement.Right)
        } else {
          if (isStart) Seq(Placement.Left, Placement.Right) else Seq(Placement.Right, Placement.Left)
        }
      case Side.Left | Side.Right =>
        if (isStart) Seq(Placement.Top, Placement.Bottom) else Seq(Placement.Bottom, Placement.Top)
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
  // Virtual Element Utilities
  // ============================================================================

  /** Check if a reference element is a virtual element. */
  def isVirtualElement(element: ReferenceElement): Boolean = {
    element.isInstanceOf[VirtualElement]
  }

  /** Check if a reference element is a DOM element. */
  def isDOMElement(element: ReferenceElement): Boolean = {
    element.isInstanceOf[dom.Element]
  }

  /** Unwrap a reference element to get the context element for DOM operations.
    *
    * For virtual elements, returns the contextElement if available, otherwise null. For DOM elements, returns the element itself.
    */
  def unwrapElement(element: ReferenceElement): dom.Element = {
    element match {
      case ve: VirtualElement =>
        ve.contextElement.getOrElse(null)
      case e: dom.Element =>
        e
    }
  }

  // ============================================================================
  // Derivable Utilities
  // ============================================================================

  /** Evaluate a derivable value.
    *
    * If the value is static (Left), returns it directly. If the value is a function (Right), calls it with the middleware state.
    *
    * @param derivable
    *   The derivable value to evaluate
    * @param state
    *   The current middleware state
    * @return
    *   The evaluated value
    */
  def evaluate[T](derivable: Derivable[T], state: MiddlewareState): T = derivable match {
    case Left(value) => value
    case Right(fn)   => fn(state)
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
    if (node == null) return dom.window
    // Use parentWindow for compatibility (defaultView is not available in scala-js-dom)
    node.ownerDocument.asInstanceOf[js.Dynamic].defaultView.asInstanceOf[dom.Window]
  }

  /** Get the frame element for a window (if the window is inside an iframe).
    *
    * @param win
    *   The window to check
    * @return
    *   The iframe element, or null if not in an iframe
    */
  def getFrameElement(win: dom.Window): dom.Element = {
    try {
      // Check if window has a parent and if we can access it
      if (win.parent != null && win.parent != win) {
        win.frameElement.asInstanceOf[dom.Element]
      } else {
        null
      }
    } catch {
      case _: Throwable => null
    }
  }

  /** Check if a node is an HTML element. */
  def isHTMLElement(node: dom.Node): Boolean = {
    node.isInstanceOf[dom.HTMLElement]
  }

  /** Check if an element has overflow.
    *
    * Checks if an element can cause overflow by examining its overflow properties and display value. Elements with display: inline or
    * display: contents are excluded as they cannot be overflow containers.
    */
  def isOverflowElement(element: dom.Element): Boolean = {
    if (!element.isInstanceOf[dom.HTMLElement]) return false
    val htmlElement = element.asInstanceOf[dom.HTMLElement]
    val style = dom.window.getComputedStyle(htmlElement)
    val overflow = style.overflow
    val overflowX = style.overflowX
    val overflowY = style.overflowY
    val display = style.display

    // Invalid display values that cannot be overflow containers
    val invalidDisplayValues = Set("inline", "contents")

    // Check if any overflow property has a value that creates an overflow container
    val hasOverflow =
      overflow == "auto" || overflow == "scroll" || overflow == "overlay" || overflow == "hidden" || overflow == "clip" ||
        overflowX == "auto" || overflowX == "scroll" || overflowX == "overlay" || overflowX == "hidden" || overflowX == "clip" ||
        overflowY == "auto" || overflowY == "scroll" || overflowY == "overlay" || overflowY == "hidden" || overflowY == "clip"

    hasOverflow && !invalidDisplayValues.contains(display)
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

  /** Get all overflow ancestors for a node.
    *
    * Returns all ancestor elements that can cause overflow (scrollable containers, windows, etc.). This includes traversing up through
    * iframe boundaries when enabled.
    *
    * @param node
    *   The node to get overflow ancestors for
    * @param list
    *   Initial list of ancestors (used for recursive calls)
    * @param traverseIframes
    *   Whether to traverse up through iframe boundaries (default: true)
    * @return
    *   Sequence of overflow ancestor elements including windows and visual viewports
    */
  def getOverflowAncestors(
    node: dom.Node,
    list: Seq[dom.EventTarget] = Seq.empty,
    traverseIframes: Boolean = true
  ): Seq[dom.EventTarget] = {
    val scrollableAncestor = getNearestOverflowAncestor(node)
    // Access body through dynamic typing since it's not directly available in dom.Document
    val ownerDoc = node.ownerDocument
    val docBody = if (ownerDoc != null) {
      ownerDoc.asInstanceOf[js.Dynamic].body.asInstanceOf[dom.HTMLElement]
    } else {
      dom.document.body
    }
    val isBody = scrollableAncestor == docBody
    val win = getWindow(scrollableAncestor)

    if (isBody) {
      // When we reach the body, collect window, visualViewport, body (if overflow), and iframe ancestors
      val frameElement = getFrameElement(win)

      // Try to get visualViewport if available
      val visualViewportSeq: Seq[dom.EventTarget] =
        try {
          val vv = win.asInstanceOf[js.Dynamic].visualViewport
          if (vv != null && !js.isUndefined(vv)) {
            Seq(vv.asInstanceOf[dom.EventTarget])
          } else {
            Seq.empty
          }
        } catch {
          case _: Throwable => Seq.empty
        }

      // Include body only if it has overflow
      val bodySeq = if (isOverflowElement(scrollableAncestor)) Seq(scrollableAncestor) else Seq.empty

      // Recursively get ancestors from parent iframe if present and traversal is enabled
      val frameAncestors = if (frameElement != null && traverseIframes) {
        getOverflowAncestors(frameElement, Seq.empty, traverseIframes)
      } else {
        Seq.empty
      }

      list ++ Seq(win) ++ visualViewportSeq ++ bodySeq ++ frameAncestors
    } else {
      // Continue traversing up the tree
      list ++ Seq(scrollableAncestor) ++ getOverflowAncestors(scrollableAncestor, Seq.empty, traverseIframes)
    }
  }
}
