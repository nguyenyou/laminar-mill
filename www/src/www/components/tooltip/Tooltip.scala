package www.components.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.primitives.tooltip.Tooltip as TooltipPrimitive

object Tooltip {
  def apply()(init: TooltipPrimitive.Root ?=> Unit) = {
    TooltipPrimitive.root(init)
  }
}

object TooltipTrigger {
  def apply(className: String)(text: String)(using root: TooltipPrimitive.Root) = {
    TooltipPrimitive.trigger(className)(text)
  }
}

object TooltipContent {
  def apply(className: String)(content: HtmlElement)(using root: TooltipPrimitive.Root) = {
    TooltipPrimitive.content(className = className)(content)
  }
}
