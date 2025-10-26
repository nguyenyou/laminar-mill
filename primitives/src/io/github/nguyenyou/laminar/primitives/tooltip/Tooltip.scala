package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.nodes.ChildNode

object Tooltip {
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipRoot as Root
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipStore as Store
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipTrigger as Trigger
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipContent as Content
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipArrow as Arrow

  def root(init: TooltipRoot ?=> Unit): Option[HtmlElement] = {
    val isHoveringVar = Var(true)
    given tooltip: TooltipRoot = TooltipRoot(TooltipStore(isHoveringVar.signal, isHoveringVar.writer))
    init
    tooltip.trigger
  }
}
