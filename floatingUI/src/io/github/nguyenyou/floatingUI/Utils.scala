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
  private val invalidOverflowDisplayValues: Set[String] = Set("inline", "contents")
  private val lastTraversableNodeNames: Set[String] = Set("html", "body", "#document")

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
    axis match {
      case Axis.X => Axis.Y
      case Axis.Y => Axis.X
    }
  }

  def getAxisLength(axis: Axis): Length = {
    axis match {
      case Axis.Y => Length.Height
      case Axis.X => Length.Width
    }
  }

  def getSideAxis(placement: Placement): Axis = {
    val side = getSide(placement)
    if (side == Side.Top || side == Side.Bottom) Axis.Y else Axis.X
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

    val refLength = length match {
      case Length.Width  => rects.reference.width
      case Length.Height => rects.reference.height
    }
    val floatLength = length match {
      case Length.Width  => rects.floating.width
      case Length.Height => rects.floating.height
    }

    var mainAlignmentSide: Side =
      alignmentAxis match {
        case Axis.X =>
          if (alignment.contains(if (rtl) Alignment.End else Alignment.Start)) Side.Right else Side.Left
        case Axis.Y =>
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
    case Axis.X => coords.x
    case Axis.Y => coords.y
  }

  def updateCoords(coords: Coords, axis: Axis, value: Double): Coords = axis match {
    case Axis.X => coords.copy(x = value)
    case Axis.Y => coords.copy(y = value)
  }

  // ============================================================================
  // AutoUpdate Utilities
  // ============================================================================

  /** Check if two client rects are equal. */
  def rectsAreEqual(a: ClientRectObject, b: ClientRectObject): Boolean = {
    a.x == b.x && a.y == b.y && a.width == b.width && a.height == b.height
  }

  /** Determine if the global window object exists (SSR safety). */
  private def hasWindow: Boolean = !js.isUndefined(js.Dynamic.global.selectDynamic("window"))

  /** Get the document element for a node. */
  def getDocumentElement(node: dom.Node): dom.Element = {
    val doc =
      if (node == null) {
        dom.document
      } else {
        Option(node.ownerDocument).getOrElse(dom.document)
      }
    doc.documentElement
  }

  /** Get the window for a node. */
  def getWindow(node: dom.Node): dom.Window = {
    if (!hasWindow) {
      js.Dynamic.global.selectDynamic("window").asInstanceOf[dom.Window]
    } else if (node == null) {
      dom.window
    } else {
      val ownerDoc = Option(node.ownerDocument).getOrElse(dom.document)
      val defaultView = ownerDoc.asInstanceOf[js.Dynamic].selectDynamic("defaultView")
      if (defaultView != null && !js.isUndefined(defaultView)) {
        defaultView.asInstanceOf[dom.Window]
      } else {
        dom.window
      }
    }
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

  /** Check if an element has overflow using the same criteria as @floating-ui/utils/dom. */
  def isOverflowElement(element: dom.Element): Boolean = {
    if (element == null || !element.isInstanceOf[dom.Element]) return false
    val css = dom.window.getComputedStyle(element)
    val overflowCombined = css.overflow + css.overflowX + css.overflowY
    val overflowMatches = "(auto|scroll|overlay|hidden|clip)".r
    overflowMatches.findFirstIn(overflowCombined).nonEmpty && !invalidOverflowDisplayValues.contains(css.display)
  }

  private def isShadowRoot(value: Any): Boolean = {
    val shadowRootCtor = js.Dynamic.global.selectDynamic("ShadowRoot")
    if (js.isUndefined(shadowRootCtor)) {
      false
    } else {
      value match {
        case sr: dom.Node => sr.isInstanceOf[dom.ShadowRoot]
        case _            => false
      }
    }
  }

  /** Get parent node, handling shadow DOM and slots. */
  def getParentNode(node: dom.Node): dom.Node = {
    if (node == null) return getDocumentElement(null)
    if (node.nodeName.toLowerCase == "html") return node

    val assignedSlot = node.asInstanceOf[js.Dynamic].selectDynamic("assignedSlot")
    val parentNode = Option(node.parentNode)
    val shadowHost =
      if (isShadowRoot(node)) {
        Option(node.asInstanceOf[js.Dynamic].selectDynamic("host").asInstanceOf[dom.Node])
      } else {
        None
      }

    Option(assignedSlot.asInstanceOf[dom.Node])
      .orElse(parentNode)
      .orElse(shadowHost)
      .getOrElse(getDocumentElement(node))
  }

  /** Check if we've reached the last traversable node. */
  def isLastTraversableNode(node: dom.Node): Boolean = {
    if (node == null) return true
    lastTraversableNodeNames.contains(node.nodeName.toLowerCase)
  }

  /** Get the nearest overflow ancestor. */
  def getNearestOverflowAncestor(node: dom.Node): dom.HTMLElement = {
    val parentNode = getParentNode(node)

    if (isLastTraversableNode(parentNode)) {
      val ownerDoc = Option(node.ownerDocument).getOrElse(dom.document)
      ownerDoc.asInstanceOf[js.Dynamic].selectDynamic("body").asInstanceOf[dom.HTMLElement]
    } else if (isHTMLElement(parentNode) && isOverflowElement(parentNode.asInstanceOf[dom.Element])) {
      parentNode.asInstanceOf[dom.HTMLElement]
    } else {
      getNearestOverflowAncestor(parentNode)
    }
  }

  /** Get all overflow ancestors for a node (matches @floating-ui/utils/dom). */
  def getOverflowAncestors(
    node: dom.Node,
    list: Seq[dom.EventTarget] = Seq.empty,
    traverseIframes: Boolean = true
  ): Seq[dom.EventTarget] = {
    val scrollableAncestor = getNearestOverflowAncestor(node)
    val ownerDoc = Option(node.ownerDocument).getOrElse(dom.document)
    val body = ownerDoc.asInstanceOf[js.Dynamic].selectDynamic("body").asInstanceOf[dom.HTMLElement]
    val isBody = scrollableAncestor == body
    val win = getWindow(scrollableAncestor)

    if (isBody) {
      val frameElement = getFrameElement(win)
      val visualViewportSeq: Seq[dom.EventTarget] =
        try {
          val vv = win.asInstanceOf[js.Dynamic].selectDynamic("visualViewport")
          if (vv != null && !js.isUndefined(vv)) Seq(vv.asInstanceOf[dom.EventTarget]) else Seq.empty
        } catch {
          case _: Throwable => Seq.empty
        }

      val bodySeq = if (isOverflowElement(scrollableAncestor)) Seq(scrollableAncestor.asInstanceOf[dom.EventTarget]) else Seq.empty
      val frameAncestors =
        if (frameElement != null && traverseIframes) {
          getOverflowAncestors(frameElement, Seq.empty, traverseIframes)
        } else {
          Seq.empty
        }

      list ++ Seq(win) ++ visualViewportSeq ++ bodySeq ++ frameAncestors
    } else {
      list ++ Seq(scrollableAncestor.asInstanceOf[dom.EventTarget]) ++ getOverflowAncestors(
        scrollableAncestor,
        Seq.empty,
        traverseIframes
      )
    }
  }
}
