package www.components.popover

import io.github.nguyenyou.laminar.api.L.*
import www.components.popover.Popover
import www.components.popover.PopoverRoot

object PopoverTrigger {

  def apply(trigger: HtmlElement)(using root: PopoverRoot) = {
    root.setupTrigger(trigger)
  }

  def apply(render: Popover.Store => HtmlElement)(using root: PopoverRoot) = {
    root.setupRenderPropTrigger(render)
  }
}
