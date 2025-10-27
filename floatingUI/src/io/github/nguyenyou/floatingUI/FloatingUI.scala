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
    *   The reference element
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
    reference: dom.Element,
    floating: dom.HTMLElement,
    placement: Placement = "bottom",
    strategy: Strategy = "absolute",
    middleware: Seq[Middleware] = Seq.empty
  ): ComputePositionReturn = {
    val config = ComputePositionConfig(
      placement = placement,
      strategy = strategy,
      middleware = middleware,
      platform = platform
    )
    ComputePosition.computePosition(reference, floating, config)
  }

  // ============================================================================
  // Middleware Functions
  // ============================================================================

  /** Offset middleware - shifts the floating element from its reference element.
    *
    * @param value
    *   The offset distance (default: 0)
    * @return
    *   Middleware object
    */
  def offset(value: Double = 0): Middleware = {
    OffsetMiddleware.offset(OffsetOptions(mainAxis = value))
  }

  /** Offset middleware with detailed options.
    *
    * @param options
    *   Offset configuration
    * @return
    *   Middleware object
    */
  def offset(options: OffsetOptions): Middleware = {
    OffsetMiddleware.offset(options)
  }

  /** Shift middleware - shifts the floating element to keep it in view.
    *
    * @param options
    *   Shift configuration
    * @return
    *   Middleware object
    */
  def shift(options: ShiftOptions = ShiftOptions()): Middleware = {
    ShiftMiddleware.shift(options)
  }

  /** Flip middleware - flips the placement to keep it in view.
    *
    * @param options
    *   Flip configuration
    * @return
    *   Middleware object
    */
  def flip(options: FlipOptions = FlipOptions()): Middleware = {
    FlipMiddleware.flip(options)
  }

  /** Arrow middleware - positions an arrow element.
    *
    * @param element
    *   The arrow element
    * @param padding
    *   Padding around the arrow (default: 0)
    * @return
    *   Middleware object
    */
  def arrow(element: dom.HTMLElement, padding: Double = 0): Middleware = {
    ArrowMiddleware.arrow(ArrowOptions(element = element, padding = padding))
  }

  /** Arrow middleware with detailed options.
    *
    * @param options
    *   Arrow configuration
    * @return
    *   Middleware object
    */
  def arrow(options: ArrowOptions): Middleware = {
    ArrowMiddleware.arrow(options)
  }

  // ============================================================================
  // Auto Update
  // ============================================================================

  /** Automatically updates the position of the floating element when necessary.
    *
    * Should only be called when the floating element is mounted on the DOM or visible on the screen.
    *
    * @param reference
    *   The reference element
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
    reference: dom.Element,
    floating: dom.HTMLElement,
    update: () => Unit,
    options: AutoUpdateOptions = AutoUpdateOptions()
  ): () => Unit = {
    AutoUpdate.autoUpdate(reference, floating, update, options)
  }
}
