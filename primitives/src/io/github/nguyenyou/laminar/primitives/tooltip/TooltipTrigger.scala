package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.modifiers.RenderableNode
import io.github.nguyenyou.laminar.nodes.ChildNode
import io.github.nguyenyou.laminar.primitives.base.*

class TooltipTrigger() extends Component {
  def render(): HtmlElement = {
    button(
      onMountCallback { ctx =>
        println("MOUNTED > TRIGGER")
      }
    )
  }

  def setChild(child: ChildNode.Base) = {
    element.amend(
      child
    )
  }
}

object TooltipTrigger extends HasClassNameProp[TooltipTrigger] {
  object Props extends PropSelector[TooltipTrigger] {
    lazy val className: ClassNameProp.type = ClassNameProp
  }

  def apply(mods: Props.Selector*)(child: ChildNode.Base)(using root: TooltipRoot): TooltipTrigger = {
    val resolvedMods: Seq[ComponentModifier[TooltipTrigger]] = mods.map(_(Props))
    val tooltipTrigger = new TooltipTrigger()
    resolvedMods.foreach(_(tooltipTrigger))

    tooltipTrigger.setChild(child)

    root.setTrigger(tooltipTrigger)

    tooltipTrigger
  }
}
