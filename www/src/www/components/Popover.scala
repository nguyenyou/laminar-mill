package www.components

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import org.scalajs.dom

object Popover {
  case class Store(openSignal: Signal[Boolean], onOpenChange: Observer[Boolean])

  def apply(openSignal: Signal[Boolean], onChangeOpen: Observer[Boolean])(init: PopoverRoot ?=> Unit) = {
    given popover: PopoverRoot = PopoverRoot(Store(openSignal, onChangeOpen))
    init
    child.maybe <-- popover.targetSignal
  }

  def apply()(init: PopoverRoot ?=> Unit) = {
    val openVar = Var(false)
    given popover: PopoverRoot = PopoverRoot(Store(openVar.signal, openVar.writer))
    init
    child.maybe <-- popover.targetSignal
  }

  def apply(store: Store)(init: PopoverRoot ?=> Unit) = {
    given popover: PopoverRoot = PopoverRoot(store)
    init
    child.maybe <-- popover.targetSignal
  }
}
