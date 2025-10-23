package io.github.nguyenyou.laminar.primitives.popover

import io.github.nguyenyou.laminar.api.L.*

object Popover {
  export io.github.nguyenyou.laminar.primitives.popover.PopoverRoot as Root
  export io.github.nguyenyou.laminar.primitives.popover.PopoverStore as Store
  export io.github.nguyenyou.laminar.primitives.popover.PopoverTrigger as Trigger
  export io.github.nguyenyou.laminar.primitives.popover.PopoverContent as Content

  def createRoot(init: PopoverRoot ?=> Unit) = {
    val openVar = Var(false)
    given popover: PopoverRoot = PopoverRoot(PopoverStore(openVar.signal, openVar.writer))
    init
    child.maybe <-- popover.targetSignal
  }

  def createTrigger(text: String)(using root: PopoverRoot) = {
    val trigger = button(text)
    root.setupTrigger(trigger)
  }

  def createContent(mods: PopoverContent.Props.Selector*)(content: HtmlElement)(using root: PopoverRoot): Unit = {
    val popoverContent: PopoverContent = new PopoverContent(content, root)
    val resolvedMods: Seq[PopoverContent.PopoverContentModifier] = mods.map(_(PopoverContent.Props))
    resolvedMods.foreach(_.applyTo(popoverContent))

    root.setContent(popoverContent)
  }
}
