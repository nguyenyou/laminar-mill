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

class TooltipContent(val content: HtmlElement, val root: TooltipRoot) {
  private var mounted = false

  val portal = div(
    dataAttr("slot") := "tooltip-content-portal",
    position.absolute,
    width := "max-content",
    top.px(0),
    left.px(0),
    content
  )

  portal.amend(
    // root.store.isHoveringSignal --> Observer[Boolean] { open =>
    //   if (open) {
    //     show()
    //   } else {
    //     hide()
    //   }
    // },
    root.targetSignal --> Observer[Option[HtmlElement]] { targetOpt =>
      targetOpt.foreach { target =>
        computePosition(
          reference = target.ref,
          floating = portal.ref,
          options = ComputePositionConfig(
            placement = "top",
            middleware = js.Array(flip())
          )
        ).onComplete {
          case Failure(exception) => println(exception)
          case Success(result) =>
            println(s"x: ${result.x}, y: ${result.y}")
            portal.ref.style.left = s"${result.x}px"
            portal.ref.style.top = s"${result.y}px"
            portal.ref.style.display = "block"
        }
      }
      // target.foreach { target =>
      //   portal.amend(
      //     top := target.ref.offsetTop,
      //     left := target.ref.offsetLeft
      //   )
      // }
    }
  )

  private val portalRoot: DetachedRoot[Div] = renderDetached(
    portal,
    activateNow = false
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
