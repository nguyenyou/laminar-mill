package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.modifiers.RenderableNode
import io.github.nguyenyou.laminar.nodes.ChildNode
import io.github.nguyenyou.laminar.primitives.base.*

class TooltipTrigger() {
  lazy val element: HtmlElement = render()

  def render(): HtmlElement = {
    button()
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

  def setChild(child: ChildNode.Base) = {
    element.amend(
      child
    )
  }
}

object TooltipTrigger {
  implicit val renderable: RenderableNode[TooltipTrigger] = RenderableNode(_.element)

  object ClassNameProp extends ComponentProp[String, TooltipTrigger] {
    private[primitives] def setProp(component: TooltipTrigger, value: String): Unit = {
      component.setClassName(value)
    }

    private[primitives] def updateProp(component: TooltipTrigger, values: Source[String]): Unit = {
      component.updateClassName(values)
    }
  }

  object Props {
    type Selector = Props.type => ComponentModifier[TooltipTrigger]

    lazy val className: ClassNameProp.type = ClassNameProp
  }

  def apply(mods: Props.Selector*)(child: ChildNode.Base)(using root: TooltipRoot): TooltipTrigger = {
    val resolvedMods: Seq[ComponentModifier[TooltipTrigger]] = mods.map(_(Props))
    val tooltipTrigger = new TooltipTrigger()
    resolvedMods.foreach(_(tooltipTrigger))

    tooltipTrigger.setChild(child)

    root.setTrigger(tooltipTrigger.element)

    tooltipTrigger
  }
}
