package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.api.L
import io.github.nguyenyou.laminar.nodes.{ChildNode, DetachedRoot}
import org.scalajs.dom
import io.github.nguyenyou.laminar.primitives.base.*

class TooltipPortal(
  val root: TooltipRoot
) {
  private var mounted = false

  val element: Div = div(
    dataAttr("slot") := "tooltip-portal",
    cls := "absolute w-max",
    top.px(0),
    left.px(0)
  )

  private val portalRoot: DetachedRoot[Div] = renderDetached(
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

  def setClassName(value: String) = {
    element.amend(
      cls := value
    )
  }

  def updateClassName(values: Source[String]) = {
    element.amend(
      cls <-- values.toObservable
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
