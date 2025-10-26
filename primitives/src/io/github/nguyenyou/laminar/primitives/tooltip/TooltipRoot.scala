package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*

class TooltipRoot(val store: TooltipStore) {
  private var tooltipContent: Option[TooltipContent] = None

  val targetVar = Var[Option[HtmlElement]](None)
  val targetSignal = targetVar.signal

  def setupTrigger(trigger: HtmlElement) = {
    trigger.amend(
      onMouseEnter --> Observer { _ =>
        store.onHoverChange.onNext(true)
      },
      onMouseLeave --> Observer { _ =>
        store.onHoverChange.onNext(false)
      },
      store.isHoveringSignal --> Observer[Boolean] { isHovering =>
        tooltipContent.foreach(_.onHoverChange(isHovering))
      }
    )
    targetVar.set(Some(trigger))
    tooltipContent.foreach(_.mount())
  }

  def setupContent(content: TooltipContent) = {
    tooltipContent = Some(content)
  }
}
