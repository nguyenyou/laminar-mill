package io.github.nguyenyou.floatingUI

import org.scalajs.dom
import scala.scalajs.js
import Utils.*
import DOMUtils.{getBoundingClientRect => domGetBoundingClientRect}
import Types.{ClientRectObject, ReferenceElement}

/*

Automatically updates the position of the floating element when necessary to ensure it stays anchored.

To ensure the floating element remains anchored to its reference element, such as when scrolling and resizing the screen, its position needs to be continually updated when necessary.

To solve this, autoUpdate() adds listeners that will automatically call an update function which invokes computePosition() when necessary. Updates typically take only ~1ms.

It’s important that this function is only called/set-up when the floating element is open on the screen, and cleaned up when it’s removed. Otherwise, it can cause severe performance degradation, especially with many floating elements being created.

Call autoUpdate() only when the floating element is open or mounted:

// This function will get called repeatedly.
function updatePosition() {
  computePosition(referenceEl, floatingEl).then(({x, y}) => {
    // ...
  });
}

// Mount the floating element to the DOM, such as on hover or click
document.body.append(floatingEl);

// Start auto updates.
const cleanup = autoUpdate(
  referenceEl,
  floatingEl,
  updatePosition,
);

Then, when the user unhovers or clicks away,
unmount the floating element and ensure you call the cleanup function
to stop the auto updates:

// Remove the floating element from the DOM, such as on blur
// or outside click.
floatingEl.remove();
// Stop auto updates.
cleanup();

 */

/** AutoUpdate functionality for floating elements.
  *
  * Automatically updates the position of floating elements when necessary, such as when scrolling, resizing, or layout shifts occur.
  *
  * Ported from @floating-ui/dom/src/autoUpdate.ts
  */
object AutoUpdate {

  /** Options for autoUpdate configuration.
    *
    * Note: The defaults for elementResize and layoutShift are set in the autoUpdate function based on API availability, matching the
    * TypeScript implementation.
    */
  case class AutoUpdateOptions(
    ancestorScroll: Boolean = true,
    ancestorResize: Boolean = true,
    elementResize: Option[Boolean] = None, // Default determined by ResizeObserver availability
    layoutShift: Option[Boolean] = None, // Default determined by IntersectionObserver availability
    animationFrame: Boolean = false
  )

  /** Cleanup function type returned by autoUpdate. */
  type CleanupFn = () => Unit

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
  ): CleanupFn = {

    // Apply conditional defaults based on API availability (matching TypeScript)
    val ancestorScroll = options.ancestorScroll
    val ancestorResize = options.ancestorResize
    val elementResize = options.elementResize.getOrElse(
      js.typeOf(js.Dynamic.global.ResizeObserver) == "function"
    )
    val layoutShift = options.layoutShift.getOrElse(
      js.typeOf(js.Dynamic.global.IntersectionObserver) == "function"
    )
    val animationFrame = options.animationFrame

    // For virtual elements, use the context element for ancestor tracking
    val referenceElement = unwrapElement(reference)

    // Collect all ancestor elements that can affect positioning
    val ancestors = if (ancestorScroll || ancestorResize) {
      val refAncestors = if (referenceElement != null) {
        getOverflowAncestors(referenceElement)
      } else {
        Seq.empty
      }
      refAncestors ++ getOverflowAncestors(floating)
    } else {
      Seq.empty
    }

    // Set up scroll and resize listeners on ancestors
    val scrollHandler: js.Function1[dom.Event, Unit] = (_: dom.Event) => update()
    val resizeHandler: js.Function1[dom.Event, Unit] = (_: dom.Event) => update()

    // Create options for passive scroll listeners (improves performance)
    val scrollOptions = js.Dynamic.literal("passive" -> true).asInstanceOf[dom.EventListenerOptions]

    ancestors.foreach { ancestor =>
      if (ancestorScroll) {
        ancestor.addEventListener("scroll", scrollHandler, scrollOptions)
      }
      if (ancestorResize) {
        ancestor.addEventListener("resize", resizeHandler, useCapture = false)
      }
    }

    // Set up ResizeObserver for element resize detection
    var resizeObserver: Option[dom.ResizeObserver] = None
    var reobserveFrame: Int = -1

    if (elementResize) {
      val resizeCallback: js.Function2[js.Array[dom.ResizeObserverEntry], dom.ResizeObserver, Unit] =
        (entries: js.Array[dom.ResizeObserverEntry], _: dom.ResizeObserver) => {
          val firstEntry = entries(0)
          // Compare with referenceElement (unwrapped DOM element), not reference
          if (firstEntry != null && firstEntry.target == referenceElement && resizeObserver.isDefined) {
            // Prevent update loops when using the size middleware
            resizeObserver.foreach(_.unobserve(floating))
            dom.window.cancelAnimationFrame(reobserveFrame)
            reobserveFrame = dom.window.requestAnimationFrame { (_: Double) =>
              resizeObserver.foreach(_.observe(floating))
            }
          }
          update()
        }

      val observer = new dom.ResizeObserver(resizeCallback)

      if (!animationFrame && referenceElement != null) {
        observer.observe(referenceElement)
      }
      observer.observe(floating)
      resizeObserver = Some(observer)
    }

    // Set up IntersectionObserver for layout shift detection
    var cleanupIo: Option[CleanupFn] = None

    if (layoutShift && referenceElement != null) {
      cleanupIo = Some(observeMove(referenceElement, update))
    }

    // Set up animation frame loop for continuous tracking
    var frameId: Int = -1
    var prevRefRect: Option[ClientRectObject] = None

    if (animationFrame) {
      prevRefRect = Some(domGetBoundingClientRect(reference))

      def frameLoop(): Unit = {
        val nextRefRect = domGetBoundingClientRect(reference)

        if (prevRefRect.exists(prev => !rectsAreEqual(prev, nextRefRect))) {
          update()
        }

        prevRefRect = Some(nextRefRect)
        frameId = dom.window.requestAnimationFrame(_ => frameLoop())
      }

      frameLoop()
    }

    // Call update initially
    update()

    // Return cleanup function
    () => {
      // Remove scroll and resize listeners
      ancestors.foreach { ancestor =>
        if (ancestorScroll) {
          ancestor.removeEventListener("scroll", scrollHandler, scrollOptions)
        }
        if (ancestorResize) {
          ancestor.removeEventListener("resize", resizeHandler, useCapture = false)
        }
      }

      // Cleanup IntersectionObserver
      cleanupIo.foreach(cleanup => cleanup())

      // Cleanup ResizeObserver
      resizeObserver.foreach(_.disconnect())
      resizeObserver = None

      // Cancel animation frame
      if (animationFrame && frameId != -1) {
        dom.window.cancelAnimationFrame(frameId)
      }
    }
  }

  /** Observe element movement using IntersectionObserver.
    *
    * This detects layout shifts by monitoring when the element moves relative to the viewport.
    *
    * @param element
    *   The element to observe
    * @param onMove
    *   Callback to call when movement is detected
    * @return
    *   Cleanup function
    */
  private def observeMove(element: dom.Element, onMove: () => Unit): CleanupFn = {
    var io: Option[dom.IntersectionObserver] = None
    var timeoutId: Int = -1

    val root = getDocumentElement(element)

    def cleanup(): Unit = {
      if (timeoutId != -1) {
        dom.window.clearTimeout(timeoutId)
        timeoutId = -1
      }
      io.foreach(_.disconnect())
      io = None
    }

    def refresh(skip: Boolean = false, threshold: Double = 1.0): Unit = {
      cleanup()

      // Get bounding rect once (not twice!)
      val elementRectForRootMargin = domGetBoundingClientRect(element)
      val left = elementRectForRootMargin.left
      val top = elementRectForRootMargin.top
      val width = elementRectForRootMargin.width
      val height = elementRectForRootMargin.height

      if (!skip) {
        onMove()
      }

      // Check for falsy values (0, NaN, etc.) matching TypeScript's !width || !height
      if (width == 0 || height == 0 || width.isNaN || height.isNaN) {
        return
      }

      val insetTop = math.floor(top).toInt
      val insetRight = math.floor(root.clientWidth - (left + width)).toInt
      val insetBottom = math.floor(root.clientHeight - (top + height)).toInt
      val insetLeft = math.floor(left).toInt
      val rootMargin = s"${-insetTop}px ${-insetRight}px ${-insetBottom}px ${-insetLeft}px"

      // Clamp threshold and use || 1 to handle edge cases
      val clampedThreshold = {
        val clamped = math.max(0, math.min(1, threshold))
        if (clamped == 0 && threshold != 0) 1.0 else clamped
      }

      var isFirstUpdate = true

      val handleObserve: js.Function2[js.Array[dom.IntersectionObserverEntry], dom.IntersectionObserver, Unit] =
        (entries: js.Array[dom.IntersectionObserverEntry], _: dom.IntersectionObserver) => {
          val ratio = entries(0).intersectionRatio

          if (ratio != threshold) {
            if (!isFirstUpdate) {
              refresh()
            } else if (ratio == 0) {
              // If the reference is clipped, throttle the refresh
              timeoutId = dom.window.setTimeout(
                () => {
                  refresh(skip = false, threshold = 1e-7)
                },
                1000
              )
            } else {
              refresh(skip = false, threshold = ratio)
            }
          }

          // Check if element actually moved even though ratio is 1
          if (ratio == 1 && !rectsAreEqual(elementRectForRootMargin, domGetBoundingClientRect(element))) {
            refresh()
          }

          isFirstUpdate = false
        }

      val options = js.Dynamic
        .literal(
          "rootMargin" -> rootMargin,
          "threshold" -> clampedThreshold // Single number, not array
        )
        .asInstanceOf[dom.IntersectionObserverInit]

      try {
        // Try with root set to ownerDocument (for iframe support)
        val optionsWithRoot = js.Dynamic
          .literal(
            "rootMargin" -> rootMargin,
            "threshold" -> clampedThreshold, // Single number, not array
            "root" -> root.ownerDocument
          )
          .asInstanceOf[dom.IntersectionObserverInit]
        io = Some(new dom.IntersectionObserver(handleObserve, optionsWithRoot))
      } catch {
        case _: Throwable =>
          // Fallback for older browsers
          io = Some(new dom.IntersectionObserver(handleObserve, options))
      }

      io.foreach(_.observe(element))
    }

    refresh(skip = true)

    cleanup
  }
}
