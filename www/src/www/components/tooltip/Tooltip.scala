package www.components.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.primitives.tooltip.Tooltip as TooltipPrimitive

object Tooltip {
  def apply()(init: TooltipPrimitive.Root ?=> Unit) = {
    TooltipPrimitive.root(init)
  }
}

object TooltipTrigger {
  def apply()(text: String)(using root: TooltipPrimitive.Root) = {
    TooltipPrimitive.trigger(text)
  }
}

object TooltipContent {
  def apply()(content: HtmlElement)(using root: TooltipPrimitive.Root) = {
    TooltipPrimitive.content()(content)
  }
}
