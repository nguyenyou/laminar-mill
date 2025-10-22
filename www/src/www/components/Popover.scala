package www.components

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import org.scalajs.dom
import io.github.nguyenyou.laminar.nodes.ReactiveHtmlElement

case class Popover(store: Popover.Store) {
  var initialized: Option[Boolean] = None

  val targetVar = Var[Option[HtmlElement]](None)
  val targetSignal = targetVar.signal

  val contentWrapper = div(
    position.fixed,
    left.px(0),
    bottom.px(0),
    width.px(400),
    height.px(400),
    cls("hidden")
  )

  contentWrapper.amend(
    store.openSignal --> Observer[Boolean] { open =>
      if (open) {
        contentWrapper.ref.style.display = "block"
      } else {
        contentWrapper.ref.style.display = "none"
      }
    }
  )

  val contentRoot: DetachedRoot[Div] = renderDetached(
    contentWrapper,
    activateNow = false
  )

  private def mount() = {
    if (initialized.isEmpty) {
      dom.document.body.appendChild(contentRoot.ref)
      activateSubscriptions()
      initialized = Some(true)
    }
  }

  private def unmount() = {
    if (initialized.isDefined) {
      deactivateSubscriptions()
      dom.document.body.removeChild(contentRoot.ref)
      initialized = None
    }
  }

  private def activateSubscriptions() = {
    contentRoot.activate()
  }
  private def deactivateSubscriptions() = {
    contentRoot.deactivate()
  }

  def show() = {}

  def setupTrigger(trigger: HtmlElement) = {
    trigger.amend(
      onClick(_.sample(store.openSignal)) --> Observer[Boolean] { open =>
        store.onChange.onNext(!open)
      },
      store.openSignal --> Observer[Boolean] { open =>
        if (open) mount() else unmount()
      }
    )
    targetVar.set(Some(trigger))
    mount()
  }

  def setContent(content: HtmlElement) = {
    contentWrapper.amend(content)
  }
}

object Popover {
  case class Store(openSignal: Signal[Boolean], onChange: Observer[Boolean])

  def Root()(init: Popover ?=> Unit) = {
    val openVar = Var(false)
    val store = Store(openVar.signal, openVar.writer)
    given popover: Popover = Popover(store)
    init
    child.maybe <-- popover.targetSignal
  }

  def Root(store: Store)(init: Popover ?=> Unit) = {
    given popover: Popover = Popover(store)
    init
    child.maybe <-- popover.targetSignal
  }

  def Trigger(trigger: HtmlElement)(using ctx: Popover) = {
    ctx.setupTrigger(trigger)
  }

  def Content(content: HtmlElement)(using ctx: Popover) = {
    ctx.setContent(content)
  }
}
