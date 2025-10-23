/*
 * Scala.js facade types for @floating-ui/dom
 *
 * Based on @floating-ui/dom version 1.7.4
 * TypeScript definitions from floating-ui/packages/dom/src/
 *
 * Following Scala.js best practices from the scala-js codebase
 */

package www.facades.floatingui

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.|
import org.scalajs.dom

/** Scala.js facade types for @floating-ui/dom library.
  *
  * @floating-ui/dom
  *   provides positioning primitives for floating elements like tooltips, popovers, dropdowns, and more.
  *
  * @see
  *   https://floating-ui.com/docs/computePosition
  */
object FloatingUIDOM {

  // ============================================================================
  // Core Types from @floating-ui/utils
  // ============================================================================

  /** Alignment of the floating element relative to the reference element. */
  type Alignment = String // "start" | "end"

  /** Side of the reference element where the floating element is placed. */
  type Side = String // "top" | "right" | "bottom" | "left"

  /** Placement of the floating element. Can be a side ("top", "right", "bottom", "left") or a side with alignment ("top-start", "top-end",
    * "right-start", etc.)
    */
  type Placement = String

  /** Positioning strategy: "absolute" or "fixed". */
  type Strategy = String

  /** Axis for positioning: "x" or "y". */
  type Axis = String

  /** Coordinates on x and y axes. */
  @js.native
  trait Coords extends js.Object {
    val x: Double = js.native
    val y: Double = js.native
  }

  /** Dimensions (width and height). */
  @js.native
  trait Dimensions extends js.Object {
    val width: Double = js.native
    val height: Double = js.native
  }

  /** Rectangle with coordinates and dimensions. */
  @js.native
  trait Rect extends Coords with Dimensions

  /** Side object with values for each side. */
  @js.native
  trait SideObject extends js.Object {
    val top: Double = js.native
    val right: Double = js.native
    val bottom: Double = js.native
    val left: Double = js.native
  }

  /** Client rectangle object (combines Rect and SideObject). */
  @js.native
  trait ClientRectObject extends Rect with SideObject

  /** Element rectangles for reference and floating elements. */
  @js.native
  trait ElementRects extends js.Object {
    val reference: Rect = js.native
    val floating: Rect = js.native
  }

  // ============================================================================
  // DOM-specific Types
  // ============================================================================

  /** Virtual element for custom positioning reference.
    * @see
    *   https://floating-ui.com/docs/virtual-elements
    */
  @js.native
  trait VirtualElement extends js.Object {
    def getBoundingClientRect(): ClientRectObject = js.native
    val getClientRects: js.UndefOr[js.Function0[js.Array[ClientRectObject] | dom.DOMRectList]] = js.native
    val contextElement: js.UndefOr[dom.Element] = js.native
  }

  /** Reference element can be a DOM Element or VirtualElement. */
  type ReferenceElement = dom.Element | VirtualElement

  /** Floating element is always an HTMLElement. */
  type FloatingElement = dom.HTMLElement

  /** Elements object containing reference and floating elements. */
  @js.native
  trait Elements extends js.Object {
    val reference: ReferenceElement = js.native
    val floating: FloatingElement = js.native
  }

  /** Node scroll information. */
  @js.native
  trait NodeScroll extends js.Object {
    val scrollLeft: Double = js.native
    val scrollTop: Double = js.native
  }

  /** Boundary for clipping. */
  type Boundary = String | dom.Element | js.Array[dom.Element] | Rect

  /** Root boundary: "viewport" or "document". */
  type RootBoundary = String

  /** Element context: "reference" or "floating". */
  type ElementContext = String

  // ============================================================================
  // Middleware Types
  // ============================================================================

  /** Middleware data returned from middleware functions. */
  @js.native
  trait MiddlewareData extends js.Object {
    val arrow: js.UndefOr[ArrowData] = js.native
    val autoPlacement: js.UndefOr[AutoPlacementData] = js.native
    val flip: js.UndefOr[FlipData] = js.native
    val hide: js.UndefOr[HideData] = js.native
    val offset: js.UndefOr[OffsetData] = js.native
    val shift: js.UndefOr[ShiftData] = js.native
  }

  @js.native
  trait ArrowData extends js.Object {
    val x: js.UndefOr[Double] = js.native
    val y: js.UndefOr[Double] = js.native
    val centerOffset: Double = js.native
    val alignmentOffset: js.UndefOr[Double] = js.native
  }

  @js.native
  trait OverflowData extends js.Object {
    val placement: Placement = js.native
    val overflows: js.Array[Double] = js.native
  }

  @js.native
  trait AutoPlacementData extends js.Object {
    val index: js.UndefOr[Int] = js.native
    val overflows: js.Array[OverflowData] = js.native
  }

  @js.native
  trait FlipData extends js.Object {
    val index: js.UndefOr[Int] = js.native
    val overflows: js.Array[OverflowData] = js.native
  }

  @js.native
  trait HideData extends js.Object {
    val referenceHidden: js.UndefOr[Boolean] = js.native
    val escaped: js.UndefOr[Boolean] = js.native
    val referenceHiddenOffsets: js.UndefOr[SideObject] = js.native
    val escapedOffsets: js.UndefOr[SideObject] = js.native
  }

  @js.native
  trait OffsetData extends Coords {
    val placement: Placement = js.native
  }

  @js.native
  trait ShiftData extends Coords {
    val enabled: js.Dictionary[Boolean] = js.native
  }

  /** Middleware state passed to middleware functions. */
  @js.native
  trait MiddlewareState extends Coords {
    val initialPlacement: Placement = js.native
    val placement: Placement = js.native
    val strategy: Strategy = js.native
    val middlewareData: MiddlewareData = js.native
    val elements: Elements = js.native
    val rects: ElementRects = js.native
    val platform: Platform = js.native
  }

  /** Middleware return value. */
  @js.native
  trait MiddlewareReturn extends js.Object {
    val x: js.UndefOr[Double] = js.native
    val y: js.UndefOr[Double] = js.native
    val data: js.UndefOr[js.Dictionary[js.Any]] = js.native
    val reset: js.UndefOr[Boolean | ResetValue] = js.native
  }

  @js.native
  trait ResetValue extends js.Object {
    val placement: js.UndefOr[Placement] = js.native
    val rects: js.UndefOr[Boolean | ElementRects] = js.native
  }

  /** Middleware object. */
  @js.native
  trait Middleware extends js.Object {
    val name: String = js.native
    val options: js.UndefOr[js.Any] = js.native
    def fn(state: MiddlewareState): js.Promise[MiddlewareReturn] | MiddlewareReturn = js.native
  }

  // ============================================================================
  // Platform Interface
  // ============================================================================

  @js.native
  trait Platform extends js.Object {
    // Required methods
    def getElementRects(args: GetElementRectsArgs): js.Promise[ElementRects] | ElementRects = js.native
    def getClippingRect(args: GetClippingRectArgs): js.Promise[Rect] | Rect = js.native
    def getDimensions(element: dom.Element): js.Promise[Dimensions] | Dimensions = js.native

    // Optional methods
    val convertOffsetParentRelativeRectToViewportRelativeRect: js.UndefOr[js.Function1[ConvertOffsetArgs, js.Promise[Rect] | Rect]] =
      js.native
    val getOffsetParent: js.UndefOr[js.Function2[dom.Element, js.UndefOr[js.Function1[dom.HTMLElement, dom.Element | Null]], js.Promise[
      dom.Element | dom.Window
    ] | dom.Element | dom.Window]] = js.native
    val isElement: js.UndefOr[js.Function1[js.Any, js.Promise[Boolean] | Boolean]] = js.native
    val getDocumentElement: js.UndefOr[js.Function1[dom.Element, js.Promise[dom.HTMLElement] | dom.HTMLElement]] = js.native
    val getClientRects: js.UndefOr[js.Function1[dom.Element, js.Promise[js.Array[ClientRectObject]] | js.Array[ClientRectObject]]] =
      js.native
    val isRTL: js.UndefOr[js.Function1[dom.Element, js.Promise[Boolean] | Boolean]] = js.native
    val getScale: js.UndefOr[js.Function1[dom.HTMLElement, js.Promise[ScaleValue] | ScaleValue]] = js.native
  }

  @js.native
  trait GetElementRectsArgs extends js.Object {
    val reference: ReferenceElement = js.native
    val floating: FloatingElement = js.native
    val strategy: Strategy = js.native
  }

  @js.native
  trait GetClippingRectArgs extends js.Object {
    val element: dom.Element = js.native
    val boundary: Boundary = js.native
    val rootBoundary: RootBoundary = js.native
    val strategy: Strategy = js.native
  }

  @js.native
  trait ConvertOffsetArgs extends js.Object {
    val elements: js.UndefOr[Elements] = js.native
    val rect: Rect = js.native
    val offsetParent: dom.Element = js.native
    val strategy: Strategy = js.native
  }

  @js.native
  trait ScaleValue extends js.Object {
    val x: Double = js.native
    val y: Double = js.native
  }

  // ============================================================================
  // Configuration and Return Types
  // ============================================================================

  /** Configuration for computePosition. */
  @js.native
  trait ComputePositionConfig extends js.Object {
    val placement: js.UndefOr[Placement] = js.native
    val strategy: js.UndefOr[Strategy] = js.native
    val middleware: js.UndefOr[js.Array[Middleware | Null]] = js.native
    val platform: js.UndefOr[Platform] = js.native
  }

  /** Return value from computePosition. */
  @js.native
  trait ComputePositionReturn extends Coords {
    val placement: Placement = js.native
    val strategy: Strategy = js.native
    val middlewareData: MiddlewareData = js.native
  }

  // ============================================================================
  // Middleware Options
  // ============================================================================

  /** Options for offset middleware. */
  @js.native
  trait OffsetOptions extends js.Object {
    val mainAxis: js.UndefOr[Double] = js.native
    val crossAxis: js.UndefOr[Double] = js.native
    val alignmentAxis: js.UndefOr[Double | Null] = js.native
  }

  /** Options for arrow middleware. */
  @js.native
  trait ArrowOptions extends js.Object {
    val element: dom.HTMLElement = js.native
    val padding: js.UndefOr[Double | SideObject] = js.native
  }

  /** Options for shift middleware. */
  @js.native
  trait ShiftOptions extends js.Object {
    val mainAxis: js.UndefOr[Boolean] = js.native
    val crossAxis: js.UndefOr[Boolean] = js.native
    val limiter: js.UndefOr[js.Function1[MiddlewareState, Coords]] = js.native
  }

  /** Detect overflow options. */
  @js.native
  trait DetectOverflowOptions extends js.Object {
    val boundary: js.UndefOr[Boundary] = js.native
    val rootBoundary: js.UndefOr[RootBoundary] = js.native
    val elementContext: js.UndefOr[ElementContext] = js.native
    val altBoundary: js.UndefOr[Boolean] = js.native
    val padding: js.UndefOr[Double | SideObject] = js.native
  }

  /** Options for flip middleware. */
  @js.native
  trait FlipOptions extends DetectOverflowOptions {
    val mainAxis: js.UndefOr[Boolean] = js.native
    val crossAxis: js.UndefOr[Boolean] = js.native
    val fallbackAxisSideDirection: js.UndefOr[String] = js.native
    val flipAlignment: js.UndefOr[Boolean] = js.native
    val fallbackPlacements: js.UndefOr[js.Array[Placement]] = js.native
    val fallbackStrategy: js.UndefOr[String] = js.native
  }

  /** Options for autoPlacement middleware. */
  @js.native
  trait AutoPlacementOptions extends DetectOverflowOptions {
    val alignment: js.UndefOr[Alignment | Null] = js.native
    val allowedPlacements: js.UndefOr[js.Array[Placement]] = js.native
    val autoAlignment: js.UndefOr[Boolean] = js.native
  }

  /** Options for hide middleware. */
  @js.native
  trait HideOptions extends DetectOverflowOptions {
    val strategy: js.UndefOr[String] = js.native
  }

  /** Options for size middleware. */
  @js.native
  trait SizeOptions extends DetectOverflowOptions {
    @JSName("apply")
    val applyFn: js.UndefOr[js.Function1[SizeApplyArgs, Unit]] = js.native
  }

  @js.native
  trait SizeApplyArgs extends js.Object {
    val state: MiddlewareState = js.native
    val availableWidth: Double = js.native
    val availableHeight: Double = js.native
  }

  /** Options for inline middleware. */
  @js.native
  trait InlineOptions extends js.Object {
    val x: js.UndefOr[Double] = js.native
    val y: js.UndefOr[Double] = js.native
    val padding: js.UndefOr[Double | SideObject] = js.native
  }

  /** Options for autoUpdate. */
  @js.native
  trait AutoUpdateOptions extends js.Object {
    val ancestorScroll: js.UndefOr[Boolean] = js.native
    val ancestorResize: js.UndefOr[Boolean] = js.native
    val elementResize: js.UndefOr[Boolean] = js.native
    val layoutShift: js.UndefOr[Boolean] = js.native
    val animationFrame: js.UndefOr[Boolean] = js.native
  }

  // ============================================================================
  // Main API - computePosition
  // ============================================================================

  /** Computes the position of a floating element relative to a reference element.
    *
    * @param reference
    *   The reference element
    * @param floating
    *   The floating element to position
    * @param options
    *   Configuration options
    * @return
    *   A Promise that resolves to the computed position
    *
    * @see
    *   https://floating-ui.com/docs/computePosition
    */
  @js.native
  @JSImport("@floating-ui/dom", "computePosition")
  def computePosition(
    reference: ReferenceElement,
    floating: FloatingElement,
    options: js.UndefOr[ComputePositionConfig] = js.undefined
  ): js.Promise[ComputePositionReturn] = js.native

  // ============================================================================
  // Middleware Functions
  // ============================================================================

  /** Offset middleware - shifts the floating element from its reference element.
    *
    * @param options
    *   Offset configuration
    * @return
    *   Middleware object
    *
    * @see
    *   https://floating-ui.com/docs/offset
    */
  @js.native
  @JSImport("@floating-ui/dom", "offset")
  def offset(
    options: js.UndefOr[Double | OffsetOptions | js.Function1[MiddlewareState, Double | OffsetOptions]] = js.undefined
  ): Middleware = js.native

  /** Arrow middleware - positions an arrow element.
    *
    * @param options
    *   Arrow configuration
    * @return
    *   Middleware object
    *
    * @see
    *   https://floating-ui.com/docs/arrow
    */
  @js.native
  @JSImport("@floating-ui/dom", "arrow")
  def arrow(options: ArrowOptions): Middleware = js.native

  /** Shift middleware - shifts the floating element to keep it in view.
    *
    * @param options
    *   Shift configuration
    * @return
    *   Middleware object
    *
    * @see
    *   https://floating-ui.com/docs/shift
    */
  @js.native
  @JSImport("@floating-ui/dom", "shift")
  def shift(options: js.UndefOr[ShiftOptions] = js.undefined): Middleware = js.native

  /** Flip middleware - flips the floating element to keep it in view.
    *
    * @param options
    *   Flip configuration
    * @return
    *   Middleware object
    *
    * @see
    *   https://floating-ui.com/docs/flip
    */
  @js.native
  @JSImport("@floating-ui/dom", "flip")
  def flip(options: js.UndefOr[FlipOptions] = js.undefined): Middleware = js.native

  /** AutoPlacement middleware - automatically chooses the placement.
    *
    * @param options
    *   AutoPlacement configuration
    * @return
    *   Middleware object
    *
    * @see
    *   https://floating-ui.com/docs/autoPlacement
    */
  @js.native
  @JSImport("@floating-ui/dom", "autoPlacement")
  def autoPlacement(options: js.UndefOr[AutoPlacementOptions] = js.undefined): Middleware = js.native

  /** Hide middleware - hides the floating element when appropriate.
    *
    * @param options
    *   Hide configuration
    * @return
    *   Middleware object
    *
    * @see
    *   https://floating-ui.com/docs/hide
    */
  @js.native
  @JSImport("@floating-ui/dom", "hide")
  def hide(options: js.UndefOr[HideOptions] = js.undefined): Middleware = js.native

  /** Size middleware - resizes the floating element.
    *
    * @param options
    *   Size configuration
    * @return
    *   Middleware object
    *
    * @see
    *   https://floating-ui.com/docs/size
    */
  @js.native
  @JSImport("@floating-ui/dom", "size")
  def size(options: js.UndefOr[SizeOptions] = js.undefined): Middleware = js.native

  /** Inline middleware - improves positioning for inline reference elements.
    *
    * @param options
    *   Inline configuration
    * @return
    *   Middleware object
    *
    * @see
    *   https://floating-ui.com/docs/inline
    */
  @js.native
  @JSImport("@floating-ui/dom", "inline")
  def inline(options: js.UndefOr[InlineOptions] = js.undefined): Middleware = js.native

  /** AutoUpdate - automatically updates the position when needed.
    *
    * @param reference
    *   The reference element
    * @param floating
    *   The floating element
    * @param update
    *   The update function to call
    * @param options
    *   AutoUpdate configuration
    * @return
    *   Cleanup function
    *
    * @see
    *   https://floating-ui.com/docs/autoUpdate
    */
  @js.native
  @JSImport("@floating-ui/dom", "autoUpdate")
  def autoUpdate(
    reference: ReferenceElement,
    floating: FloatingElement,
    update: js.Function0[Unit],
    options: js.UndefOr[AutoUpdateOptions] = js.undefined
  ): js.Function0[Unit] = js.native

  /** Platform object for DOM environment.
    *
    * @see
    *   https://floating-ui.com/docs/platform
    */
  @js.native
  @JSImport("@floating-ui/dom", "platform")
  val platform: Platform = js.native

  /** Detect overflow utility.
    *
    * @param state
    *   Middleware state
    * @param options
    *   Detect overflow options
    * @return
    *   Promise resolving to side object with overflow values
    */
  @js.native
  @JSImport("@floating-ui/dom", "detectOverflow")
  def detectOverflow(
    state: MiddlewareState,
    options: js.UndefOr[DetectOverflowOptions] = js.undefined
  ): js.Promise[SideObject] = js.native
}
