package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.api.L
import io.github.nguyenyou.laminar.nodes.{ChildNode, DetachedRoot}
import org.scalajs.dom
import io.github.nguyenyou.laminar.primitives.base.*
import io.github.nguyenyou.laminar.nodes.ReactiveHtmlElement
import io.github.nguyenyou.facades.floatingui.FloatingUIDOM
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import scala.scalajs.js.Thenable.Implicits.thenable2future
import scala.util.Failure
import scala.util.Success

class TooltipPortal(
  val root: TooltipRoot
) extends Component {
  private var mounted = false

  private val portalRoot = renderDetached(
    element,
    activateNow = true
  )

  def mount() = {
    if (!mounted) {
      mounted = true
      if (!portalRoot.isActive) portalRoot.activate()
      dom.document.body.appendChild(portalRoot.ref)
    }
  }

  def unmount() = {
    if (mounted) {
      mounted = false
      if (portalRoot.isActive) portalRoot.deactivate()
      dom.document.body.removeChild(portalRoot.ref)
    }
  }

  def onHoverChange(isHovering: Boolean) = {
    if (isHovering) mount() else unmount()
  }

  def render(): HtmlElement = {
    div(
      dataAttr("slot") := "tooltip-portal",
      cls := "absolute w-max",
      top.px(0),
      left.px(0),
      onMountCallback { ctx =>
        println("MOUNTED > PORTAL")
        val portal = ctx.thisNode
        root.trigger.foreach { trigger =>
          println("COMPUTE POSITION")
          FloatingUIDOM
            .computePosition(
              reference = trigger.element.ref,
              floating = ctx.thisNode.ref,
              options = FloatingUIDOM.ComputePositionConfig(
                placement = "top",
                middleware = root.floatinguiMiddlewares
              )
            )
            .onComplete {
              case Failure(exception) => println(exception)
              case Success(result) =>
                println(s"X: ${result.x}, Y: ${result.y}")
                portal.ref.style.left = s"${result.x}px"
                portal.ref.style.top = s"${result.y}px"
                portal.ref.style.display = "block"

                // Position the arrow element if present
                result.middlewareData.arrow.foreach { arrowData =>
                  root.arrow.foreach { arrowElement =>
                    // Calculate the static side based on placement
                    val staticSide = result.placement.split("-")(0) match {
                      case "top"    => "bottom"
                      case "right"  => "left"
                      case "bottom" => "top"
                      case "left"   => "right"
                      case _        => "bottom"
                    }

                    // Apply x position if available
                    arrowData.x.foreach { x =>
                      println(s"ARROW X: ${x}")
                      arrowElement.element.ref.style.left = s"${x}px"
                    }

                    // Apply y position if available
                    arrowData.y.foreach { y =>
                      println(s"ARROW Y: ${y}")
                      arrowElement.element.ref.style.top = s"${y}px"
                    }

                    // Clear other sides and set the static side offset
                    arrowElement.element.ref.style.right = ""
                    arrowElement.element.ref.style.bottom = ""
                    arrowElement.element.ref.style.setProperty(staticSide, "-4px")
                  }
                }
            }
        }
      }
    )
  }
}

object TooltipPortal extends HasClassNameProp[TooltipPortal] {
  object Props extends PropSelector[TooltipPortal] {
    lazy val className: ClassNameProp.type = ClassNameProp
  }

  def apply()(context: TooltipPortal ?=> Unit)(using root: TooltipRoot): TooltipPortal = {
    given tooltipPortal: TooltipPortal = new TooltipPortal(root)
    root.setPortal(tooltipPortal)
    context
    tooltipPortal
  }

}
