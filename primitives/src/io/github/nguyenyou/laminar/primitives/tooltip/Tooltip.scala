package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*

object Tooltip {
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipRoot as Root
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipStore as Store
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipTrigger as Trigger
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipContent as Content
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipArrow as Arrow

  def root(init: TooltipRoot ?=> Unit): DynamicInserter = {
    val isHoveringVar = Var(true)
    given tooltip: TooltipRoot = TooltipRoot(TooltipStore(isHoveringVar.signal, isHoveringVar.writer))
    init
    child.maybe <-- tooltip.targetSignal
  }

  def trigger(className: String)(text: String)(using root: TooltipRoot): Unit = {
    root.setupTrigger(button(cls(className), text))
  }

  def content(className: String)(content: HtmlElement, tooltipArrow: Option[TooltipArrow] = None)(using root: TooltipRoot): Unit = {
    val x = TooltipContent(
      _.className := className
    )(content)
    val tooltipContent: TooltipContent = new TooltipContent(
      root = root,
      tooltipArrow = tooltipArrow
    )
    /*
    TooltipContent(

    )

     */
    root.setupContent(tooltipContent)
  }
}
