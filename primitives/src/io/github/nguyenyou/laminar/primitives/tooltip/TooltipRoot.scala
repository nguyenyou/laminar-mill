package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.primitives.base.*

class TooltipRoot(val store: TooltipStore) {
  // parts
  private var tooltipContent: Option[TooltipContent] = None
  private var tooltipArrow: Option[TooltipArrow] = None
  private var tooltipTrigger: Option[TooltipTrigger] = None
  private var tooltipPortal: Option[TooltipPortal] = None

  def setupTrigger(trigger: TooltipTrigger): Unit = {
    trigger.element.amend(
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

  def setContent(content: TooltipContent): Unit = {
    tooltipContent = Some(content)
  }

  def setArrow(arrow: TooltipArrow): Unit = {
    tooltipArrow = Some(arrow)
  }

  def setTrigger(trigger: TooltipTrigger): Unit = {
    tooltipTrigger = Some(trigger)
    setupTrigger(trigger)
  }

  def setPortal(portal: TooltipPortal): Unit = {
    tooltipPortal = Some(portal)
  }

  def content: Option[TooltipContent] = tooltipContent
  def arrow: Option[TooltipArrow] = tooltipArrow
  def trigger: Option[TooltipTrigger] = tooltipTrigger
  def portal: Option[TooltipPortal] = tooltipPortal
}

object TooltipRoot {
  def apply()(context: TooltipContext): Option[TooltipTrigger] = {
    val isHoveringVar = Var(true)
    given tooltipRoot: TooltipRoot = new TooltipRoot(TooltipStore(isHoveringVar.signal, isHoveringVar.writer))
    context
    tooltipRoot.trigger
  }
}
