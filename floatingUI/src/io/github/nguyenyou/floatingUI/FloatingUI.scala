package io.github.nguyenyou.floatingUI

import org.scalajs.dom
import Types.*
import ComputePosition.*
import middleware.*
import AutoUpdate.*

/** Main API for floating element positioning.
  *
  * This is a native Scala.js implementation of floating-ui/dom, eliminating the need for the external JavaScript dependency.
  */
object FloatingUI {

  // Re-export types
  export Types.*
  export AutoUpdate.AutoUpdateOptions

  // Re-export platform
  val platform: Platform = DOMPlatform

  // ============================================================================
  // Main API
  // ============================================================================

  /** Computes the position of a floating element relative to a reference element.
    *
    * @param reference
    *   The reference element (can be a DOM element or a VirtualElement)
    * @param floating
    *   The floating element to position
    * @param placement
    *   The desired placement (default: "bottom")
    * @param strategy
    *   The positioning strategy (default: "absolute")
    * @param middleware
    *   Array of middleware to apply
    * @return
    *   The computed position
    */
  def computePosition(
    reference: ReferenceElement,
    floating: dom.HTMLElement,
    placement: Placement = "bottom",
    strategy: Strategy = "absolute",
    middleware: Seq[Middleware] = Seq.empty
  ): ComputePositionReturn = {
    // Cache strategy matches TypeScript implementation exactly:
    // - Creates a new Map (not WeakMap) for each computePosition call
    // - Cache lives only for a single call to handle middleware resets
    // - Cache is cleared after computation completes
    // See: @floating-ui/dom/src/index.ts lines 19-28
    //
    // This caches the expensive `getClippingElementAncestors` function so that
    // multiple lifecycle resets re-use the same result. It only lives for a
    // single call. If other functions become expensive, we can add them as well.
    val cache = scala.collection.mutable.Map[ReferenceElement, Seq[dom.Element]]()

    // Inject cache into platform
    platform._c = Some(cache)

    val config = ComputePositionConfig(
      placement = placement,
      strategy = strategy,
      middleware = middleware,
      platform = platform
    )

    val result = ComputePosition.computePosition(reference, floating, config)

    // Clear cache after computation
    platform._c = None

    result
  }

  // ============================================================================
  // Middleware Functions
  // ============================================================================

  /** Offset middleware - shifts the floating element from its reference element.
    *
    * @param options
    *   Offset configuration (can be a number, object, or derivable function). Default is 0.
    * @return
    *   Middleware object
    */
  def offset(options: OffsetOptions = Left(Left(0))): Middleware = {
    OffsetMiddleware.offset(options)
  }

  /** Shift middleware - shifts the floating element to keep it in view.
    *
    * @param options
    *   Shift configuration (can be static or derivable from state)
    * @return
    *   Middleware object
    */
  def shift(options: Derivable[ShiftOptions] = Left(ShiftOptions())): Middleware = {
    ShiftMiddleware.shift(options)
  }

  /** Flip middleware - flips the placement to keep it in view.
    *
    * @param options
    *   Flip configuration (can be static or derivable from state)
    * @return
    *   Middleware object
    */
  def flip(options: Derivable[FlipOptions] = Left(FlipOptions())): Middleware = {
    FlipMiddleware.flip(options)
  }

  /** Arrow middleware - positions an arrow element.
    *
    * @param options
    *   Arrow configuration (can be static or derivable from state)
    * @return
    *   Middleware object
    */
  def arrow(options: Derivable[ArrowOptions]): Middleware = {
    ArrowMiddleware.arrow(options)
  }

  /** AutoPlacement middleware - automatically chooses best placement based on available space.
    *
    * @param options
    *   AutoPlacement configuration (can be static or derivable from state)
    * @return
    *   Middleware object
    */
  def autoPlacement(options: Derivable[AutoPlacementOptions] = Left(AutoPlacementOptions())): Middleware = {
    AutoPlacementMiddleware.autoPlacement(options)
  }

  /** Hide middleware - provides data to hide floating element when clipped.
    *
    * @param options
    *   Hide configuration (can be static or derivable from state)
    * @return
    *   Middleware object
    */
  def hide(options: Derivable[HideOptions] = Left(HideOptions())): Middleware = {
    HideMiddleware.hide(options)
  }

  /** Size middleware - allows resizing floating element based on available space.
    *
    * @param options
    *   Size configuration (can be static or derivable from state)
    * @return
    *   Middleware object
    */
  def size(options: Derivable[SizeOptions] = Left(SizeOptions())): Middleware = {
    SizeMiddleware.size(options)
  }

  /** Inline middleware - improved positioning for inline elements.
    *
    * @param options
    *   Inline configuration (can be static or derivable from state)
    * @return
    *   Middleware object
    */
  def inline(options: Derivable[InlineOptions] = Left(InlineOptions())): Middleware = {
    InlineMiddleware.inline(options)
  }

  /** LimitShift - built-in limiter that will stop shift() at a certain point.
    *
    * @param options
    *   LimitShift configuration (can be static or derivable from state)
    * @return
    *   Limiter object
    */
  def limitShift(options: Derivable[LimitShiftOptions] = Left(LimitShiftOptions())): Limiter = {
    ShiftMiddleware.limitShift(options)
  }

  // ============================================================================
  // Auto Update
  // ============================================================================

  /** Automatically updates the position of the floating element when necessary.
    *
    * Should only be called when the floating element is mounted on the DOM or visible on the screen.
    *
    * @param reference
    *   The reference element (can be a DOM element or a VirtualElement)
    * @param floating
    *   The floating element
    * @param update
    *   The update callback to call when repositioning is needed
    * @param options
    *   Configuration options
    * @return
    *   Cleanup function that should be invoked when the floating element is removed from the DOM or hidden from the screen
    */
  def autoUpdate(
    reference: ReferenceElement,
    floating: dom.HTMLElement,
    update: () => Unit,
    options: AutoUpdateOptions = AutoUpdateOptions()
  ): () => Unit = {
    AutoUpdate.autoUpdate(reference, floating, update, options)
  }

  // ============================================================================
  // Utility Functions
  // ============================================================================

  /** Get all overflow ancestors for a node.
    *
    * Returns all ancestor elements that can cause overflow (scrollable containers, etc.).
    *
    * @param node
    *   The node to get overflow ancestors for
    * @return
    *   Sequence of overflow ancestor elements
    */
  def getOverflowAncestors(node: dom.Node): Seq[dom.EventTarget] = {
    Utils.getOverflowAncestors(node)
  }

  /** Detect overflow of the floating element relative to its clipping boundary.
    *
    * Returns the overflow amount on each side of the floating element.
    *
    * @param state
    *   The current middleware state
    * @param options
    *   Detection options (can be static or derivable from state)
    * @return
    *   SideObject containing overflow amounts for each side
    */
  def detectOverflow(
    state: MiddlewareState,
    options: Derivable[DetectOverflowOptions] = Left(DetectOverflowOptions())
  ): SideObject = {
    DetectOverflow.detectOverflow(state, options)
  }
}
