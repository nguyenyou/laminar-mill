package io.github.nguyenyou.laminar.primitives.utils.floating

import org.scalajs.dom
import scala.scalajs.js
import Utils.*
import Types.ClientRectObject

/** AutoUpdate functionality for floating elements.
  *
  * Automatically updates the position of floating elements when necessary, such as when scrolling, resizing, or layout shifts occur.
  *
  * Ported from @floating-ui/dom/src/autoUpdate.ts
  */
object AutoUpdate {

  /** Options for autoUpdate configuration. */
  case class AutoUpdateOptions(
    ancestorScroll: Boolean = true,
    ancestorResize: Boolean = true,
    elementResize: Boolean = true,
    layoutShift: Boolean = true,
    animationFrame: Boolean = false
  )

  /** Cleanup function type returned by autoUpdate. */
  type CleanupFn = () => Unit

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
  ): CleanupFn = {

    // Collect all ancestor elements that can affect positioning
    val ancestors = if (options.ancestorScroll || options.ancestorResize) {
      getOverflowAncestors(reference) ++ getOverflowAncestors(floating)
    } else {
      Seq.empty
    }

    // Set up scroll and resize listeners on ancestors
    val scrollHandler: js.Function1[dom.Event, Unit] = (_: dom.Event) => update()
    val resizeHandler: js.Function1[dom.Event, Unit] = (_: dom.Event) => update()

    ancestors.foreach { ancestor =>
      if (options.ancestorScroll) {
        ancestor.addEventListener("scroll", scrollHandler, useCapture = false)
      }
      if (options.ancestorResize) {
        ancestor.addEventListener("resize", resizeHandler, useCapture = false)
      }
    }

    // Set up ResizeObserver for element resize detection
    var resizeObserver: Option[dom.ResizeObserver] = None
    var reobserveFrame: Int = -1

    if (options.elementResize && js.typeOf(js.Dynamic.global.ResizeObserver) != "undefined") {
      val resizeCallback: js.Function2[js.Array[dom.ResizeObserverEntry], dom.ResizeObserver, Unit] =
        (entries: js.Array[dom.ResizeObserverEntry], _: dom.ResizeObserver) => {
          val firstEntry = entries(0)
          if (firstEntry != null && firstEntry.target == reference && resizeObserver.isDefined) {
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

      if (!options.animationFrame) {
        observer.observe(reference)
      }
      observer.observe(floating)
      resizeObserver = Some(observer)
    }

    // Set up IntersectionObserver for layout shift detection
    var cleanupIo: Option[CleanupFn] = None

    if (options.layoutShift && js.typeOf(js.Dynamic.global.IntersectionObserver) != "undefined") {
      cleanupIo = Some(observeMove(reference, update))
    }

    // Set up animation frame loop for continuous tracking
    var frameId: Int = -1
    var prevRefRect: Option[ClientRectObject] = None

    if (options.animationFrame) {
      prevRefRect = Some(getBoundingClientRect(reference))

      def frameLoop(): Unit = {
        val nextRefRect = getBoundingClientRect(reference)

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
        if (options.ancestorScroll) {
          ancestor.removeEventListener("scroll", scrollHandler, useCapture = false)
        }
        if (options.ancestorResize) {
          ancestor.removeEventListener("resize", resizeHandler, useCapture = false)
        }
      }

      // Cleanup IntersectionObserver
      cleanupIo.foreach(cleanup => cleanup())

      // Cleanup ResizeObserver
      resizeObserver.foreach(_.disconnect())
      resizeObserver = None

      // Cancel animation frame
      if (options.animationFrame && frameId != -1) {
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

      val domRect = element.getBoundingClientRect()
      val elementRectForRootMargin = getBoundingClientRect(element)
      val left = domRect.left
      val top = domRect.top
      val width = domRect.width
      val height = domRect.height

      if (!skip) {
        onMove()
      }

      if (width == 0 || height == 0) {
        return
      }

      val insetTop = math.floor(top).toInt
      val insetRight = math.floor(root.clientWidth - (left + width)).toInt
      val insetBottom = math.floor(root.clientHeight - (top + height)).toInt
      val insetLeft = math.floor(left).toInt
      val rootMargin = s"${-insetTop}px ${-insetRight}px ${-insetBottom}px ${-insetLeft}px"

      val clampedThreshold = math.max(0, math.min(1, threshold))

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
          if (ratio == 1 && !rectsAreEqual(elementRectForRootMargin, getBoundingClientRect(element))) {
            refresh()
          }

          isFirstUpdate = false
        }

      val options = js.Dynamic
        .literal(
          "rootMargin" -> rootMargin,
          "threshold" -> js.Array(clampedThreshold)
        )
        .asInstanceOf[dom.IntersectionObserverInit]

      try {
        // Try with root set to ownerDocument (for iframe support)
        val optionsWithRoot = js.Dynamic
          .literal(
            "rootMargin" -> rootMargin,
            "threshold" -> js.Array(clampedThreshold),
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
