package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.primitives.base.*
import io.github.nguyenyou.laminar.primitives.utils.floating.FloatingUI
import io.github.nguyenyou.laminar.primitives.utils.floating.FloatingUI.*

import scala.collection.mutable.ArrayBuffer

class TooltipRoot(val store: TooltipStore) {
  // parts
  private var tooltipContent: Option[TooltipContent] = None
  private var tooltipArrow: Option[TooltipArrow] = None
  private var tooltipTrigger: Option[TooltipTrigger] = None
  private var tooltipPortal: Option[TooltipPortal] = None

  private val middlewares = ArrayBuffer[Middleware](
    FloatingUI.offset(6),
    FloatingUI.flip(),
    FloatingUI.shift(ShiftOptions(padding = 8))
  )

  def compute(): Unit = {
    for {
      trigger <- tooltipTrigger.map(_.element.ref)
      portal <- tooltipPortal.map(_.element.ref)
    } {
      println(middlewares)
      val result = FloatingUI.computePosition(
        reference = trigger,
        floating = portal,
        placement = "top",
        middleware = middlewares.toSeq
      )

      println(s"X: ${result.x}, Y: ${result.y}")
      portal.style.left = s"${result.x}px"
      portal.style.top = s"${result.y}px"
      portal.style.opacity = "1"

      // Position the arrow element if present
      result.middlewareData.arrow.foreach { arrowData =>
        tooltipArrow.foreach { arrowElement =>
          // Calculate the static side based on placement
          val staticSide = result.placement.split("-")(0) match {
            case "top"    => "bottom"
            case "right"  => "left"
            case "bottom" => "top"
            case "left"   => "right"
            case _        => "bottom"
          }

          // Apply x position if available
          arrowData.x.foreach { x =>
            println(s"ARROW X: ${x}")
            arrowElement.element.ref.style.left = s"${x}px"
          }

          // Apply y position if available
          arrowData.y.foreach { y =>
            println(s"ARROW Y: ${y}")
            arrowElement.element.ref.style.top = s"${y}px"
          }

          // Clear other sides and set the static side offset
          arrowElement.element.ref.style.right = ""
          arrowElement.element.ref.style.bottom = ""
          arrowElement.element.ref.style.setProperty(staticSide, "-4px")
        }
      }
    }

  }

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
      },
      store.isHoveringSignal --> Observer[Boolean] { isHovering =>
        if (isHovering) {
          compute()
        } else {
          ()
        }
      }
    )
  }

  def setContent(content: TooltipContent): Unit = {
    tooltipContent = Some(content)
    println("SET > CONTENT")
  }

  def setArrow(arrow: TooltipArrow): Unit = {
    tooltipArrow = Some(arrow)
    println("SET > ARROW")
    middlewares += FloatingUI.arrow(arrow.element.ref)
  }

  def setTrigger(trigger: TooltipTrigger): Unit = {
    tooltipTrigger = Some(trigger)
    println("SET > TRIGGER")
    setupTrigger(trigger)
  }

  def setPortal(portal: TooltipPortal): Unit = {
    println("SET > PORTAL")
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
