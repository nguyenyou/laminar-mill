package io.github.nguyenyou.floatingUI

import scala.scalajs.js
import org.scalajs.dom

/** Core types for floating element positioning.
  *
  * Ported from @floating-ui/utils and @floating-ui/core
  */
object Types {

  // ============================================================================
  // Basic Types
  // ============================================================================

  /** Alignment of the floating element relative to the reference element. */
  type Alignment = "start" | "end"

  /** Side of the reference element where the floating element is placed. */
  type Side = "top" | "right" | "bottom" | "left"

  /** Placement of the floating element. Can be a side or a side with alignment. */
  enum Placement {
    case Top, TopStart, TopEnd
    case Right, RightStart, RightEnd
    case Bottom, BottomStart, BottomEnd
    case Left, LeftStart, LeftEnd

    /** Convert placement to string representation. */
    def toValue: String = this match {
      case Top         => "top"
      case TopStart    => "top-start"
      case TopEnd      => "top-end"
      case Right       => "right"
      case RightStart  => "right-start"
      case RightEnd    => "right-end"
      case Bottom      => "bottom"
      case BottomStart => "bottom-start"
      case BottomEnd   => "bottom-end"
      case Left        => "left"
      case LeftStart   => "left-start"
      case LeftEnd     => "left-end"
    }
  }

  object Placement {

    /** Parse a string into a Placement. */
    def fromString(s: String): Option[Placement] = s match {
      case "top"          => Some(Top)
      case "top-start"    => Some(TopStart)
      case "top-end"      => Some(TopEnd)
      case "right"        => Some(Right)
      case "right-start"  => Some(RightStart)
      case "right-end"    => Some(RightEnd)
      case "bottom"       => Some(Bottom)
      case "bottom-start" => Some(BottomStart)
      case "bottom-end"   => Some(BottomEnd)
      case "left"         => Some(Left)
      case "left-start"   => Some(LeftStart)
      case "left-end"     => Some(LeftEnd)
      case _              => None
    }

    /** All possible placements. */
    val all: Seq[Placement] = Seq(
      Top,
      TopStart,
      TopEnd,
      Right,
      RightStart,
      RightEnd,
      Bottom,
      BottomStart,
      BottomEnd,
      Left,
      LeftStart,
      LeftEnd
    )
  }

  /** Positioning strategy: "absolute" or "fixed". */
  enum Strategy {
    case Absolute, Fixed

    /** Convert strategy to string representation. */
    def toValue: String = this match {
      case Absolute => "absolute"
      case Fixed    => "fixed"
    }
  }

  object Strategy {

    /** Parse a string into a Strategy. */
    def fromString(s: String): Option[Strategy] = s match {
      case "absolute" => Some(Absolute)
      case "fixed"    => Some(Fixed)
      case _          => None
    }
  }

  /** Axis for positioning: "x" or "y". */
  type Axis = "x" | "y"

  /** Length dimension: "width" or "height". */
  type Length = "width" | "height"

  // ============================================================================
  // Coordinate and Dimension Types
  // ============================================================================

  /** Coordinates on x and y axes. */
  case class Coords(x: Double, y: Double)

  /** Dimensions (width and height). */
  case class Dimensions(width: Double, height: Double)

  /** Rectangle with coordinates and dimensions. */
  case class Rect(x: Double, y: Double, width: Double, height: Double)

  /** Side object with values for each side. */
  case class SideObject(top: Double, right: Double, bottom: Double, left: Double)

  /** Client rectangle object (combines Rect and SideObject). */
  case class ClientRectObject(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    top: Double,
    right: Double,
    bottom: Double,
    left: Double
  )

  /** Element rectangles for reference and floating elements. */
  case class ElementRects(reference: Rect, floating: Rect)

  /** Padding can be a number or a partial side object. */
  type Padding = Double | PartialSideObject

  /** Partial side object for padding. */
  case class PartialSideObject(
    top: Option[Double] = None,
    right: Option[Double] = None,
    bottom: Option[Double] = None,
    left: Option[Double] = None
  )

  // ============================================================================
  // Middleware Types
  // ============================================================================

  /** Middleware data returned from middleware functions.
    *
    * Supports both typed data for known middleware and arbitrary data for custom middleware.
    */
  case class MiddlewareData(
    arrow: Option[ArrowData] = None,
    offset: Option[OffsetData] = None,
    shift: Option[ShiftData] = None,
    flip: Option[FlipData] = None,
    autoPlacement: Option[AutoPlacementData] = None,
    hide: Option[HideData] = None,
    size: Option[SizeData] = None,
    inline: Option[InlineData] = None,
    // Extensible storage for custom middleware data
    custom: Map[String, Any] = Map.empty
  )

  /** Arrow middleware data. */
  case class ArrowData(
    x: Option[Double] = None,
    y: Option[Double] = None,
    centerOffset: Double,
    alignmentOffset: Option[Double] = None
  )

  /** Offset middleware data. */
  case class OffsetData(x: Double, y: Double, placement: Placement)

  /** Axis enabled flags for shift middleware. */
  case class AxisEnabled(x: Boolean, y: Boolean)

  /** Shift middleware data. */
  case class ShiftData(x: Double, y: Double, enabled: AxisEnabled)

  /** Flip middleware data. */
  case class FlipData(
    index: Option[Int] = None,
    overflows: Seq[PlacementOverflow] = Seq.empty
  )

  /** Overflow data for a specific placement. */
  case class PlacementOverflow(
    placement: Placement,
    overflows: Seq[Double]
  )

  /** AutoPlacement middleware data. */
  case class AutoPlacementData(
    index: Option[Int] = None,
    overflows: Seq[PlacementOverflow] = Seq.empty
  )

  /** Hide middleware data. */
  case class HideData(
    referenceHidden: Option[Boolean] = None,
    escaped: Option[Boolean] = None,
    referenceHiddenOffsets: Option[SideObject] = None,
    escapedOffsets: Option[SideObject] = None
  )

  /** Size middleware data. */
  case class SizeData(
    availableWidth: Double,
    availableHeight: Double
  )

  /** State object passed to the size middleware's apply function.
    *
    * Contains all MiddlewareState fields plus availableWidth and availableHeight.
    */
  case class ApplyState(
    x: Double,
    y: Double,
    initialPlacement: Placement,
    placement: Placement,
    strategy: Strategy,
    middlewareData: MiddlewareData,
    rects: ElementRects,
    platform: Platform,
    elements: Elements,
    availableWidth: Double,
    availableHeight: Double
  )

  /** Inline middleware data. */
  case class InlineData(
    x: Option[Double] = None,
    y: Option[Double] = None
  )

  /** Middleware state passed to middleware functions. */
  case class MiddlewareState(
    x: Double,
    y: Double,
    initialPlacement: Placement,
    placement: Placement,
    strategy: Strategy,
    middlewareData: MiddlewareData,
    elements: Elements,
    rects: ElementRects,
    platform: Platform
  )

  /** Middleware return value. */
  case class MiddlewareReturn(
    x: Option[Double] = None,
    y: Option[Double] = None,
    data: Option[Map[String, Any]] = None,
    reset: Option[Either[Boolean, ResetValue]] = None
  )

  /** Reset value for middleware. */
  case class ResetValue(
    placement: Option[Placement] = None,
    rects: Option[Either[Boolean, ElementRects]] = None
  )

  /** Middleware object. */
  trait Middleware {
    def name: String
    def fn(state: MiddlewareState): MiddlewareReturn
  }

  // ============================================================================
  // Virtual Elements
  // ============================================================================

  /** Virtual element for custom positioning reference.
    *
    * Allows positioning relative to arbitrary coordinates (e.g., mouse position, custom objects).
    * @see
    *   https://floating-ui.com/docs/virtual-elements
    */
  trait VirtualElement {

    /** Returns the bounding client rect for this virtual element. */
    def getBoundingClientRect(): ClientRectObject

    /** Optionally returns multiple client rects (for inline elements). */
    def getClientRects(): Option[Seq[ClientRectObject]] = None

    /** Optional context element for determining containing block, etc. */
    def contextElement: Option[dom.Element] = None
  }

  /** Reference element type - can be either a DOM element or a virtual element. */
  type ReferenceElement = dom.Element | VirtualElement

  // ============================================================================
  // Derivable Values
  // ============================================================================

  /** Derivable value type - can be either a static value or a function that computes the value from middleware state.
    *
    * This allows middleware options to be computed dynamically based on the current positioning state.
    * @see
    *   https://floating-ui.com/docs/middleware#options
    */
  type Derivable[T] = Either[T, MiddlewareState => T]

  /** Elements object containing reference and floating elements. */
  case class Elements(reference: ReferenceElement, floating: dom.HTMLElement)

  // ============================================================================
  // Platform Interface
  // ============================================================================

  /** Platform interface for DOM operations.
    *
    * Matches the TypeScript Platform interface from @floating-ui/core/src/types.ts All methods use object parameters to match the
    * TypeScript signature.
    */
  trait Platform {
    // Required methods
    def getElementRects(reference: ReferenceElement, floating: dom.HTMLElement, strategy: Strategy): ElementRects
    def getDimensions(element: dom.Element): Dimensions
    def getClippingRect(element: Any, boundary: String, rootBoundary: String, strategy: Strategy): Rect

    // Cache for expensive operations (e.g., getClippingElementAncestors)
    // This is injected by computePosition and used by platform methods
    var _c: Option[scala.collection.mutable.Map[ReferenceElement, Seq[dom.Element]]] = None

    // Optional methods with default implementations

    /** Convert offset-parent-relative rect to viewport-relative rect.
      *
      * Optional method - converts coordinates from offset parent space to viewport space.
      */
    def convertOffsetParentRelativeRectToViewportRelativeRect(
      elements: Option[Elements],
      rect: Rect,
      offsetParent: Any,
      strategy: Strategy
    ): Option[Rect] = None

    /** Get the offset parent of an element.
      *
      * Optional method - returns the closest positioned ancestor element.
      */
    def getOffsetParent(element: Any): Option[Any] = None

    /** Check if a value is a DOM element.
      *
      * Optional method - returns true if the value is an Element.
      */
    def isElement(value: Any): Option[Boolean] = None

    /** Get the document element.
      *
      * Optional method - returns the document element (usually <html>).
      */
    def getDocumentElement(element: Any): Option[Any] = None

    /** Get the scale of an element.
      *
      * Optional method - returns the x and y scale factors of an element.
      */
    def getScale(element: Any): Option[Coords] = None

    /** Check if the element uses right-to-left text direction.
      *
      * Optional method - returns true if the element uses RTL text direction.
      */
    def isRTL(element: dom.Element): Boolean = {
      val computedStyle = dom.window.getComputedStyle(element)
      computedStyle.direction == "rtl"
    }

    /** Get client rects for an element (for inline elements).
      *
      * Optional method - returns an array of client rects for the element.
      */
    def getClientRects(element: ReferenceElement): Seq[ClientRectObject]
  }

  // ============================================================================
  // Configuration and Return Types
  // ============================================================================

  /** Configuration for computePosition. */
  case class ComputePositionConfig(
    placement: Placement = Placement.Bottom,
    strategy: Strategy = Strategy.Absolute,
    middleware: Seq[Middleware] = Seq.empty,
    platform: Platform
  )

  /** Return value from computePosition. */
  case class ComputePositionReturn(
    x: Double,
    y: Double,
    placement: Placement,
    strategy: Strategy,
    middlewareData: MiddlewareData
  )

  // ============================================================================
  // Middleware Options
  // ============================================================================

  /** Object form of offset options. */
  case class OffsetOptionsObject(
    mainAxis: Double = 0,
    crossAxis: Double = 0,
    alignmentAxis: Option[Double] = None
  )

  /** Offset value - can be a number (shorthand for mainAxis) or an object with axis values.
    *
    * Matches TypeScript: type OffsetValue = number | { mainAxis?: number; crossAxis?: number; alignmentAxis?: number | null }
    */
  type OffsetValue = Either[Double, OffsetOptionsObject]

  /** Options for offset middleware.
    *
    * Can be a static value or a function that computes the value from middleware state. Matches TypeScript: type OffsetOptions =
    * OffsetValue | Derivable<OffsetValue>
    */
  type OffsetOptions = Derivable[OffsetValue]

  /** Options for shift middleware.
    *
    * Extends DetectOverflowOptions to include all boundary detection options.
    */
  case class ShiftOptions(
    // Shift-specific options
    mainAxis: Boolean = true,
    crossAxis: Boolean = false,
    limiter: Option[Limiter] = None,
    // DetectOverflowOptions fields
    boundary: String = "clippingAncestors",
    rootBoundary: String = "viewport",
    elementContext: String = "floating",
    altBoundary: Boolean = false,
    padding: Derivable[Padding] = Left(0)
  )

  /** Limiter for shift middleware. */
  case class Limiter(
    options: Any = (),
    fn: MiddlewareState => Coords
  )

  /** Cross-axis option for flip middleware - can be Boolean or "alignment". */
  type FlipCrossAxis = Boolean | String

  /** Options for flip middleware.
    *
    * Extends DetectOverflowOptions to include all boundary detection options.
    */
  case class FlipOptions(
    // Flip-specific options
    mainAxis: Boolean = true,
    crossAxis: FlipCrossAxis = true,
    fallbackPlacements: Option[Seq[Placement]] = None,
    fallbackStrategy: String = "bestFit",
    fallbackAxisSideDirection: String = "none",
    flipAlignment: Boolean = true,
    // DetectOverflowOptions fields
    boundary: String = "clippingAncestors",
    rootBoundary: String = "viewport",
    elementContext: String = "floating",
    altBoundary: Boolean = false,
    padding: Derivable[Padding] = Left(0)
  )

  /** Options for arrow middleware.
    *
    * Note: In TypeScript, the type is `ArrowOptions | Derivable<ArrowOptions>`, but we handle this at the function signature level by
    * accepting `Derivable[ArrowOptions]`.
    */
  case class ArrowOptions(
    element: dom.HTMLElement,
    padding: Derivable[Padding] = Left(0)
  )

  /** Options for autoPlacement middleware.
    *
    * Extends DetectOverflowOptions to include all boundary detection options.
    */
  case class AutoPlacementOptions(
    // AutoPlacement-specific options
    crossAxis: Boolean = false,
    alignment: Option[Alignment] = None,
    allowedPlacements: Seq[Placement] = Seq.empty,
    autoAlignment: Boolean = true,
    // DetectOverflowOptions fields
    boundary: String = "clippingAncestors",
    rootBoundary: String = "viewport",
    elementContext: String = "floating",
    altBoundary: Boolean = false,
    padding: Derivable[Padding] = Left(0)
  )

  /** Options for hide middleware.
    *
    * Extends DetectOverflowOptions to include all boundary detection options.
    */
  case class HideOptions(
    // Hide-specific option
    strategy: String = "referenceHidden", // "referenceHidden" or "escaped"
    // DetectOverflowOptions fields
    boundary: String = "clippingAncestors",
    rootBoundary: String = "viewport",
    elementContext: String = "floating",
    altBoundary: Boolean = false,
    padding: Padding = 0
  )

  /** Options for size middleware.
    *
    * Extends DetectOverflowOptions to include all boundary detection options.
    */
  case class SizeOptions(
    // DetectOverflowOptions fields
    boundary: String = "clippingAncestors",
    rootBoundary: String = "viewport",
    elementContext: String = "floating",
    altBoundary: Boolean = false,
    padding: Derivable[Padding] = Left(0),
    // Size-specific option
    apply: Option[ApplyState => Unit] = None
  )

  /** Options for inline middleware.
    *
    * Note: padding defaults to 2 to handle MouseEvent client coords being up to 2px off ClientRect bounds.
    */
  case class InlineOptions(
    x: Option[Double] = None,
    y: Option[Double] = None,
    padding: Padding = 2
  )

  /** Options for limitShift.
    *
    * Note: Since the entire LimitShiftOptions can be derivable, individual fields are not Derivable.
    */
  case class LimitShiftOptions(
    offset: Either[Double, LimitShiftOffsetOptions] = Left(0),
    mainAxis: Boolean = true,
    crossAxis: Boolean = true
  )

  /** Offset options for limitShift. */
  case class LimitShiftOffsetOptions(
    mainAxis: Double = 0,
    crossAxis: Double = 0
  )

  /** Detect overflow options. */
  case class DetectOverflowOptions(
    boundary: String = "clippingAncestors",
    rootBoundary: String = "viewport",
    elementContext: String = "floating",
    altBoundary: Boolean = false,
    padding: Padding = 0
  )
}
