package io.github.nguyenyou.laminar.primitives.popover

import io.github.nguyenyou.laminar.api.L.*
import org.scalajs.dom

class PopoverRoot(val store: PopoverStore) {
  private var popoverContent: Option[PopoverContent] = None

  val targetVar = Var[Option[HtmlElement]](None)
  val targetSignal = targetVar.signal

  def setupTrigger(trigger: HtmlElement) = {
    trigger.amend(
      onClick(_.sample(store.openSignal)) --> Observer[Boolean] { open =>
        store.onOpenChange.onNext(!open)
      },
      store.openSignal --> Observer[Boolean] { isOpen =>
        popoverContent.foreach(_.onOpenChange(isOpen))
      }
    )
    targetVar.set(Some(trigger))
    popoverContent.foreach(_.mount())
  }
  def setupRenderPropTrigger(renderProps: PopoverStore => HtmlElement) = {
    val trigger = renderProps(store)
    trigger.amend(
      store.openSignal --> Observer[Boolean] { isOpen =>
        popoverContent.foreach(_.onOpenChange(isOpen))
      }
    )
    targetVar.set(Some(trigger))
    popoverContent.foreach(_.unmount())
  }

  def setupContent(content: PopoverContent) = {
    popoverContent = Some(content)
  }
}
