package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.primitives.base.*
import io.github.nguyenyou.floatingUI.FloatingUI
import io.github.nguyenyou.floatingUI.FloatingUI.*

import scala.collection.mutable.ArrayBuffer

// Context Value of Tooltip Context
class TooltipRoot(val store: TooltipStore) {
  // parts
  private var tooltipContent: Option[TooltipContent] = None
  private var tooltipArrow: Option[TooltipArrow] = None
  private var tooltipTrigger: Option[TooltipTrigger] = None
  private var tooltipPortal: Option[TooltipPortal] = None

  private val middlewares = ArrayBuffer[Middleware](
    FloatingUI.offset(6),
    FloatingUI.flip(),
    FloatingUI.shift(ShiftOptions(padding = Left(8)))
  )

  // Store the cleanup function from autoUpdate
  private var autoUpdateCleanup: Option[() => Unit] = None

  /** Update the tooltip position based on current state. */
  private def updatePosition(): Unit = {
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

  /** Start automatic position updates using FloatingUI's autoUpdate. */
  private def startAutoUpdate(): Unit = {
    // Clean up any existing autoUpdate
    stopAutoUpdate()

    for {
      trigger <- tooltipTrigger.map(_.element.ref)
      portal <- tooltipPortal.map(_.element.ref)
    } {
      println("Starting autoUpdate for tooltip")

      // Set up autoUpdate with default options
      val cleanup = FloatingUI.autoUpdate(
        reference = trigger,
        floating = portal,
        update = () => updatePosition(),
        options = AutoUpdateOptions(
          ancestorScroll = true,
          ancestorResize = true,
          elementResize = true,
          layoutShift = true,
          animationFrame = false
        )
      )

      autoUpdateCleanup = Some(cleanup)
    }
  }

  /** Stop automatic position updates and clean up resources. */
  private def stopAutoUpdate(): Unit = {
    autoUpdateCleanup.foreach { cleanup =>
      println("Stopping autoUpdate for tooltip")
      cleanup()
    }
    autoUpdateCleanup = None
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
          // Start automatic position updates when tooltip becomes visible
          startAutoUpdate()
        } else {
          // Stop automatic updates and clean up when tooltip is hidden
          stopAutoUpdate()
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
    val isHoveringVar = Var(false)
    // Context Value is given
    given tooltipRoot: TooltipRoot = new TooltipRoot(TooltipStore(isHoveringVar.signal, isHoveringVar.writer))
    context
    tooltipRoot.trigger
  }
}
