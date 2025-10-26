package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*

object Tooltip {
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipRoot as Root
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipStore as Store
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipTrigger as Trigger
  export io.github.nguyenyou.laminar.primitives.tooltip.TooltipContent as Content

  def root(init: TooltipRoot ?=> Unit): DynamicInserter = {
    val isHoveringVar = Var(false)
    given tooltip: TooltipRoot = TooltipRoot(TooltipStore(isHoveringVar.signal, isHoveringVar.writer))
    init
    child.maybe <-- tooltip.targetSignal
  }

  def trigger() = {}

  def content() = {}
}
