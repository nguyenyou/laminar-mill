package www.floating

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.floatingUI.FloatingUI
import io.github.nguyenyou.floatingUI.FloatingUI.{computePosition, getOverflowAncestors}
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.middleware.FlipMiddleware
import io.github.nguyenyou.floatingUI.middleware.ShiftMiddleware
import io.github.nguyenyou.floatingUI.DOMPlatform
import io.github.nguyenyou.airstream.core.{EventStream, Signal}
import io.github.nguyenyou.airstream.state.Var
import io.github.nguyenyou.airstream.eventbus.EventBus
import org.scalajs.dom
import scala.scalajs.js

def Flip() = {
  val strategy: Strategy = Strategy.Absolute
  val referenceRef = Var[Option[dom.HTMLElement]](None)
  val floatingRef = Var[Option[dom.HTMLElement]](None)
  val scrollRef = Var[Option[dom.HTMLElement]](None)
  val cleanupRef = Var[Option[js.Function0[Unit]]](None)

  val scrollPosition = Var[(Option[Double], Option[Double])](None, None)

  // RTL configuration
  val rtl = false

  /** Update scroll position state */
  def updateScrollPosition(scroll: dom.HTMLElement): Unit = {
    scrollPosition.set((Some(scroll.scrollLeft), Some(scroll.scrollTop)))
  }

  /** Handler for scroll events */
  val handleScroll: js.Function1[dom.Event, Unit] = { (_: dom.Event) =>
    scrollRef.now().foreach { scroll =>
      updateScrollPosition(scroll)
    }
  }

  def updatePosition() = {
    println("update position")
    for {
      reference <- referenceRef.now()
      floating <- floatingRef.now()
    } {
      val pos = FloatingUI.computePosition(
        reference = reference,
        floating = floating,
        middleware = Seq(FlipMiddleware.flip())
      )
      println(s"X: ${pos.x}, Y: ${pos.y}")

      floating.style.top = s"${pos.y}px"
      floating.style.left = s"${pos.x}px"
      floating.style.position = pos.strategy.toValue
    }

  }

  div(
    onUnmountCallback { _ =>
      cleanupRef.now().foreach(_())
    },
    h1("Flip"),
    referenceRef.signal
      .combineWith(
        floatingRef.signal,
        scrollRef.signal
      )
      .map { case (referenceOpt, floatingOpt, scrollOpt) =>
        (referenceOpt, floatingOpt, scrollOpt) match {
          case (Some(reference), Some(floating), Some(scroll)) => {
            // Inline useScroll logic: setup scroll listeners and centering

            // Get all overflow ancestors from reference and floating elements
            val referenceAncestors = getOverflowAncestors(reference).collect { case elem: dom.Element => elem }
            val floatingAncestors = getOverflowAncestors(floating).collect { case elem: dom.Element => elem }
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

            // register auto update
            val cleanup = FloatingUI.autoUpdate(
              reference = reference,
              floating = floating,
              update = updatePosition
            )
            cleanupRef.set(Some(cleanup))
          }

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
        position.relative,

        // Cleanup scroll listeners on unmount
        onUnmountCallback { _ =>
          scrollRef.now().foreach { scroll =>
            referenceRef.now().foreach { reference =>
              val referenceAncestors = getOverflowAncestors(reference).collect { case elem: dom.Element => elem }
              val floatingAncestors = floatingRef
                .now()
                .map(f => getOverflowAncestors(f).collect { case elem: dom.Element => elem })
                .getOrElse(Seq.empty)
              val allAncestors = (referenceAncestors ++ floatingAncestors).distinct

              // Remove scroll listeners
              allAncestors.foreach { ancestor =>
                ancestor.removeEventListener("scroll", handleScroll)
              }
            }
          }
        },
        div(
          className := "scroll-indicator",
          position.fixed,
          child.text <-- scrollPosition.signal.map { case (x, y) =>
            s"x: ${x.map(_.toInt).getOrElse("null")}, y: ${y.map(_.toInt).getOrElse("null")}"
          }
        ),
        div(
          onMountCallback { ctx => referenceRef.set(Some(ctx.thisNode.ref)) },
          className := "reference",
          "Reference"
        ),
        div(
          onMountCallback { ctx => floatingRef.set(Some(ctx.thisNode.ref)) },
          className := "floating",
          "Floating"
        )
      )
    )
  )
}
