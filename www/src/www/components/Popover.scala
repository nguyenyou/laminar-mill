package www.components

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import org.scalajs.dom
import io.github.nguyenyou.laminar.nodes.ReactiveHtmlElement

case class Popover() {
  var initialized: Option[Boolean] = None

  val openVar = Var[Option[Boolean]](None)
  val openSignal = openVar.signal
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
    openSignal --> Observer[Option[Boolean]] { openOpt =>
      openOpt match
        case Some(open) =>
          if (open) {
            contentWrapper.ref.style.display = "block"
          } else {
            contentWrapper.ref.style.display = "none"
          }
        case None =>
          contentWrapper.ref.style.display = "none"
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
      onClick(_.sample(openSignal)) --> Observer[Option[Boolean]] {
        case Some(open) =>
          openVar.set(Some(!open))
        case None =>
          openVar.set(Some(true))
      },
      openSignal --> Observer[Option[Boolean]] {
        case Some(open) =>
          if (open) mount() else unmount()
        case None =>
          ()
      }
    )
    targetVar.set(Some(trigger))
    mount()
  }

  def open() = {
    activateSubscriptions()
  }

  def setContent(content: HtmlElement) = {
    contentWrapper.amend(content)
  }
}

object Popover {

  def apply(init: Popover ?=> Unit) = {
    given popover: Popover = new Popover()
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
