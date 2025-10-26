package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.api.L
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import org.scalajs.dom
import io.github.nguyenyou.facades.floatingui.FloatingUIDOM.*
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import scala.scalajs.js.Thenable.Implicits.thenable2future
import scala.util.Failure
import scala.util.Success
import scala.scalajs.js

class TooltipContent(
  val content: HtmlElement,
  val className: String,
  val root: TooltipRoot,
  val tooltipArrow: Option[TooltipArrow] = None
) {
  private var mounted = false

  val portal: Div = div(
    dataAttr("slot") := "tooltip-content-portal",
    cls := "absolute w-max",
    top.px(0),
    left.px(0),
    content,
    tooltipArrow
  )

  portal.amend(
    // root.store.isHoveringSignal --> Observer[Boolean] { open =>
    //   if (open) {
    //     show()
    //   } else {
    //     hide()
    //   }
    // },
    onMountBind { ctx =>
      root.targetSignal --> Observer[Option[HtmlElement]] { targetOpt =>
        targetOpt.foreach { target =>
          val middlewares = js.Array(
            offset(6),
            flip(),
            shift(
              ShiftOptions(
                padding = 8
              )
            )
          )

          tooltipArrow.foreach { arrowElement =>
            middlewares.push(
              arrow(
                ArrowOptions(
                  element = arrowElement.element.ref
                )
              )
            )
          }

          computePosition(
            reference = target.ref,
            floating = ctx.thisNode.ref,
            options = ComputePositionConfig(
              placement = "top",
              middleware = middlewares
            )
          ).onComplete {
            case Failure(exception) => println(exception)
            case Success(result) =>
              println(s"x: ${result.x}, y: ${result.y}")
              portal.ref.style.left = s"${result.x}px"
              portal.ref.style.top = s"${result.y}px"
              portal.ref.style.display = "block"

              // Position the arrow element if present
              result.middlewareData.arrow.foreach { arrowData =>
                tooltipArrow.foreach { arrowElement =>
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
                    arrowElement.element.ref.style.left = s"${x}px"
                  }

                  // Apply y position if available
                  arrowData.y.foreach { y =>
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
    }
  )

  private val portalRoot: DetachedRoot[Div] = renderDetached(
    portal,
    activateNow = true
  )

  def show() = {
    portal.ref.style.display = "block"
  }

  def hide() = {
    portal.ref.style.display = "none"
  }

  def mount() = {
    if (!mounted) {
      mounted = true
      dom.document.body.appendChild(portalRoot.ref)
      portalRoot.activate()
    }
  }

  def unmount() = {
    if (mounted) {
      mounted = false
      portalRoot.deactivate()
      dom.document.body.removeChild(portalRoot.ref)
    }
  }

  def onHoverChange(isHovering: Boolean) = {
    if (isHovering) mount() else unmount()
  }
}
