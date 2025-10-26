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
      opacity := 0,
      onMountCallback { ctx =>
        println("MOUNTED > PORTAL")
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
