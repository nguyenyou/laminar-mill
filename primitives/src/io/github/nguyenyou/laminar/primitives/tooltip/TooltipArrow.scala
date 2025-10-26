package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.modifiers.RenderableNode

class TooltipArrow(className: String) {
  lazy val element: HtmlElement = render()

  def render(): HtmlElement = {
    span(
      cls := className
    )
  }
}

object TooltipArrow {
  implicit val renderable: RenderableNode[TooltipArrow] = RenderableNode(_.element)
}
