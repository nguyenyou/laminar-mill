package www.examples.floatingui

import scala.scalajs.js
import scala.scalajs.js.Thenable.Implicits.thenable2future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom
import io.github.nguyenyou.laminar.api.L._
import www.facades.floatingui.FloatingUIDOM._

/** AutoUpdate Examples - Demonstrates automatic position updates.
  *
  * Based on: https://floating-ui.com/docs/autoUpdate
  *
  * Key concepts demonstrated:
  *   - Using autoUpdate to keep floating elements positioned correctly
  *   - Proper cleanup of autoUpdate listeners
  *   - Handling scroll and resize events automatically
  *   - Integration with Laminar's lifecycle
  */
object AutoUpdateExample {

  /** Example 1: Basic autoUpdate usage
    *
    * The tooltip stays anchored to the button even when scrolling or resizing
    */
  def basicAutoUpdate(): HtmlElement = {
    val isVisible = Var(false)

    div(
      height := "150vh", // Make page scrollable
      p("Scroll the page - the tooltip will stay anchored to the button!"),
      button(
        marginTop := "50px",
        "Toggle tooltip (with autoUpdate)",
        onClick --> Observer[dom.MouseEvent] { _ =>
          isVisible.update(!_)
        }
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
        onMountUnmountCallback(
          mount = { ctx =>
            given Owner = ctx.owner
            val buttonEl = ctx.thisNode.ref.previousElementSibling.asInstanceOf[dom.HTMLElement]
            val tooltipEl = ctx.thisNode.ref
            var cleanupFn: Option[js.Function0[Unit]] = None

            // Update position function
            def updatePosition(): Unit = {
              computePosition(
                buttonEl,
                tooltipEl,
                ComputePositionConfig(
                  placement = "top",
                  middleware = js.Array(offset(8), flip(), shift())
                )
              ).foreach { result =>
                tooltipEl.style.left = s"${result.x}px"
                tooltipEl.style.top = s"${result.y}px"
              }
            }

            // Set up autoUpdate when tooltip becomes visible
            isVisible.signal.foreach { visible =>
              if (visible) {
                // Initial position
                updatePosition()

                // Set up autoUpdate - it will call updatePosition automatically
                // when scrolling, resizing, or other relevant events occur
                cleanupFn = Some(
                  autoUpdate(
                    buttonEl,
                    tooltipEl,
                    () => updatePosition(),
                    AutoUpdateOptions(
                      ancestorScroll = true, // Update on scroll
                      ancestorResize = true, // Update on resize
                      elementResize = true // Update when elements resize
                    )
                  )
                )
              } else {
                // Clean up autoUpdate when tooltip is hidden
                cleanupFn.foreach(cleanup => cleanup())
                cleanupFn = None
              }
            }
          },
          unmount = { _ =>
            // Cleanup happens automatically when owner is killed
          }
        ),
        "I stay anchored even when you scroll!"
      )
    )
  }

  /** Example 2: AutoUpdate with animation frame
    *
    * Uses animationFrame option for smoother updates during animations
    */
  def autoUpdateWithAnimationFrame(): HtmlElement = {
    val isVisible = Var(false)
    val buttonPosition = Var(0.0)

    div(
      p("Click the button to start animation. The tooltip follows smoothly!"),
      button(
        position := "relative",
        left <-- buttonPosition.signal.map(pos => s"${pos}px"),
        transition := "left 2s ease-in-out",
        "Animate me",
        onClick --> Observer[dom.MouseEvent] { _ =>
          isVisible.update(!_)
          // Animate button position
          if (!isVisible.now()) {
            buttonPosition.set(if (buttonPosition.now() == 0) 200 else 0)
          }
        }
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
        onMountUnmountCallback(
          mount = { ctx =>
            given Owner = ctx.owner
            val buttonEl = ctx.thisNode.ref.previousElementSibling.asInstanceOf[dom.HTMLElement]
            val tooltipEl = ctx.thisNode.ref
            var cleanupFn: Option[js.Function0[Unit]] = None

            def updatePosition(): Unit = {
              computePosition(
                buttonEl,
                tooltipEl,
                ComputePositionConfig(
                  placement = "top",
                  middleware = js.Array(offset(8))
                )
              ).foreach { result =>
                tooltipEl.style.left = s"${result.x}px"
                tooltipEl.style.top = s"${result.y}px"
              }
            }

            isVisible.signal.foreach { visible =>
              if (visible) {
                updatePosition()
                cleanupFn = Some(
                  autoUpdate(
                    buttonEl,
                    tooltipEl,
                    () => updatePosition(),
                    AutoUpdateOptions(
                      animationFrame = true // Update on every animation frame for smooth tracking
                    )
                  )
                )
              } else {
                cleanupFn.foreach(cleanup => cleanup())
                cleanupFn = None
              }
            }
          },
          unmount = { _ =>
            // Cleanup happens automatically when owner is killed
          }
        ),
        "I follow the animation!"
      )
    )
  }

  /** Example 3: Dropdown menu with autoUpdate
    *
    * A more realistic example showing a dropdown menu that stays positioned
    */
  def dropdownWithAutoUpdate(): HtmlElement = {
    val isOpen = Var(false)
    val selectedItem = Var("Select an option")

    val menuItems = List(
      "Option 1",
      "Option 2",
      "Option 3",
      "Option 4",
      "Option 5"
    )

    div(
      height := "120vh", // Make scrollable
      p("A dropdown menu that stays positioned when scrolling."),
      // Trigger button
      button(
        marginTop := "100px",
        padding := "8px 16px",
        backgroundColor := "#007bff",
        color := "white",
        border := "none",
        borderRadius := "4px",
        cursor := "pointer",
        onClick --> Observer[dom.MouseEvent] { _ =>
          isOpen.update(!_)
        },
        child.text <-- selectedItem.signal
      ),
      // Dropdown menu
      div(
        display <-- isOpen.signal.map(if (_) "block" else "none"),
        position := "absolute",
        top := "0",
        left := "0",
        width := "200px",
        backgroundColor := "white",
        border := "1px solid #ccc",
        borderRadius := "4px",
        boxShadow := "0 2px 8px rgba(0,0,0,0.15)",
        zIndex := "1000",
        onMountUnmountCallback(
          mount = { ctx =>
            given Owner = ctx.owner
            val buttonEl = ctx.thisNode.ref.previousElementSibling.asInstanceOf[dom.HTMLElement]
            val menuEl = ctx.thisNode.ref
            var cleanupFn: Option[js.Function0[Unit]] = None

            def updatePosition(): Unit = {
              computePosition(
                buttonEl,
                menuEl,
                ComputePositionConfig(
                  placement = "bottom-start",
                  middleware = js.Array(
                    offset(4),
                    flip(),
                    shift(ShiftOptions())
                  )
                )
              ).foreach { result =>
                menuEl.style.left = s"${result.x}px"
                menuEl.style.top = s"${result.y}px"
              }
            }

            isOpen.signal.foreach { open =>
              if (open) {
                updatePosition()
                cleanupFn = Some(
                  autoUpdate(
                    buttonEl,
                    menuEl,
                    () => updatePosition(),
                    AutoUpdateOptions(
                      ancestorScroll = true,
                      ancestorResize = true,
                      elementResize = true
                    )
                  )
                )
              } else {
                cleanupFn.foreach(cleanup => cleanup())
                cleanupFn = None
              }
            }
          },
          unmount = { _ =>
            // Cleanup happens automatically when owner is killed
          }
        ),
        // Menu items
        menuItems.map { item =>
          val isHovered = Var(false)
          div(
            padding := "8px 12px",
            cursor := "pointer",
            backgroundColor <-- isHovered.signal.map(if (_) "#f0f0f0" else "transparent"),
            onMouseEnter --> Observer[dom.MouseEvent] { _ => isHovered.set(true) },
            onMouseLeave --> Observer[dom.MouseEvent] { _ => isHovered.set(false) },
            onClick --> Observer[dom.MouseEvent] { _ =>
              selectedItem.set(item)
              isOpen.set(false)
            },
            item
          )
        }
      )
    )
  }

  /** Complete demo */
  def demo(): HtmlElement = {
    div(
      h1("FloatingUI AutoUpdate Examples"),
      p(
        "AutoUpdate automatically repositions floating elements when scrolling, resizing, or animating.",
        br(),
        "Based on: ",
        a(
          href := "https://floating-ui.com/docs/autoUpdate",
          target := "_blank",
          "Official AutoUpdate Documentation"
        )
      ),
      hr(),
      h2("1. Basic AutoUpdate (Scroll the page!)"),
      basicAutoUpdate(),
      hr(),
      h2("2. AutoUpdate with Animation Frame"),
      autoUpdateWithAnimationFrame(),
      hr(),
      h2("3. Dropdown Menu with AutoUpdate"),
      dropdownWithAutoUpdate()
    )
  }
}
