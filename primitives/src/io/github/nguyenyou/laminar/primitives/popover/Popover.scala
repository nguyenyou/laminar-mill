package io.github.nguyenyou.laminar.primitives.popover

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.primitives.base.*

object Popover {
  export io.github.nguyenyou.laminar.primitives.popover.PopoverRoot as Root
  export io.github.nguyenyou.laminar.primitives.popover.PopoverStore as Store
  export io.github.nguyenyou.laminar.primitives.popover.PopoverTrigger as Trigger
  export io.github.nguyenyou.laminar.primitives.popover.PopoverContent as Content

  def root(init: PopoverRoot ?=> Unit) = {
    val openVar = Var(false)
    given popover: PopoverRoot = PopoverRoot(PopoverStore(openVar.signal, openVar.writer))
    init
    child.maybe <-- popover.targetSignal
  }

  def trigger(text: String)(using root: PopoverRoot) = {
    val trigger = button(text)
    root.setupTrigger(trigger)
  }

  def content(mods: PopoverContent.Props.Selector*)(content: HtmlElement)(using root: PopoverRoot): Unit = {
    val popoverContent: PopoverContent = new PopoverContent(content, root)
    val resolvedMods: Seq[ComponentModifier[PopoverContent]] = mods.map(_(PopoverContent.Props))
    resolvedMods.foreach(_.apply(popoverContent))

    root.setupContent(popoverContent)
  }
}
