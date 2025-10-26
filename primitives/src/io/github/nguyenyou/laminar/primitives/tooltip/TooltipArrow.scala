package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.modifiers.RenderableNode
import io.github.nguyenyou.laminar.primitives.base.*

class TooltipArrow() {
  lazy val element: HtmlElement = render()

  def render(): HtmlElement = {
    span()
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

object TooltipArrow {
  implicit val renderable: RenderableNode[TooltipArrow] = RenderableNode(_.element)

  object ClassNameProp extends ComponentProp[String, TooltipArrow] {
    private[primitives] def setProp(component: TooltipArrow, value: String): Unit = {
      component.setClassName(value)
    }

    private[primitives] def updateProp(component: TooltipArrow, values: Source[String]): Unit = {
      component.updateClassName(values)
    }
  }

  object Props {
    type Selector = Props.type => ComponentModifier[TooltipArrow]

    lazy val className: ClassNameProp.type = ClassNameProp
  }

  def apply(mods: Props.Selector*)(using root: TooltipRoot): TooltipArrow = {
    val resolvedMods: Seq[ComponentModifier[TooltipArrow]] = mods.map(_(Props))
    val tooltipArrow = new TooltipArrow()
    resolvedMods.foreach(_(tooltipArrow))

    root.setupArrow(tooltipArrow)

    tooltipArrow
  }
}
