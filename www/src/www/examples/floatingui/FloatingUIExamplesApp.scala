package www.examples.floatingui

import io.github.nguyenyou.laminar.api.L._
import org.scalajs.dom

/** Main application showcasing all FloatingUI examples.
  *
  * This app demonstrates the complete FloatingUI integration with Scala.js/Laminar, including basic tooltips, middleware usage, and
  * autoUpdate functionality.
  *
  * To run this app, update your www/src/www/App.scala to render this component.
  */
object FloatingUIExamplesApp {

  sealed trait Tab
  object Tab {
    case object BasicTooltips extends Tab
    case object Middleware extends Tab
    case object AutoUpdate extends Tab
    case object QuickReference extends Tab
  }

  def apply(): HtmlElement = {
    val selectedTab = Var[Tab](Tab.BasicTooltips)

    div(
      // Global styles
      styleAttr := """
        body {
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
          margin: 0;
          padding: 20px;
          background-color: #f5f5f5;
        }
        
        h1 {
          color: #333;
          border-bottom: 2px solid #007bff;
          padding-bottom: 10px;
        }
        
        h2 {
          color: #555;
          margin-top: 30px;
        }
        
        p {
          color: #666;
          line-height: 1.6;
        }
        
        hr {
          border: none;
          border-top: 1px solid #ddd;
          margin: 30px 0;
        }
        
        button {
          padding: 8px 16px;
          background-color: #007bff;
          color: white;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          font-size: 14px;
          transition: background-color 0.2s;
        }
        
        button:hover {
          background-color: #0056b3;
        }
        
        button:active {
          background-color: #004085;
        }
        
        a {
          color: #007bff;
          text-decoration: none;
        }
        
        a:hover {
          text-decoration: underline;
        }
        
        code {
          background-color: #f4f4f4;
          padding: 2px 6px;
          border-radius: 3px;
          font-family: 'Courier New', monospace;
          font-size: 0.9em;
        }
        
        pre {
          background-color: #f4f4f4;
          padding: 15px;
          border-radius: 5px;
          overflow-x: auto;
        }
      """,
      // Header
      div(
        backgroundColor := "white",
        padding := "20px",
        borderRadius := "8px",
        boxShadow := "0 2px 4px rgba(0,0,0,0.1)",
        marginBottom := "20px",
        h1(
          margin := "0 0 10px 0",
          "FloatingUI Examples for Scala.js/Laminar"
        ),
        p(
          margin := "0",
          "Comprehensive examples demonstrating FloatingUI integration with Scala.js and Laminar.",
          br(),
          "Based on the ",
          a(
            href := "https://floating-ui.com/docs/getting-started",
            target := "_blank",
            "official FloatingUI documentation"
          ),
          "."
        )
      ),
      // Tab navigation
      div(
        backgroundColor := "white",
        padding := "10px",
        borderRadius := "8px",
        boxShadow := "0 2px 4px rgba(0,0,0,0.1)",
        marginBottom := "20px",
        display := "flex",
        gap := "10px",
        tabButton("Basic Tooltips", Tab.BasicTooltips, selectedTab),
        tabButton("Middleware", Tab.Middleware, selectedTab),
        tabButton("AutoUpdate", Tab.AutoUpdate, selectedTab),
        tabButton("Quick Reference", Tab.QuickReference, selectedTab)
      ),
      // Content area
      div(
        backgroundColor := "white",
        padding := "20px",
        borderRadius := "8px",
        boxShadow := "0 2px 4px rgba(0,0,0,0.1)",
        minHeight := "500px",
        child <-- selectedTab.signal.map {
          case Tab.BasicTooltips  => BasicTooltipExample.demo()
          case Tab.Middleware     => MiddlewareExample.demo()
          case Tab.AutoUpdate     => AutoUpdateExample.demo()
          case Tab.QuickReference => quickReference()
        }
      ),
      // Footer
      div(
        marginTop := "20px",
        padding := "20px",
        textAlign := "center",
        color := "#666",
        fontSize := "14px",
        p(
          "See ",
          a(
            href := "https://github.com/floating-ui/floating-ui",
            target := "_blank",
            "FloatingUI on GitHub"
          ),
          " | ",
          a(
            href := "../../facades/FACADE_IMPROVEMENTS.md",
            "Facade Documentation"
          ),
          " | ",
          a(
            href := "README.md",
            "Examples README"
          )
        )
      )
    )
  }

  private def tabButton(label: String, tab: Tab, selectedTab: Var[Tab]): HtmlElement = {
    button(
      label,
      backgroundColor <-- selectedTab.signal.map { selected =>
        if (selected == tab) "#0056b3" else "#007bff"
      },
      onClick --> Observer[dom.MouseEvent] { _ =>
        selectedTab.set(tab)
      }
    )
  }

  private def quickReference(): HtmlElement = {
    div(
      h1("Quick Reference"),
      p("Common patterns and code snippets for using FloatingUI in Scala.js/Laminar."),
      hr(),
      h2("1. Basic Setup"),
      p("Import the necessary dependencies:"),
      pre(
        code(
          """import scala.scalajs.js
import scala.scalajs.js.Thenable.Implicits.thenable2future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom
import io.github.nguyenyou.laminar.api.L._
import www.facades.floatingui.FloatingUIDOM._"""
        )
      ),
      hr(),
      h2("2. Simple Tooltip"),
      pre(
        code(
          """computePosition(buttonEl, tooltipEl).foreach { result =>
  tooltipEl.style.left = s"$${result.x}px"
  tooltipEl.style.top = s"$${result.y}px"
}"""
        )
      ),
      hr(),
      h2("3. With Configuration"),
      pre(
        code(
          """computePosition(
  buttonEl,
  tooltipEl,
  ComputePositionConfig(
    placement = "top",
    middleware = js.Array(
      offset(10),
      flip(),
      shift()
    )
  )
).foreach { result =>
  tooltipEl.style.left = s"$${result.x}px"
  tooltipEl.style.top = s"$${result.y}px"
}"""
        )
      ),
      hr(),
      h2("4. With AutoUpdate"),
      pre(
        code(
          """var cleanupFn: Option[js.Function0[Unit]] = None

def updatePosition(): Unit = {
  computePosition(buttonEl, tooltipEl, config).foreach { result =>
    tooltipEl.style.left = s"$${result.x}px"
    tooltipEl.style.top = s"$${result.y}px"
  }
}

// Set up
cleanupFn = Some(
  autoUpdate(
    buttonEl,
    tooltipEl,
    () => updatePosition(),
    AutoUpdateOptions(
      ancestorScroll = true,
      ancestorResize = true
    )
  )
)

// Clean up
cleanupFn.foreach(cleanup => cleanup())"""
        )
      ),
      hr(),
      h2("5. Arrow Positioning"),
      pre(
        code(
          """computePosition(
  buttonEl,
  tooltipEl,
  ComputePositionConfig(
    middleware = js.Array(
      arrow(ArrowOptions(element = arrowEl, padding = 5))
    )
  )
).foreach { result =>
  // Position tooltip
  tooltipEl.style.left = s"$${result.x}px"
  tooltipEl.style.top = s"$${result.y}px"
  
  // Position arrow
  result.middlewareData.arrow.foreach { arrowData =>
    arrowData.x.foreach(x => arrowEl.style.left = s"$${x}px")
    arrowData.y.foreach(y => arrowEl.style.top = s"$${y}px")
  }
}"""
        )
      ),
      hr(),
      h2("6. Available Middleware"),
      ul(
        li(code("offset(value)"), " - Add spacing between elements"),
        li(code("flip()"), " - Flip placement when no space"),
        li(code("shift()"), " - Shift element to stay in view"),
        li(code("arrow(options)"), " - Position an arrow element"),
        li(code("hide(options)"), " - Hide when reference is hidden"),
        li(code("size(options)"), " - Resize floating element"),
        li(code("inline(options)"), " - For inline reference elements"),
        li(code("autoPlacement(options)"), " - Auto-choose best placement")
      ),
      hr(),
      h2("7. Placement Options"),
      p("Available placements:"),
      ul(
        li(code("top"), ", ", code("top-start"), ", ", code("top-end")),
        li(code("right"), ", ", code("right-start"), ", ", code("right-end")),
        li(code("bottom"), ", ", code("bottom-start"), ", ", code("bottom-end")),
        li(code("left"), ", ", code("left-start"), ", ", code("left-end"))
      ),
      hr(),
      h2("8. Laminar Integration Pattern"),
      pre(
        code(
          """val isVisible = Var(false)

div(
  button(
    onClick --> Observer(_ => isVisible.update(!_)),
    "Toggle"
  ),
  div(
    display <-- isVisible.signal.map(if (_) "block" else "none"),
    onMountCallback { ctx =>
      val buttonEl = /* get button element */
      val tooltipEl = ctx.thisNode.ref
      
      isVisible.signal.foreach { visible =>
        if (visible) {
          computePosition(buttonEl, tooltipEl).foreach { result =>
            // Apply positioning
          }
        }
      }(ctx.owner)
    },
    "Tooltip content"
  )
)"""
        )
      )
    )
  }
}
