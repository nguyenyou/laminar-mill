package www.examples.floatingui

import scala.scalajs.js
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import org.scalajs.dom
import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.facades.floatingui.FloatingUIDOM.*

/** Middleware Examples - Demonstrates FloatingUI middleware usage.
  *
  * Based on: https://floating-ui.com/docs/middleware
  *
  * Key concepts demonstrated:
  *   - flip() - automatically flips placement when there's no space
  *   - shift() - shifts the floating element to keep it in view
  *   - offset() - adds spacing between elements
  *   - arrow() - positions an arrow element
  *   - Combining multiple middleware
  */
object MiddlewareExample {

  /** Example 1: Flip middleware - prevents tooltip from going off-screen
    *
    * Based on: https://floating-ui.com/docs/flip
    */
  def flipExample(): HtmlElement = {
    val isVisible = Var(false)

    div(
      marginTop := "200px", // Push button down so top placement would overflow
      p("This button is near the top of the viewport. The tooltip will flip to bottom automatically."),
      button(
        "Hover me (flip demo)",
        onMouseEnter --> Observer[dom.MouseEvent] { _ => isVisible.set(true) },
        onMouseLeave --> Observer[dom.MouseEvent] { _ => isVisible.set(false) }
      ),
      div(
        role := "tooltip",
        display <-- isVisible.signal.map(if (_) "block" else "none"),
        position := "absolute",
        top := "0",
        left := "0",
        width := "max-content",
        backgroundColor := "#222",
        color := "white",
        padding := "8px 12px",
        borderRadius := "4px",
        fontSize := "14px",
        zIndex := "1000",
        onMountCallback { ctx =>
          given Owner = ctx.owner
          val buttonEl = ctx.thisNode.ref.previousElementSibling.asInstanceOf[dom.HTMLElement]
          val tooltipEl = ctx.thisNode.ref

          isVisible.signal.foreach { visible =>
            if (visible) {
              computePosition(
                buttonEl,
                tooltipEl,
                ComputePositionConfig(
                  placement = "top", // Prefer top, but will flip to bottom if needed
                  middleware = js.Array(
                    offset(8),
                    flip() // Automatically flips when there's no space
                  )
                )
              ).toFuture.foreach { result =>
                tooltipEl.style.left = s"${result.x}px"
                tooltipEl.style.top = s"${result.y}px"
                // You can check which placement was actually used:
                dom.console.log(s"Actual placement: ${result.placement}")
              }
            }
          }
        },
        "I flip to stay visible!"
      )
    )
  }

  /** Example 2: Shift middleware - keeps tooltip in view by shifting it
    *
    * Based on: https://floating-ui.com/docs/shift
    */
  def shiftExample(): HtmlElement = {
    val isVisible = Var(false)

    div(
      p("This tooltip will shift horizontally to stay within the viewport."),
      button(
        position := "absolute",
        right := "10px", // Position button near right edge
        "Hover me (shift demo)",
        onMouseEnter --> Observer[dom.MouseEvent] { _ => isVisible.set(true) },
        onMouseLeave --> Observer[dom.MouseEvent] { _ => isVisible.set(false) }
      ),
      div(
        role := "tooltip",
        display <-- isVisible.signal.map(if (_) "block" else "none"),
        position := "absolute",
        top := "0",
        left := "0",
        width := "200px", // Wide tooltip to demonstrate shifting
        backgroundColor := "#222",
        color := "white",
        padding := "8px 12px",
        borderRadius := "4px",
        fontSize := "14px",
        zIndex := "1000",
        onMountCallback { ctx =>
          given Owner = ctx.owner
          val buttonEl = ctx.thisNode.ref.previousElementSibling.asInstanceOf[dom.HTMLElement]
          val tooltipEl = ctx.thisNode.ref

          isVisible.signal.foreach { visible =>
            if (visible) {
              computePosition(
                buttonEl,
                tooltipEl,
                ComputePositionConfig(
                  placement = "bottom",
                  middleware = js.Array(
                    offset(8),
                    shift(
                      ShiftOptions(
                        // mainAxis = true, // Shift along the main axis (default: true)
                        // crossAxis = true  // Shift along the cross axis (default: false)
                      )
                    )
                  )
                )
              ).toFuture.foreach { result =>
                tooltipEl.style.left = s"${result.x}px"
                tooltipEl.style.top = s"${result.y}px"
              }
            }
          }
        },
        "This is a wide tooltip that shifts to stay in view!"
      )
    )
  }

  /** Example 3: Arrow middleware - positions an arrow pointing to the reference
    *
    * Based on: https://floating-ui.com/docs/arrow
    */
  def arrowExample(): HtmlElement = {
    val isVisible = Var(false)

    div(
      p("Tooltip with an arrow pointing to the button."),
      button(
        "Hover me (arrow demo)",
        onMouseEnter --> Observer[dom.MouseEvent] { _ => isVisible.set(true) },
        onMouseLeave --> Observer[dom.MouseEvent] { _ => isVisible.set(false) }
      ),
      div(
        role := "tooltip",
        display <-- isVisible.signal.map(if (_) "block" else "none"),
        position := "absolute",
        top := "0",
        left := "0",
        width := "max-content",
        backgroundColor := "#222",
        color := "white",
        padding := "8px 12px",
        borderRadius := "4px",
        fontSize := "14px",
        zIndex := "1000",
        onMountCallback { ctx =>
          given Owner = ctx.owner
          val buttonEl = ctx.thisNode.ref.previousElementSibling.asInstanceOf[dom.HTMLElement]
          val tooltipEl = ctx.thisNode.ref
          val arrowEl = tooltipEl.querySelector("#arrow").asInstanceOf[dom.HTMLElement]

          isVisible.signal.foreach { visible =>
            if (visible) {
              computePosition(
                buttonEl,
                tooltipEl,
                ComputePositionConfig(
                  placement = "top",
                  middleware = js.Array(
                    offset(8),
                    flip(),
                    shift(),
                    arrow(
                      ArrowOptions(
                        element = arrowEl,
                        padding = 5 // Keep arrow 5px away from tooltip edges
                      )
                    )
                  )
                )
              ).toFuture.foreach { result =>
                tooltipEl.style.left = s"${result.x}px"
                tooltipEl.style.top = s"${result.y}px"

                // Position the arrow
                result.middlewareData.arrow.foreach { arrowData =>
                  val staticSide = result.placement.split("-")(0) match {
                    case "top"    => "bottom"
                    case "right"  => "left"
                    case "bottom" => "top"
                    case "left"   => "right"
                    case _        => "bottom"
                  }

                  arrowData.x.foreach { x =>
                    arrowEl.style.left = s"${x}px"
                  }
                  arrowData.y.foreach { y =>
                    arrowEl.style.top = s"${y}px"
                  }

                  // Position arrow on the correct side
                  arrowEl.style.setProperty(staticSide, "-4px")
                }
              }
            }
          }
        },
        "Tooltip with arrow!",
        // Arrow element
        div(
          idAttr := "arrow",
          position := "absolute",
          width := "8px",
          height := "8px",
          backgroundColor := "#222",
          transform := "rotate(45deg)"
        )
      )
    )
  }

  /** Example 4: Combined middleware - realistic tooltip with all features */
  def combinedExample(): HtmlElement = {
    val isVisible = Var(false)

    div(
      p("A production-ready tooltip with offset, flip, shift, and arrow."),
      button(
        "Hover me (full example)",
        onMouseEnter --> Observer[dom.MouseEvent] { _ => isVisible.set(true) },
        onMouseLeave --> Observer[dom.MouseEvent] { _ => isVisible.set(false) }
      ),
      div(
        role := "tooltip",
        display <-- isVisible.signal.map(if (_) "block" else "none"),
        position := "absolute",
        top := "0",
        left := "0",
        width := "max-content",
        maxWidth := "200px",
        backgroundColor := "#1a1a1a",
        color := "white",
        padding := "8px 12px",
        borderRadius := "6px",
        fontSize := "14px",
        boxShadow := "0 4px 6px rgba(0, 0, 0, 0.1)",
        zIndex := "1000",
        onMountCallback { ctx =>
          given Owner = ctx.owner
          val buttonEl = ctx.thisNode.ref.previousElementSibling.asInstanceOf[dom.HTMLElement]
          val tooltipEl = ctx.thisNode.ref
          val arrowEl = tooltipEl.querySelector("#arrow").asInstanceOf[dom.HTMLElement]

          isVisible.signal.foreach { visible =>
            if (visible) {
              computePosition(
                buttonEl,
                tooltipEl,
                ComputePositionConfig(
                  placement = "top",
                  middleware = js.Array(
                    offset(10),
                    flip(
                      FlipOptions(
                        fallbackPlacements = js.Array("bottom", "left", "right")
                      )
                    ),
                    shift(ShiftOptions()),
                    arrow(ArrowOptions(element = arrowEl, padding = 5))
                  )
                )
              ).toFuture.foreach { result =>
                tooltipEl.style.left = s"${result.x}px"
                tooltipEl.style.top = s"${result.y}px"

                // Position arrow
                result.middlewareData.arrow.foreach { arrowData =>
                  val staticSide = result.placement.split("-")(0) match {
                    case "top"    => "bottom"
                    case "right"  => "left"
                    case "bottom" => "top"
                    case "left"   => "right"
                    case _        => "bottom"
                  }

                  arrowData.x.foreach(x => arrowEl.style.left = s"${x}px")
                  arrowData.y.foreach(y => arrowEl.style.top = s"${y}px")
                  arrowEl.style.setProperty(staticSide, "-4px")
                }
              }
            }
          }
        },
        "This is a fully-featured tooltip with all middleware working together!",
        div(
          idAttr := "arrow",
          position := "absolute",
          width := "8px",
          height := "8px",
          backgroundColor := "#1a1a1a",
          transform := "rotate(45deg)"
        )
      )
    )
  }

  /** Complete demo */
  def demo(): HtmlElement = {
    div(
      h1("FloatingUI Middleware Examples"),
      p("Middleware allow you to customize positioning behavior."),
      hr(),
      h2("1. Flip Middleware"),
      flipExample(),
      hr(),
      h2("2. Shift Middleware"),
      shiftExample(),
      hr(),
      h2("3. Arrow Middleware"),
      arrowExample(),
      hr(),
      h2("4. Combined Middleware"),
      combinedExample()
    )
  }
}
