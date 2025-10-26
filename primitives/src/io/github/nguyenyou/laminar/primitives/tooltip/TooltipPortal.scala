package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.api.L
import io.github.nguyenyou.laminar.nodes.{ChildNode, DetachedRoot}
import org.scalajs.dom
import io.github.nguyenyou.laminar.primitives.base.*
import io.github.nguyenyou.laminar.nodes.ReactiveHtmlElement

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

  def render(): HtmlElement = {
    div(
      dataAttr("slot") := "tooltip-portal",
      cls := "absolute w-max",
      top.px(0),
      left.px(0)
    )
  }
}

object TooltipPortal {
  object ClassNameProp extends ComponentProp[String, TooltipPortal] {
    private[primitives] def setProp(component: TooltipPortal, value: String): Unit = {
      component.setClassName(value)
    }

    private[primitives] def updateProp(component: TooltipPortal, values: Source[String]): Unit = {
      component.updateClassName(values)
    }
  }

  object Props {
    type Selector = Props.type => ComponentModifier[TooltipPortal]

    lazy val className: ClassNameProp.type = ClassNameProp
  }

}
