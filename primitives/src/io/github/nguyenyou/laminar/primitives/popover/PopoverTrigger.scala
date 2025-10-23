package io.github.nguyenyou.laminar.primitives.popover

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.primitives.popover.Popover
import io.github.nguyenyou.laminar.primitives.popover.PopoverRoot

import io.github.nguyenyou.laminar.primitives.popover.Popover as PopoverPrimitive

object PopoverTrigger {

  def apply()(text: String)(using root: PopoverPrimitive.Root): Unit = {
    val trigger = button(text)
    root.setupTrigger(trigger)
  }

  def apply()(trigger: HtmlElement)(using root: PopoverPrimitive.Root): Unit = {
    root.setupTrigger(trigger)
  }

  def apply()(render: PopoverPrimitive.Store => HtmlElement)(using root: PopoverPrimitive.Root): Unit = {
    root.setupRenderPropTrigger(render)
  }
}
