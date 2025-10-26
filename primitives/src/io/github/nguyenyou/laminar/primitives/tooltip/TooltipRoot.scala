package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*

class TooltipRoot(val store: TooltipStore) {
  private var tooltipContent: Option[TooltipContent] = None
  private var tooltipArrow: Option[TooltipArrow] = None
  private var tooltipTrigger: Option[HtmlElement] = None

  def setupTrigger(trigger: HtmlElement): Unit = {
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
    tooltipContent.foreach(_.mount())
  }

  def setupContent(content: TooltipContent): Unit = {
    tooltipContent = Some(content)
  }

  def setupArrow(arrow: TooltipArrow): Unit = {
    tooltipArrow = Some(arrow)
  }

  def setTrigger(trigger: HtmlElement): Unit = {
    tooltipTrigger = Some(trigger)
    setupTrigger(trigger)
  }

  def content: Option[TooltipContent] = tooltipContent
  def arrow: Option[TooltipArrow] = tooltipArrow
  def trigger: Option[HtmlElement] = tooltipTrigger
}

object TooltipRoot {
  type Context = TooltipRoot ?=> Unit

  def apply()(context: Context): Option[HtmlElement] = {
    val isHoveringVar = Var(true)
    given tooltipRoot: TooltipRoot = new TooltipRoot(TooltipStore(isHoveringVar.signal, isHoveringVar.writer))
    context
    tooltipRoot.trigger
  }
}
