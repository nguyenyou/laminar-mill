package www.components.popover

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import org.scalajs.dom
import www.components.popover.Popover

case class PopoverRoot(store: PopoverStore) {
  private var popoverContentRef: Option[PopoverContent] = None

  val targetVar = Var[Option[HtmlElement]](None)
  val targetSignal = targetVar.signal

  def setupTrigger(trigger: HtmlElement) = {
    trigger.amend(
      onClick(_.sample(store.openSignal)) --> Observer[Boolean] { open =>
        store.onOpenChange.onNext(!open)
      },
      store.openSignal --> Observer[Boolean] { isOpen =>
        popoverContentRef.foreach(_.onOpenChange(isOpen))
      }
    )
    targetVar.set(Some(trigger))
    popoverContentRef.foreach(_.mount())
  }
  def setupRenderPropTrigger(renderProps: PopoverStore => HtmlElement) = {
    val trigger = renderProps(store)
    trigger.amend(
      store.openSignal --> Observer[Boolean] { isOpen =>
        popoverContentRef.foreach(_.onOpenChange(isOpen))
      }
    )
    targetVar.set(Some(trigger))
    popoverContentRef.foreach(_.unmount())
  }

  def setContent(content: PopoverContent) = {
    popoverContentRef = Some(content)
  }
}
