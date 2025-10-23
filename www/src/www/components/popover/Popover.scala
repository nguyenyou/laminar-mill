package www.components.popover

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import org.scalajs.dom
// import www.components.popover.PopoverRoot
import www.primitives.popover.Popover as PopoverPrimitive

object Popover {

  def apply()(init: PopoverPrimitive.Root ?=> Unit) = {
    val openVar = Var(false)
    given popover: PopoverPrimitive.Root = PopoverPrimitive.Root(PopoverPrimitive.Store(openVar.signal, openVar.writer))
    init
    child.maybe <-- popover.targetSignal
  }

  object Trigger {
    def apply()(text: String)(using root: PopoverPrimitive.Root): Unit = {
      val trigger = button(text)
      root.setupTrigger(trigger)
    }

  }

  object Content {
    def apply(mods: PopoverPrimitive.Content.Props.Selector*)(content: HtmlElement)(using root: PopoverPrimitive.Root): Unit = {
      val popoverContent: PopoverPrimitive.Content = new PopoverPrimitive.Content(content, root)
      val resolvedMods: Seq[PopoverPrimitive.Content.PopoverContentModifier] = mods.map(_(PopoverPrimitive.Content.Props))
      resolvedMods.foreach(_.applyTo(popoverContent))

      root.setContent(popoverContent)
    }
  }
}
