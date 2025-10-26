package www.components.popover

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import org.scalajs.dom
import io.github.nguyenyou.laminar.primitives.popover.Popover as PopoverPrimitive

object Popover {

  def apply()(init: PopoverPrimitive.Root ?=> Unit) = {
    PopoverPrimitive.root(init)
  }

}

object PopoverTrigger {

  def apply()(text: String)(using root: PopoverPrimitive.Root): Unit = {
    PopoverPrimitive.trigger(text)
  }

}

object PopoverContent {

  def apply(mods: PopoverPrimitive.Content.Props.Selector*)(content: HtmlElement)(using root: PopoverPrimitive.Root): Unit = {
    PopoverPrimitive.content(mods*)(content)
  }

}
