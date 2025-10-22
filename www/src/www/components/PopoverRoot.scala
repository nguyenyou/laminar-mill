package www.components

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import org.scalajs.dom

case class PopoverRoot(store: Popover.Store) {
  var initialized: Option[Boolean] = None

  val targetVar = Var[Option[HtmlElement]](None)
  val targetSignal = targetVar.signal

  val portal = div(
    dataAttr("slot") := "popover-content-wrapper",
    position.fixed,
    left.px(0),
    bottom.px(0),
    width.px(400),
    height.px(400),
    cls("hidden")
  )

  portal.amend(
    store.openSignal --> Observer[Boolean] { open =>
      if (open) {
        portal.ref.style.display = "block"
      } else {
        portal.ref.style.display = "none"
      }
    }
  )

  val portalRoot: DetachedRoot[Div] = renderDetached(
    portal,
    activateNow = false
  )

  private def mount() = {
    if (initialized.isEmpty) {
      dom.document.body.appendChild(portalRoot.ref)
      activateSubscriptions()
      initialized = Some(true)
    }
  }

  private def unmount() = {
    if (initialized.isDefined) {
      deactivateSubscriptions()
      dom.document.body.removeChild(portalRoot.ref)
      initialized = None
    }
  }

  private def activateSubscriptions() = {
    portalRoot.activate()
  }
  private def deactivateSubscriptions() = {
    portalRoot.deactivate()
  }

  def show() = {}

  def setupTrigger(trigger: HtmlElement) = {
    trigger.amend(
      onClick(_.sample(store.openSignal)) --> Observer[Boolean] { open =>
        store.onOpenChange.onNext(!open)
      },
      store.openSignal --> Observer[Boolean] { open =>
        if (open) mount() else unmount()
      }
    )
    targetVar.set(Some(trigger))
    mount()
  }
  def setupRenderPropTrigger(renderProps: Popover.Store => HtmlElement) = {
    val trigger = renderProps(store)
    trigger.amend(
      store.openSignal --> Observer[Boolean] { open =>
        if (open) mount() else unmount()
      }
    )
    targetVar.set(Some(trigger))
    mount()
  }

  def setContent(content: HtmlElement) = {
    portal.amend(content.amend(dataAttr("slot") := "popover-content"))
  }
}
