package www.components.popover

import io.github.nguyenyou.laminar.api.L.*
import www.components.popover.Popover
import www.components.popover.PopoverRoot

object PopoverTrigger {

  def apply()(text: String)(using root: PopoverRoot): Unit = {
    val trigger = button(text)
    root.setupTrigger(trigger)
  }

  def apply()(trigger: HtmlElement)(using root: PopoverRoot): Unit = {
    root.setupTrigger(trigger)
  }

  def apply()(render: PopoverStore => HtmlElement)(using root: PopoverRoot): Unit = {
    root.setupRenderPropTrigger(render)
  }
}
