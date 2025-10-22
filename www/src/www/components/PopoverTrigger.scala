package www.components

import io.github.nguyenyou.laminar.api.L.*

object PopoverTrigger {

  def apply(trigger: HtmlElement)(using root: PopoverRoot) = {
    root.setupTrigger(trigger)
  }

  def apply(render: Popover.Store => HtmlElement)(using root: PopoverRoot) = {
    root.setupRenderPropTrigger(render)
  }
}
