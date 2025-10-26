package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.primitives.base.*
import io.github.nguyenyou.facades.floatingui.FloatingUIDOM

import scala.scalajs.js

class TooltipRoot(val store: TooltipStore) {
  // parts
  private var tooltipContent: Option[TooltipContent] = None
  private var tooltipArrow: Option[TooltipArrow] = None
  private var tooltipTrigger: Option[TooltipTrigger] = None
  private var tooltipPortal: Option[TooltipPortal] = None

  val floatinguiMiddlewares = js.Array(
    FloatingUIDOM.offset(6),
    FloatingUIDOM.flip(),
    FloatingUIDOM.shift(
      FloatingUIDOM.ShiftOptions(
        padding = 8
      )
    )
  )

  def setupTrigger(trigger: TooltipTrigger): Unit = {
    trigger.element.amend(
      onMouseEnter --> Observer { _ =>
        store.onHoverChange.onNext(true)
      },
      onMouseLeave --> Observer { _ =>
        store.onHoverChange.onNext(false)
      },
      store.isHoveringSignal --> Observer[Boolean] { isHovering =>
        println(s"IS HOVERING: $isHovering")
        tooltipPortal.foreach(_.onHoverChange(isHovering))
      }
    )
  }

  def setContent(content: TooltipContent): Unit = {
    tooltipContent = Some(content)
  }

  def setArrow(arrow: TooltipArrow): Unit = {
    tooltipArrow = Some(arrow)
    floatinguiMiddlewares.push(
      FloatingUIDOM.arrow(
        FloatingUIDOM.ArrowOptions(
          element = arrow.element.ref
        )
      )
    )
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
