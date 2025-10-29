package www.floating

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.floatingUI.FloatingUI.computePosition
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.middleware.FlipMiddleware
import io.github.nguyenyou.floatingUI.middleware.ShiftMiddleware
import io.github.nguyenyou.floatingUI.DOMPlatform
import io.github.nguyenyou.airstream.core.{EventStream, Signal}
import io.github.nguyenyou.airstream.state.Var
import io.github.nguyenyou.airstream.eventbus.EventBus
import org.scalajs.dom
import scala.scalajs.js

/** Scroll utility configuration */
case class ScrollConfig(
  referenceRef: Var[Option[dom.Element]],
  floatingRef: Var[Option[dom.HTMLElement]],
  update: () => Unit,
  rtl: Boolean = false
)

/** Result of useScroll utility */
case class ScrollResult(
  scrollRef: Var[Option[dom.HTMLElement]],
  indicator: HtmlElement,
  scrollPosition: Signal[(Option[Double], Option[Double])]
)

/** Laminar/Airstream implementation of floating-ui's useScroll utility.
  *
  * This utility provides:
  *   1. Automatic centering of scrollable content on mount 2. Tracking and displaying current scroll position (x, y coordinates) 3. RTL
  *      (right-to-left) layout support 4. Automatic scroll event listener management with cleanup
  *
  * Ported from: floating-ui/packages/dom/test/visual/utils/useScroll.tsx
  *
  * @param config
  *   Configuration containing refs and update callback
  * @return
  *   ScrollResult with scrollRef, indicator element, and scroll position signal
  */
def useScroll(config: ScrollConfig): ScrollResult = {
  import config.*

  // State for scroll position tracking
  val scrollX = Var[Option[Double]](None)
  val scrollY = Var[Option[Double]](None)
  val scrollPosition: Signal[(Option[Double], Option[Double])] =
    Signal.combine(scrollX.signal, scrollY.signal)

  // Ref for the scroll container
  val scrollRef = Var[Option[dom.HTMLElement]](None)

  // Event bus for triggering updates
  val updateBus = new EventBus[Unit]

  /** Get all overflow ancestors for an element */
  def getOverflowAncestors(element: dom.Element): Seq[dom.Element] = {
    val ancestors = scala.collection.mutable.ArrayBuffer[dom.Element]()
    var current = element.parentNode

    while (current != null && current != dom.document.documentElement) {
      current match {
        case elem: dom.Element =>
          val style = dom.window.getComputedStyle(elem)
          val overflow = style.overflow
          val overflowX = style.overflowX
          val overflowY = style.overflowY

          if (
            overflow == "auto" || overflow == "scroll" ||
            overflowX == "auto" || overflowX == "scroll" ||
            overflowY == "auto" || overflowY == "scroll"
          ) {
            ancestors += elem
          }
          current = elem.parentNode
        case _ =>
          current = null
      }
    }

    ancestors.toSeq
  }

  /** Center the scroll container */
  def centerScroll(scroll: dom.HTMLElement): Unit = {
    val y = scroll.scrollHeight / 2.0 - scroll.offsetHeight / 2.0
    val x = scroll.scrollWidth / 2.0 - scroll.offsetWidth / 2.0
    scroll.scrollTop = y
    scroll.scrollLeft = if (rtl) -x else x
  }

  /** Update scroll position state */
  def updateScrollPosition(scroll: dom.HTMLElement): Unit = {
    scrollX.set(Some(scroll.scrollLeft))
    scrollY.set(Some(scroll.scrollTop))
  }

  /** Handler for scroll events */
  val handleScroll: js.Function1[dom.Event, Unit] = { (_: dom.Event) =>
    scrollRef.now().foreach { scroll =>
      updateScrollPosition(scroll)
    }
    update()
  }

  // Create the scroll indicator element
  val indicator = div(
    className := "scroll-indicator",
    position := "fixed",
    backgroundColor := "#edeff726",
    zIndex := "10",
    width := "fit-content",
    padding := "5px",
    borderRadius := "5px",
    child.text <-- scrollPosition.map { case (x, y) =>
      s"x: ${x.map(_.toInt).getOrElse("null")}, y: ${y.map(_.toInt).getOrElse("null")}"
    }
  )

  // Setup scroll container lifecycle
  val scrollContainerBinder = onMountCallback { ctx =>
    import ctx.owner

    // Wait for all refs to be available using Signal.combine
    val refsReady = Signal.combine(scrollRef.signal, referenceRef.signal, floatingRef.signal)

    refsReady.foreach { case (scrollOpt, referenceOpt, floatingOpt) =>
      (scrollOpt, referenceOpt) match {
        case (Some(scroll), Some(reference)) =>
          // Get all overflow ancestors
          val referenceAncestors = if (reference.isInstanceOf[dom.Element]) {
            getOverflowAncestors(reference.asInstanceOf[dom.Element])
          } else Seq.empty

          val floatingAncestors = floatingOpt.map(getOverflowAncestors).getOrElse(Seq.empty)
          val allAncestors = (referenceAncestors ++ floatingAncestors).distinct

          // Attach scroll listeners to all ancestors
          allAncestors.foreach { ancestor =>
            ancestor.addEventListener("scroll", handleScroll)
          }

          // Center the scroll container
          centerScroll(scroll)
          updateScrollPosition(scroll)

          // Initial update
          update()

        case _ => // Refs not ready yet
      }
    }
  }

  // Cleanup scroll listeners on unmount
  val scrollContainerCleanup = onUnmountCallback { _ =>
    scrollRef.now().foreach { scroll =>
      referenceRef.now().foreach { reference =>
        val referenceAncestors = if (reference.isInstanceOf[dom.Element]) {
          getOverflowAncestors(reference.asInstanceOf[dom.Element])
        } else Seq.empty

        val floatingAncestors = floatingRef.now().map(getOverflowAncestors).getOrElse(Seq.empty)
        val allAncestors = (referenceAncestors ++ floatingAncestors).distinct

        // Remove scroll listeners
        allAncestors.foreach { ancestor =>
          ancestor.removeEventListener("scroll", handleScroll)
        }
      }
    }
  }

  ScrollResult(scrollRef, indicator, scrollPosition)
}

def Flip() = {
  val referenceRef = Var[Option[dom.HTMLElement]](None)
  val floatingRef = Var[Option[dom.HTMLElement]](None)
  val scrollRef = Var[Option[dom.HTMLElement]](None)

  // Update function for repositioning
  // def updatePosition(): Unit = {
  //   (referenceRef.now(), floatingRef.now()) match {
  //     case (Some(reference), Some(floating)) =>
  //       val result = computePosition(
  //         reference,
  //         floating,
  //         placement = "bottom",
  //         strategy = "absolute",
  //         middleware = Seq(
  //           FlipMiddleware.flip(),
  //           ShiftMiddleware.shift()
  //         )
  //       )
  //       floating.style.left = s"${result.x}px"
  //       floating.style.top = s"${result.y}px"
  //     case _ => // Refs not ready
  //   }
  // }

  // Use the scroll utility
  // val scrollResult = useScroll(
  //   ScrollConfig(
  //     referenceRef = referenceRef,
  //     floatingRef = floatingRef,
  //     update = updatePosition,
  //     rtl = false
  //   )
  // )

  // State for scroll position tracking (inline useScroll logic)
  val scrollX = Var[Option[Double]](None)
  val scrollY = Var[Option[Double]](None)
  val scrollPosition: Signal[(Option[Double], Option[Double])] = scrollX.signal.combineWith(scrollY.signal)

  // RTL configuration
  val rtl = false

  /** Get all overflow ancestors for an element */
  def getOverflowAncestors(element: dom.Element): Seq[dom.Element] = {
    val ancestors = scala.collection.mutable.ArrayBuffer[dom.Element]()
    var current = element.parentNode

    while (current != null && current != dom.document.documentElement) {
      current match {
        case elem: dom.Element =>
          val style = dom.window.getComputedStyle(elem)
          val overflow = style.overflow
          val overflowX = style.overflowX
          val overflowY = style.overflowY

          if (
            overflow == "auto" || overflow == "scroll" ||
            overflowX == "auto" || overflowX == "scroll" ||
            overflowY == "auto" || overflowY == "scroll"
          ) {
            ancestors += elem
          }
          current = elem.parentNode
        case _ =>
          current = null
      }
    }

    ancestors.toSeq
  }

  /** Update scroll position state */
  def updateScrollPosition(scroll: dom.HTMLElement): Unit = {
    scrollX.set(Some(scroll.scrollLeft))
    scrollY.set(Some(scroll.scrollTop))
  }

  /** Handler for scroll events */
  val handleScroll: js.Function1[dom.Event, Unit] = { (_: dom.Event) =>
    scrollRef.now().foreach { scroll =>
      updateScrollPosition(scroll)
    }
    // Call update() if needed for floating UI positioning
    // update()
  }

  div(
    h1("Flip"),
    referenceRef.signal
      .combineWith(
        floatingRef.signal,
        scrollRef.signal
      )
      .map { case (referenceOpt, floatingOpt, scrollOpt) =>
        (referenceOpt, floatingOpt, scrollOpt) match {
          case (Some(reference), Some(floating), Some(scroll)) =>
            println("Refs ready!")
            println(s"Reference: $reference")
            println(s"Floating: $floating")
            println(s"Scroll: $scroll")
          case _ => "Refs not ready yet"
        }
      } --> Observer.empty,
    p(),
    div(
      className := "container",
      div(
        onMountCallback { ctx => scrollRef.set(Some(ctx.thisNode.ref)) },
        className := "scroll",
        dataAttr("x") := "",
        position := "relative",

        // Inline useScroll logic: setup scroll listeners and centering on mount
        onMountCallback { ctx =>
          given Owner = ctx.owner

          // Wait for all refs to be available using Signal.combine
          val refsReady = Signal.combine(scrollRef.signal, referenceRef.signal, floatingRef.signal)

          refsReady.foreach { case (scrollOpt, referenceOpt, floatingOpt) =>
            (scrollOpt, referenceOpt) match {
              case (Some(scroll), Some(reference)) =>
                // Get all overflow ancestors from reference and floating elements
                val referenceAncestors = getOverflowAncestors(reference)
                val floatingAncestors = floatingOpt.map(getOverflowAncestors).getOrElse(Seq.empty)
                val allAncestors = (referenceAncestors ++ floatingAncestors).distinct

                // Attach scroll listeners to all ancestors
                allAncestors.foreach { ancestor =>
                  ancestor.addEventListener("scroll", handleScroll)
                }

                // Center the scroll container (from useScroll.tsx lines 78-82)
                val y = scroll.scrollHeight / 2.0 - scroll.offsetHeight / 2.0
                val x = scroll.scrollWidth / 2.0 - scroll.offsetWidth / 2.0
                scroll.scrollTop = y
                scroll.scrollLeft = if (rtl) -x else x

                // Update scroll position state
                updateScrollPosition(scroll)

              case _ => // Refs not ready yet
            }
          }
        },

        // Cleanup scroll listeners on unmount
        onUnmountCallback { _ =>
          scrollRef.now().foreach { scroll =>
            referenceRef.now().foreach { reference =>
              val referenceAncestors = getOverflowAncestors(reference)
              val floatingAncestors = floatingRef.now().map(getOverflowAncestors).getOrElse(Seq.empty)
              val allAncestors = (referenceAncestors ++ floatingAncestors).distinct

              // Remove scroll listeners
              allAncestors.foreach { ancestor =>
                ancestor.removeEventListener("scroll", handleScroll)
              }
            }
          }
        },

        // Scroll indicator (from useScroll.tsx lines 97-109)
        div(
          className := "scroll-indicator",
          position := "fixed",
          backgroundColor := "#edeff726",
          zIndex := "10",
          width := "fit-content",
          padding := "5px",
          borderRadius := "5px",
          child.text <-- scrollPosition.map { case (x, y) =>
            s"x: ${x.map(_.toInt).getOrElse("null")}, y: ${y.map(_.toInt).getOrElse("null")}"
          }
        ),

        // Reference element
        div(
          onMountCallback { ctx => referenceRef.set(Some(ctx.thisNode.ref)) },
          className := "reference",
          "Reference"
        ),

        // Floating element
        div(
          onMountCallback { ctx => floatingRef.set(Some(ctx.thisNode.ref)) },
          className := "floating",
          "Floating"
        )
      )
    )
  )
}
