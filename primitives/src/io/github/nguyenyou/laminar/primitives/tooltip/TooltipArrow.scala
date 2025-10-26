package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.modifiers.RenderableNode
import io.github.nguyenyou.laminar.primitives.base.*

class TooltipArrow() extends Component {
  def render(): HtmlElement = {
    span()
  }
}

object TooltipArrow extends ComponentProps[TooltipArrow] {
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
