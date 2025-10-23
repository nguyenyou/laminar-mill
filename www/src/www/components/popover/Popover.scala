package www.components.popover

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import org.scalajs.dom
import www.components.popover.PopoverRoot

object Popover {
  sealed trait PopoverModifier
  class StoreProp extends PopoverModifier
  class DefaultOpenProp extends PopoverModifier
  class OpenProp extends PopoverModifier

  object Props {
    type Selector = Props.type => PopoverModifier

    lazy val store: StoreProp = StoreProp()
    lazy val defaultOpen: DefaultOpenProp = DefaultOpenProp()
    lazy val open: OpenProp = OpenProp()
  }

  // def apply(openSignal: Signal[Boolean], onChangeOpen: Observer[Boolean])(init: PopoverRoot ?=> Unit) = {
  //   given popover: PopoverRoot = PopoverRoot(PopoverStore(openSignal, onChangeOpen))
  //   init
  //   child.maybe <-- popover.targetSignal
  // }

  // def apply(defaultOpen: Boolean = false)(init: PopoverRoot ?=> Unit) = {
  //   val openVar = Var(false)
  //   given popover: PopoverRoot = PopoverRoot(PopoverStore(openVar.signal, openVar.writer))
  //   init
  //   child.maybe <-- popover.targetSignal
  // }

  // def apply(store: PopoverStore)(init: PopoverRoot ?=> Unit) = {
  //   given popover: PopoverRoot = PopoverRoot(store)
  //   init
  //   child.maybe <-- popover.targetSignal
  // }

  def apply(mods: Props.Selector*)(init: PopoverRoot ?=> Unit) = {
    val resolvedMods: Seq[PopoverModifier] = mods.map(_(Props))

    val openVar = Var(false)
    given popover: PopoverRoot = PopoverRoot(PopoverStore(openVar.signal, openVar.writer))
    init
    child.maybe <-- popover.targetSignal
  }
}
