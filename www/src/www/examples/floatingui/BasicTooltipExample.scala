package www.examples.floatingui

import scala.scalajs.js
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import org.scalajs.dom
import io.github.nguyenyou.laminar.api.L._
import www.facades.floatingui.FloatingUIDOM._

/** Basic Tooltip Example - Demonstrates fundamental FloatingUI usage.
  *
  * This example is based on the official Floating UI tutorial: https://floating-ui.com/docs/tutorial
  *
  * Key concepts demonstrated:
  *   - Basic computePosition usage
  *   - Handling Promises in Scala.js (using thenable2future conversion)
  *   - Applying positioning styles to floating elements
  *   - Different placement options
  */
object BasicTooltipExample {

  /** Example 1: Simplest possible tooltip - bottom placement (default) */
  def simpleTooltip(): HtmlElement = {
    val tooltipText = Var("My tooltip")
    val isVisible = Var(false)

    val btn = button(
      idAttr := "button",
      "Hover me",
      onMouseEnter --> Observer[dom.MouseEvent] { _ =>
        isVisible.set(true)
      },
      onMouseLeave --> Observer[dom.MouseEvent] { _ =>
        isVisible.set(false)
      }
    )

    div(
      // Reference element (button)
      btn,
      // Floating element (tooltip)
      div(
        idAttr := "tooltip",
        role := "tooltip",
        display <-- isVisible.signal.map(if (_) "block" else "none"),
        position := "absolute",
        top := "0",
        left := "0",
        width := "max-content",
        backgroundColor := "#222",
        color := "white",
        fontWeight := "bold",
        padding := "5px",
        borderRadius := "4px",
        fontSize := "90%",
        zIndex := "1000",
        // Update position when tooltip becomes visible
        onMountCallback { ctx =>
          given Owner = ctx.owner
          val buttonEl = btn.ref
          val tooltipEl = ctx.thisNode.ref

          // Position the tooltip whenever it becomes visible
          isVisible.signal.foreach { visible =>
            if (visible) {
              // Convert Promise to Future using .toFuture
              computePosition(buttonEl, tooltipEl).toFuture.foreach { result =>
                tooltipEl.style.left = s"${result.x}px"
                tooltipEl.style.top = s"${result.y}px"
              }
            }
          }
        },
        child.text <-- tooltipText.signal
      )
    )
  }

  /** Example 2: Tooltip with custom placement */
  def tooltipWithPlacement(placement: String = "top"): HtmlElement = {
    val isVisible = Var(false)
    val btn = button(
      s"Tooltip on $placement",
      onMouseEnter --> Observer[dom.MouseEvent] { _ => isVisible.set(true) },
      onMouseLeave --> Observer[dom.MouseEvent] { _ => isVisible.set(false) }
    )

    div(
      btn,
      div(
        role := "tooltip",
        display <-- isVisible.signal.map(if (_) "block" else "none"),
        position := "absolute",
        top := "0",
        left := "0",
        width := "max-content",
        backgroundColor := "#222",
        color := "white",
        padding := "5px",
        borderRadius := "4px",
        fontSize := "90%",
        zIndex := "1000",
        onMountCallback { ctx =>
          given Owner = ctx.owner
          val buttonEl = btn.ref
          val tooltipEl = ctx.thisNode.ref

          isVisible.signal.foreach { visible =>
            if (visible) {
              // Use ComputePositionConfig with custom placement
              computePosition(
                buttonEl,
                tooltipEl,
                ComputePositionConfig(placement = placement)
              ).toFuture.foreach { result =>
                tooltipEl.style.left = s"${result.x}px"
                tooltipEl.style.top = s"${result.y}px"
              }
            }
          }
        },
        s"Tooltip content - $placement"
      )
    )
  }

  /** Example 3: All placement variations */
  def allPlacements(): HtmlElement = {
    val placements = List(
      "top",
      "top-start",
      "top-end",
      "right",
      "right-start",
      "right-end",
      "bottom",
      "bottom-start",
      "bottom-end",
      "left",
      "left-start",
      "left-end"
    )

    div(
      h2("All Placement Options"),
      p("Hover over each button to see the tooltip placement"),
      div(
        display := "grid",
        styleAttr := "grid-template-columns: repeat(3, 1fr)",
        gap := "10px",
        placements.map(placement => tooltipWithPlacement(placement))
      )
    )
  }

  /** Example 4: Tooltip with offset
    *
    * Demonstrates using middleware to add spacing between reference and floating element
    */
  def tooltipWithOffset(): HtmlElement = {
    val isVisible = Var(false)
    val btn = button(
      "Tooltip with 10px offset",
      onMouseEnter --> Observer[dom.MouseEvent] { _ => isVisible.set(true) },
      onMouseLeave --> Observer[dom.MouseEvent] { _ => isVisible.set(false) }
    )

    div(
      btn,
      div(
        role := "tooltip",
        display <-- isVisible.signal.map(if (_) "block" else "none"),
        position := "absolute",
        top := "0",
        left := "0",
        width := "max-content",
        backgroundColor := "#222",
        color := "white",
        padding := "5px",
        borderRadius := "4px",
        fontSize := "90%",
        zIndex := "1000",
        onMountCallback { ctx =>
          given Owner = ctx.owner
          val buttonEl = btn.ref
          val tooltipEl = ctx.thisNode.ref

          isVisible.signal.foreach { visible =>
            if (visible) {
              // Use offset middleware to add spacing
              computePosition(
                buttonEl,
                tooltipEl,
                ComputePositionConfig(
                  placement = "top",
                  middleware = js.Array(
                    offset(10) // 10px offset from the button
                  )
                )
              ).toFuture.foreach { result =>
                tooltipEl.style.left = s"${result.x}px"
                tooltipEl.style.top = s"${result.y}px"
              }
            }
          }
        },
        "I'm 10px away from the button!"
      )
    )
  }

  /** Complete demo with all basic examples */
  def demo(): HtmlElement = {
    div(
      h1("FloatingUI Basic Tooltip Examples"),
      p(
        "These examples demonstrate the fundamental usage of FloatingUI in Scala.js/Laminar.",
        br(),
        "Based on: ",
        a(
          href := "https://floating-ui.com/docs/tutorial",
          target := "_blank",
          "Official FloatingUI Tutorial"
        )
      ),
      hr(),
      h2("Example 1: Simple Tooltip (Default Bottom Placement)"),
      simpleTooltip(),
      hr(),
      h2("Example 2: Tooltip with Top Placement"),
      tooltipWithPlacement("top"),
      hr(),
      h2("Example 3: Tooltip with Offset"),
      tooltipWithOffset(),
      hr(),
      allPlacements()
    )
  }
}
