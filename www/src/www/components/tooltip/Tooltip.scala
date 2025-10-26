package www.components.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.primitives.tooltip.Tooltip as TooltipPrimitive

object Tooltip {
  def apply()(context: TooltipPrimitive.Root.Context) = {
    TooltipPrimitive.Root()(context)
  }
}

object TooltipTrigger {
  def apply(className: String)(text: String)(using root: TooltipPrimitive.Root) = {
    TooltipPrimitive.Trigger(
      _.className := className
    )(text)
  }
}

object TooltipContent {
  def apply(className: String)(content: HtmlElement)(using root: TooltipPrimitive.Root) = {
    TooltipPrimitive.Content(
      _.className := className
    )(
      content,
      TooltipPrimitive.Arrow(
        _.className := "absolute bg-[#222] w-2 h-2 rotate-45"
      )
    )
  }
}
