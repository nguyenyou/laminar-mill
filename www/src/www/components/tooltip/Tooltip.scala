package www.components.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.primitives.tooltip.{TooltipPortal, TooltipTrigger, Tooltip as TooltipPrimitive}

object Tooltip {
  def apply()(context: TooltipPrimitive.Context): Option[TooltipTrigger] = {
    TooltipPrimitive.Root()(context)
  }
}

object TooltipTrigger {
  def apply(mods: TooltipPrimitive.Trigger.Props.Selector*)(text: String)(using root: TooltipPrimitive.Root): TooltipTrigger = {
    TooltipPrimitive.Trigger(
      mods*
    )(text)
  }
}

object TooltipContent {
  def apply(mods: TooltipPrimitive.Content.Props.Selector*)(content: HtmlElement)(using root: TooltipPrimitive.Root): TooltipPortal = {
    TooltipPrimitive.Portal() {
      TooltipPrimitive.Content(
        mods*
      )(
        content,
        TooltipPrimitive.Arrow(
          _.className := "absolute bg-[#222] w-2 h-2 rotate-45"
        )
      )
    }
  }
}
