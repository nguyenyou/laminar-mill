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

  /** Alignment of the floating element relative to the reference element.
    *
    * Specifies whether the floating element should align to the start or end of the reference element along the cross axis.
    *
    * Matches TypeScript: type Alignment = 'start' | 'end'
    *
    * @see
    *   https://floating-ui.com/docs/computePosition#placement
    */
  enum Alignment(val toValue: String) {

    /** Align to the start of the reference element.
      *
      * For horizontal placements (top/bottom), this means left alignment. For vertical placements (left/right), this means top alignment.
      */
    case Start extends Alignment("start")

    /** Align to the end of the reference element.
      *
      * For horizontal placements (top/bottom), this means right alignment. For vertical placements (left/right), this means bottom
      * alignment.
      */
    case End extends Alignment("end")
  }

  object Alignment {

    /** Parse Alignment from string value.
      *
      * @param value
      *   String value ("start" or "end")
      * @return
      *   Corresponding Alignment enum value
      * @throws IllegalArgumentException
      *   if value is not a valid Alignment
      */
    def fromString(value: String): Alignment = value match {
      case "start" => Start
      case "end"   => End
      case _       => throw new IllegalArgumentException(s"Invalid Alignment: $value. Valid values are: 'start', 'end'")
    }
  }

  /** Side of the reference element where the floating element is placed.
    *
    * Specifies the primary side (top, right, bottom, or left) where the floating element should be positioned relative to the reference
    * element.
    *
    * Matches TypeScript: type Side = 'top' | 'right' | 'bottom' | 'left'
    *
    * @see
    *   https://floating-ui.com/docs/computePosition#placement
    */
  enum Side(val toValue: String) {

    /** Place floating element above the reference element. */
    case Top extends Side("top")

    /** Place floating element to the right of the reference element. */
    case Right extends Side("right")

    /** Place floating element below the reference element. */
    case Bottom extends Side("bottom")

    /** Place floating element to the left of the reference element. */
    case Left extends Side("left")
  }

  object Side {

    /** Parse Side from string value.
      *
      * @param value
      *   String value ("top", "right", "bottom", or "left")
      * @return
      *   Corresponding Side enum value
      * @throws IllegalArgumentException
      *   if value is not a valid Side
      */
    def fromString(value: String): Side = value match {
      case "top"    => Top
      case "right"  => Right
      case "bottom" => Bottom
      case "left"   => Left
      case _        => throw new IllegalArgumentException(s"Invalid Side: $value. Valid values are: 'top', 'right', 'bottom', 'left'")
    }

    /** All sides in order. */
    val all: Seq[Side] = Seq(Top, Right, Bottom, Left)
  }

  /** Placement of the floating element relative to the reference element.
    *
    * Specifies where the floating element should be positioned. Can be a side (top, right, bottom, left) or a side with alignment (e.g.,
    * top-start, top-end).
    *
    * Matches TypeScript: type Placement = 'top' | 'top-start' | 'top-end' | 'right' | 'right-start' | 'right-end' | 'bottom' |
    * 'bottom-start' | 'bottom-end' | 'left' | 'left-start' | 'left-end'
    *
    * @see
    *   https://floating-ui.com/docs/computePosition#placement
    */
  enum Placement(val toValue: String) {

    /** Place above, centered. */
    case Top extends Placement("top")

    /** Place above, aligned to start. */
    case TopStart extends Placement("top-start")

    /** Place above, aligned to end. */
    case TopEnd extends Placement("top-end")

    /** Place to the right, centered. */
    case Right extends Placement("right")

    /** Place to the right, aligned to start. */
    case RightStart extends Placement("right-start")

    /** Place to the right, aligned to end. */
    case RightEnd extends Placement("right-end")

    /** Place below, centered. */
    case Bottom extends Placement("bottom")

    /** Place below, aligned to start. */
    case BottomStart extends Placement("bottom-start")

    /** Place below, aligned to end. */
    case BottomEnd extends Placement("bottom-end")

    /** Place to the left, centered. */
    case Left extends Placement("left")

    /** Place to the left, aligned to start. */
    case LeftStart extends Placement("left-start")

    /** Place to the left, aligned to end. */
    case LeftEnd extends Placement("left-end")
  }

  object Placement {

    /** Parse Placement from string value.
      *
      * @param value
      *   String value (e.g., "top", "top-start", "bottom-end")
      * @return
      *   Corresponding Placement enum value
      * @throws IllegalArgumentException
      *   if value is not a valid Placement
      */
    def fromString(value: String): Placement = value match {
      case "top"          => Top
      case "top-start"    => TopStart
      case "top-end"      => TopEnd
      case "right"        => Right
      case "right-start"  => RightStart
      case "right-end"    => RightEnd
      case "bottom"       => Bottom
      case "bottom-start" => BottomStart
      case "bottom-end"   => BottomEnd
      case "left"         => Left
      case "left-start"   => LeftStart
      case "left-end"     => LeftEnd
      case _ =>
        throw new IllegalArgumentException(
          s"Invalid Placement: $value. Valid values are: 'top', 'top-start', 'top-end', 'right', 'right-start', 'right-end', 'bottom', 'bottom-start', 'bottom-end', 'left', 'left-start', 'left-end'"
        )
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

  /** Positioning strategy for the floating element.
    *
    * Specifies the CSS position property to use for positioning the floating element.
    *
    * Matches TypeScript: type Strategy = 'absolute' | 'fixed'
    *
    * @see
    *   https://floating-ui.com/docs/computePosition#strategy
    */
  enum Strategy(val toValue: String) {

    /** Use CSS absolute positioning.
      *
      * The floating element is positioned relative to its nearest positioned ancestor. This is the default and most common strategy.
      */
    case Absolute extends Strategy("absolute")

    /** Use CSS fixed positioning.
      *
      * The floating element is positioned relative to the viewport. Useful when the floating element needs to stay in place during
      * scrolling.
      */
    case Fixed extends Strategy("fixed")
  }

  object Strategy {

    /** Parse Strategy from string value.
      *
      * @param value
      *   String value ("absolute" or "fixed")
      * @return
      *   Corresponding Strategy enum value
      * @throws IllegalArgumentException
      *   if value is not a valid Strategy
      */
    def fromString(value: String): Strategy = value match {
      case "absolute" => Absolute
      case "fixed"    => Fixed
      case _          => throw new IllegalArgumentException(s"Invalid Strategy: $value. Valid values are: 'absolute', 'fixed'")
    }
  }

  /** Axis for positioning and measurements.
    *
    * Specifies the horizontal (x) or vertical (y) axis for positioning calculations.
    *
    * Matches TypeScript: type Axis = 'x' | 'y'
    *
    * @see
    *   https://floating-ui.com/docs/detectOverflow
    */
  enum Axis(val toValue: String) {

    /** Horizontal axis (left-right direction). */
    case X extends Axis("x")

    /** Vertical axis (top-bottom direction). */
    case Y extends Axis("y")
  }

  object Axis {

    /** Parse Axis from string value.
      *
      * @param value
      *   String value ("x" or "y")
      * @return
      *   Corresponding Axis enum value
      * @throws IllegalArgumentException
      *   if value is not a valid Axis
      */
    def fromString(value: String): Axis = value match {
      case "x" => X
      case "y" => Y
      case _   => throw new IllegalArgumentException(s"Invalid Axis: $value. Valid values are: 'x', 'y'")
    }
  }

  /** Length dimension for size measurements.
    *
    * Specifies whether to measure width (horizontal) or height (vertical) dimension.
    *
    * Matches TypeScript: type Length = 'width' | 'height'
    *
    * @see
    *   https://floating-ui.com/docs/size
    */
  enum Length(val toValue: String) {

    /** Width dimension (horizontal measurement). */
    case Width extends Length("width")

    /** Height dimension (vertical measurement). */
    case Height extends Length("height")
  }

  object Length {

    /** Parse Length from string value.
      *
      * @param value
      *   String value ("width" or "height")
      * @return
      *   Corresponding Length enum value
      * @throws IllegalArgumentException
      *   if value is not a valid Length
      */
    def fromString(value: String): Length = value match {
      case "width"  => Width
      case "height" => Height
      case _        => throw new IllegalArgumentException(s"Invalid Length: $value. Valid values are: 'width', 'height'")
    }
  }

  /** Fallback strategy for flip middleware when no placements fit.
    *
    * Specifies what to do when none of the allowed placements fit within the boundary.
    *
    * Matches TypeScript: type FallbackStrategy = 'bestFit' | 'initialPlacement'
    *
    * @see
    *   https://floating-ui.com/docs/flip#fallbackstrategy
    */
  enum FallbackStrategy(val toValue: String) {

    /** Use the placement that has the most space available.
      *
      * When no placements fit, choose the one with the largest available space. This is the default behavior.
      */
    case BestFit extends FallbackStrategy("bestFit")

    /** Use the initial placement regardless of available space.
      *
      * When no placements fit, keep the initial placement even if it overflows. Useful when you want predictable positioning.
      */
    case InitialPlacement extends FallbackStrategy("initialPlacement")
  }

  object FallbackStrategy {

    /** Parse FallbackStrategy from string value.
      *
      * @param value
      *   String value ("bestFit" or "initialPlacement")
      * @return
      *   Corresponding FallbackStrategy enum value
      * @throws IllegalArgumentException
      *   if value is not a valid FallbackStrategy
      */
    def fromString(value: String): FallbackStrategy = value match {
      case "bestFit"          => BestFit
      case "initialPlacement" => InitialPlacement
      case _ =>
        throw new IllegalArgumentException(s"Invalid FallbackStrategy: $value. Valid values are: 'bestFit', 'initialPlacement'")
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

  /** Middleware object for customizing positioning behavior.
    *
    * Middleware allows you to customize the positioning logic and add features beyond basic placement. Each middleware has a unique name
    * and a function that processes the positioning state.
    *
    * The library provides 8 built-in middleware:
    *   - **"offset"** - Displaces the floating element from its reference element
    *   - **"flip"** - Flips the placement to keep the floating element in view
    *   - **"shift"** - Shifts the floating element along an axis to keep it in view
    *   - **"hide"** - Provides data to hide the floating element when appropriate
    *   - **"size"** - Resizes the floating element based on available space
    *   - **"arrow"** - Positions an inner arrow element to point at the reference
    *   - **"autoPlacement"** - Automatically chooses the best placement
    *   - **"inline"** - Handles inline reference elements spanning multiple lines
    *
    * Custom middleware can use any name. The name is used as a key to store middleware-specific data in `MiddlewareData.custom`.
    *
    * Matches TypeScript: type Middleware = { name: string; options?: any; fn: (state: MiddlewareState) => Promisable<MiddlewareReturn> }
    *
    * @see
    *   https://floating-ui.com/docs/middleware
    */
  trait Middleware {

    /** Unique identifier for this middleware.
      *
      * Built-in middleware use fixed names (e.g., "offset", "flip"). Custom middleware can use any string value. The name is used to
      * organize middleware data in the `MiddlewareData` object.
      */
    def name: String

    /** Middleware function that processes positioning state.
      *
      * @param state
      *   Current positioning state including coordinates, placement, rects, and platform
      * @return
      *   Middleware return value with optional coordinate adjustments, data, or reset instructions
      */
    def fn(state: MiddlewareState): MiddlewareReturn
  }

  /** Convenience constants for built-in middleware names.
    *
    * These constants provide type-safe references to the names of the 8 built-in middleware. They are provided for convenience and
    * documentation purposes.
    *
    * Note: Custom middleware can use any name not in this list. These constants are not exhaustive - they only cover the built-in
    * middleware provided by the library.
    *
    * @see
    *   https://floating-ui.com/docs/middleware
    */
  object MiddlewareNames {

    /** Offset middleware name: "offset"
      *
      * Displaces the floating element from its reference element by a specified distance.
      *
      * @see
      *   https://floating-ui.com/docs/offset
      */
    val Offset: String = "offset"

    /** Flip middleware name: "flip"
      *
      * Flips the placement to the opposite side to keep the floating element in view.
      *
      * @see
      *   https://floating-ui.com/docs/flip
      */
    val Flip: String = "flip"

    /** Shift middleware name: "shift"
      *
      * Shifts the floating element along an axis to keep it in view when it would overflow.
      *
      * @see
      *   https://floating-ui.com/docs/shift
      */
    val Shift: String = "shift"

    /** Hide middleware name: "hide"
      *
      * Provides data to determine when the floating element should be hidden.
      *
      * @see
      *   https://floating-ui.com/docs/hide
      */
    val Hide: String = "hide"

    /** Size middleware name: "size"
      *
      * Provides data to resize the floating element based on available space.
      *
      * @see
      *   https://floating-ui.com/docs/size
      */
    val Size: String = "size"

    /** Arrow middleware name: "arrow"
      *
      * Positions an inner arrow element to point at the reference element.
      *
      * @see
      *   https://floating-ui.com/docs/arrow
      */
    val Arrow: String = "arrow"

    /** AutoPlacement middleware name: "autoPlacement"
      *
      * Automatically chooses the placement with the most available space.
      *
      * @see
      *   https://floating-ui.com/docs/autoPlacement
      */
    val AutoPlacement: String = "autoPlacement"

    /** Inline middleware name: "inline"
      *
      * Improves positioning for inline reference elements that span multiple lines.
      *
      * @see
      *   https://floating-ui.com/docs/inline
      */
    val Inline: String = "inline"
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

  /** Cache type for clipping element ancestors (per-call Map keyed by DOM element). */
  type ClippingCache = scala.collection.mutable.Map[dom.Element, Seq[dom.Element]]

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
    var _c: Option[ClippingCache] = None

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

  /** Cross-axis option for flip middleware - matches `true | false | 'alignment'`. */
  enum FlipCrossAxis {
    case All
    case None
    case Alignment
  }

  object FlipCrossAxis {

    /** Convenience helper to convert from a Boolean value. */
    def fromBoolean(value: Boolean): FlipCrossAxis = if (value) FlipCrossAxis.All else FlipCrossAxis.None
  }

  /** Direction for `fallbackAxisSideDirection`. */
  enum FallbackAxisSideDirection(val toValue: String) {
    case None extends FallbackAxisSideDirection("none")
    case Start extends FallbackAxisSideDirection("start")
    case End extends FallbackAxisSideDirection("end")
  }

  object FallbackAxisSideDirection {

    /** Parse direction from string literal. */
    def fromString(value: String): FallbackAxisSideDirection = value match {
      case "none"  => None
      case "start" => Start
      case "end"   => End
      case _ =>
        throw new IllegalArgumentException("Invalid FallbackAxisSideDirection: " + value + ". Valid values: 'none', 'start', 'end'")
    }
  }

  /** Options for flip middleware.
    *
    * Extends DetectOverflowOptions to include all boundary detection options.
    */
  case class FlipOptions(
    // Flip-specific options
    mainAxis: Boolean = true,
    crossAxis: FlipCrossAxis = FlipCrossAxis.All,
    fallbackPlacements: Option[Seq[Placement]] = None,
    fallbackStrategy: FallbackStrategy = FallbackStrategy.BestFit,
    fallbackAxisSideDirection: FallbackAxisSideDirection = FallbackAxisSideDirection.None,
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
