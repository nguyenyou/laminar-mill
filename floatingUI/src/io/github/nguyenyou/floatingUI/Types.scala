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
  enum Alignment {
    case Start, End

    /** Convert alignment to string representation. */
    def toValue: String = this match {
      case Start => "start"
      case End   => "end"
    }
  }

  object Alignment {

    /** Parse a string into an Alignment. */
    def fromString(s: String): Option[Alignment] = s match {
      case "start" => Some(Start)
      case "end"   => Some(End)
      case _       => None
    }
  }

  /** Side of the reference element where the floating element is placed. */
  enum Side {
    case Top, Right, Bottom, Left

    /** Convert side to string representation. */
    def toValue: String = this match {
      case Top    => "top"
      case Right  => "right"
      case Bottom => "bottom"
      case Left   => "left"
    }
  }

  object Side {

    /** Parse a string into a Side. */
    def fromString(s: String): Option[Side] = s match {
      case "top"    => Some(Top)
      case "right"  => Some(Right)
      case "bottom" => Some(Bottom)
      case "left"   => Some(Left)
      case _        => None
    }

    /** All sides in order. */
    val all: Seq[Side] = Seq(Top, Right, Bottom, Left)
  }

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
  enum Axis {
    case X, Y

    /** Convert axis to string representation. */
    def toValue: String = this match {
      case X => "x"
      case Y => "y"
    }
  }

  object Axis {

    /** Parse a string into an Axis. */
    def fromString(s: String): Option[Axis] = s match {
      case "x" => Some(X)
      case "y" => Some(Y)
      case _   => None
    }
  }

  /** Length dimension: "width" or "height". */
  enum Length {
    case Width, Height

    /** Convert length to string representation. */
    def toValue: String = this match {
      case Width  => "width"
      case Height => "height"
    }
  }

  object Length {

    /** Parse a string into a Length. */
    def fromString(s: String): Option[Length] = s match {
      case "width"  => Some(Width)
      case "height" => Some(Height)
      case _        => None
    }
  }

  /** Fallback strategy for flip middleware: "bestFit" or "initialPlacement". */
  enum FallbackStrategy {
    case BestFit, InitialPlacement

    /** Convert fallback strategy to string representation. */
    def toValue: String = this match {
      case BestFit          => "bestFit"
      case InitialPlacement => "initialPlacement"
    }
  }

  object FallbackStrategy {

    /** Parse a string into a FallbackStrategy. */
    def fromString(s: String): Option[FallbackStrategy] = s match {
      case "bestFit"          => Some(BestFit)
      case "initialPlacement" => Some(InitialPlacement)
      case _                  => None
    }
  }

  /** Boundary type for clipping detection.
    *
    * Matches TypeScript: 'clippingAncestors' | Element | Array<Element> | Rect
    *
    * Supports four types of boundaries:
    *   - String "clippingAncestors": Uses element's overflow ancestors
    *   - String (CSS selector): Queries DOM for boundary element
    *   - dom.Element: Single element as boundary
    *   - js.Array[dom.Element]: Multiple elements as boundaries
    *   - Rect: Custom rectangle as boundary
    */
  type Boundary = String | dom.Element | js.Array[dom.Element] | Rect

  /** Internal representation of Boundary for type-safe pattern matching.
    *
    * This is used internally to ensure exhaustive pattern matching while keeping the public API ergonomic.
    */
  private[floatingUI] sealed trait BoundaryInternal

  private[floatingUI] object BoundaryInternal {

    /** Use clipping ancestors as boundary. */
    case object ClippingAncestors extends BoundaryInternal

    /** Single DOM element as boundary. */
    case class Element(element: dom.Element) extends BoundaryInternal

    /** Multiple DOM elements as boundaries. */
    case class Elements(elements: js.Array[dom.Element]) extends BoundaryInternal

    /** Custom rectangle as boundary. */
    case class CustomRect(rect: Rect) extends BoundaryInternal

    /** Convert public Boundary type to internal representation.
      *
      * @param boundary
      *   The boundary value from user
      * @return
      *   Internal representation for type-safe pattern matching
      */
    def fromBoundary(boundary: Boundary): BoundaryInternal = {
      // Use runtime type checking to determine which variant we have
      // This is necessary because Scala 3 union types don't support exhaustive matching
      (boundary: Any) match {
        case s: String if s == "clippingAncestors" =>
          ClippingAncestors

        case s: String =>
          // CSS selector - query the DOM
          val el = dom.document.querySelector(s)
          if (el != null) {
            Element(el.asInstanceOf[dom.Element])
          } else {
            // Fallback to clippingAncestors if selector doesn't match
            ClippingAncestors
          }

        case el: dom.Element =>
          Element(el)

        case arr: js.Array[?] =>
          // Type erasure means we can't check element type at runtime
          // Trust that user passed js.Array[dom.Element]
          Elements(arr.asInstanceOf[js.Array[dom.Element]])

        case rect: Rect =>
          CustomRect(rect)

        case _ =>
          // Fallback for unexpected types
          ClippingAncestors
      }
    }
  }

  /** Root boundary type for clipping detection.
    *
    * Matches TypeScript: `type RootBoundary = 'viewport' | 'document' | Rect`
    *
    * The root boundary defines the outermost clipping area for overflow detection. Valid values:
    *   - `"viewport"` - Use the browser viewport as the root boundary (default)
    *   - `"document"` - Use the entire document as the root boundary
    *   - `Rect` - Use a custom rectangle as the root boundary
    *
    * Examples:
    * {{{
    * // String literals (backward compatible)
    * val opts1 = FlipOptions(rootBoundary = "viewport")
    * val opts2 = ShiftOptions(rootBoundary = "document")
    *
    * // Custom Rect (new functionality)
    * val customRoot = Rect(x = 0, y = 0, width = 1920, height = 1080)
    * val opts3 = AutoPlacementOptions(rootBoundary = customRoot)
    * }}}
    *
    * @see
    *   https://floating-ui.com/docs/detectOverflow#rootboundary
    */
  type RootBoundary = String | Rect

  /** Internal representation of RootBoundary for type-safe pattern matching.
    *
    * This sealed trait is used internally to convert the union type `RootBoundary` into a form that supports exhaustive pattern matching.
    * Users should not interact with this type directly - use the `RootBoundary` type alias instead.
    */
  private[floatingUI] sealed trait RootBoundaryInternal

  private[floatingUI] object RootBoundaryInternal {

    /** Viewport root boundary - uses the browser viewport. */
    case object Viewport extends RootBoundaryInternal

    /** Document root boundary - uses the entire document. */
    case object Document extends RootBoundaryInternal

    /** Custom rectangle root boundary. */
    case class CustomRect(rect: Rect) extends RootBoundaryInternal

    /** Convert a RootBoundary union type to RootBoundaryInternal for pattern matching.
      *
      * Handles runtime type checking since Scala 3 union types don't support exhaustive matching.
      *
      * @param rootBoundary
      *   The root boundary value (String or Rect)
      * @return
      *   The internal representation for pattern matching
      */
    def fromRootBoundary(rootBoundary: RootBoundary): RootBoundaryInternal = {
      (rootBoundary: Any) match {
        case s: String if s == "viewport" => Viewport
        case s: String if s == "document" => Document
        case rect: Rect                   => CustomRect(rect)
        case _                            => Viewport // Fallback to default
      }
    }
  }

  /** Element context for overflow detection.
    *
    * Specifies which element (floating or reference) to check for overflow relative to a boundary.
    *
    * Matches TypeScript: type ElementContext = 'reference' | 'floating'
    *
    * @see
    *   https://floating-ui.com/docs/detectOverflow#elementcontext
    */
  enum ElementContext(val toValue: String) {

    /** Check overflow of the reference element.
      *
      * Used when you want to detect if the reference element itself is overflowing its boundary.
      */
    case Reference extends ElementContext("reference")

    /** Check overflow of the floating element (default).
      *
      * Used when you want to detect if the floating element is overflowing its boundary. This is the most common use case.
      */
    case Floating extends ElementContext("floating")
  }

  object ElementContext {

    /** Parse ElementContext from string value.
      *
      * @param value
      *   String value ("reference" or "floating")
      * @return
      *   Corresponding ElementContext enum value
      * @throws IllegalArgumentException
      *   if value is not a valid ElementContext
      */
    def fromString(value: String): ElementContext = value match {
      case "reference" => Reference
      case "floating"  => Floating
      case _           => throw new IllegalArgumentException(s"Invalid ElementContext: $value. Valid values are: 'reference', 'floating'")
    }
  }

  /** Hide strategy for determining when to hide the floating element.
    *
    * Specifies which hiding detection strategy to use when checking if the floating element should be hidden.
    *
    * Matches TypeScript: type HideStrategy = 'referenceHidden' | 'escaped'
    *
    * @see
    *   https://floating-ui.com/docs/hide
    */
  enum HideStrategy(val toValue: String) {

    /** Detect if the reference element is hidden or fully clipped.
      *
      * Checks if the reference element is not visible within its clipping boundary. This is useful for hiding the floating element when the
      * reference element is scrolled out of view or otherwise hidden.
      *
      * When this strategy is used, the middleware checks overflow with `elementContext = 'reference'`.
      */
    case ReferenceHidden extends HideStrategy("referenceHidden")

    /** Detect if the floating element has escaped its boundary.
      *
      * Checks if the floating element has overflowed outside its allowed boundary. This is useful for hiding the floating element when it
      * would appear outside the viewport or other boundary constraints.
      *
      * When this strategy is used, the middleware checks overflow with `altBoundary = true`.
      */
    case Escaped extends HideStrategy("escaped")
  }

  object HideStrategy {

    /** Parse HideStrategy from string value.
      *
      * @param value
      *   String value ("referenceHidden" or "escaped")
      * @return
      *   Corresponding HideStrategy enum value
      * @throws IllegalArgumentException
      *   if value is not a valid HideStrategy
      */
    def fromString(value: String): HideStrategy = value match {
      case "referenceHidden" => ReferenceHidden
      case "escaped"         => Escaped
      case _ => throw new IllegalArgumentException(s"Invalid HideStrategy: $value. Valid values are: 'referenceHidden', 'escaped'")
    }
  }

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
    def getClippingRect(element: Any, boundary: Boundary, rootBoundary: RootBoundary, strategy: Strategy): Rect

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
    boundary: Boundary = "clippingAncestors",
    rootBoundary: RootBoundary = "viewport",
    elementContext: ElementContext = ElementContext.Floating,
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
    fallbackStrategy: FallbackStrategy = FallbackStrategy.BestFit,
    fallbackAxisSideDirection: String = "none",
    flipAlignment: Boolean = true,
    // DetectOverflowOptions fields
    boundary: Boundary = "clippingAncestors",
    rootBoundary: RootBoundary = "viewport",
    elementContext: ElementContext = ElementContext.Floating,
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
    boundary: Boundary = "clippingAncestors",
    rootBoundary: RootBoundary = "viewport",
    elementContext: ElementContext = ElementContext.Floating,
    altBoundary: Boolean = false,
    padding: Derivable[Padding] = Left(0)
  )

  /** Options for hide middleware.
    *
    * Extends DetectOverflowOptions to include all boundary detection options.
    */
  case class HideOptions(
    // Hide-specific option
    strategy: HideStrategy = HideStrategy.ReferenceHidden,
    // DetectOverflowOptions fields
    boundary: Boundary = "clippingAncestors",
    rootBoundary: RootBoundary = "viewport",
    elementContext: ElementContext = ElementContext.Floating,
    altBoundary: Boolean = false,
    padding: Padding = 0
  )

  /** Options for size middleware.
    *
    * Extends DetectOverflowOptions to include all boundary detection options.
    */
  case class SizeOptions(
    // DetectOverflowOptions fields
    boundary: Boundary = "clippingAncestors",
    rootBoundary: RootBoundary = "viewport",
    elementContext: ElementContext = ElementContext.Floating,
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

  /** Detect overflow options.
    *
    * Matches TypeScript DetectOverflowOptions from @floating-ui/core
    */
  case class DetectOverflowOptions(
    /** The clipping boundary - can be "clippingAncestors", Element, Array[Element], or Rect.
      * @default
      *   "clippingAncestors"
      */
    boundary: Boundary = "clippingAncestors",
    /** The root clipping boundary - "viewport", "document", or custom Rect.
      *
      * Valid values:
      *   - `"viewport"` - Use the browser viewport as the root boundary (default)
      *   - `"document"` - Use the entire document as the root boundary
      *   - `Rect` - Use a custom rectangle as the root boundary
      *
      * @default
      *   "viewport"
      */
    rootBoundary: RootBoundary = "viewport",
    /** The element context - which element to check for overflow.
      * @default
      *   ElementContext.Floating
      */
    elementContext: ElementContext = ElementContext.Floating,
    /** Whether to use alternate boundary.
      * @default
      *   false
      */
    altBoundary: Boolean = false,
    /** Padding around the boundary.
      * @default
      *   0
      */
    padding: Padding = 0
  )
}
