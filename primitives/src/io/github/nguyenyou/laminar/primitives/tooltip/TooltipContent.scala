package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.api.L
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import io.github.nguyenyou.laminar.modifiers.RenderableNode
import org.scalajs.dom
import io.github.nguyenyou.facades.floatingui.FloatingUIDOM.*
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import scala.scalajs.js.Thenable.Implicits.thenable2future
import scala.util.Failure
import scala.util.Success
import scala.scalajs.js
import io.github.nguyenyou.laminar.primitives.base.*

class TooltipContent(
  val tooltipArrow: Option[TooltipArrow] = None,
  val root: TooltipRoot
) {
  private var mounted = false

  val contentWrapper = div()

  val portal: Div = div(
    dataAttr("slot") := "tooltip-content-portal",
    cls := "absolute w-max",
    top.px(0),
    left.px(0),
    contentWrapper,
    tooltipArrow
  )

  portal.amend(
    // root.store.isHoveringSignal --> Observer[Boolean] { open =>
    //   if (open) {
    //     show()
    //   } else {
    //     hide()
    //   }
    // },
    onMountBind { ctx =>
      root.targetSignal --> Observer[Option[HtmlElement]] { targetOpt =>
        targetOpt.foreach { target =>
          val middlewares = js.Array(
            offset(6),
            flip(),
            shift(
              ShiftOptions(
                padding = 8
              )
            )
          )

          tooltipArrow.foreach { arrowElement =>
            middlewares.push(
              arrow(
                ArrowOptions(
                  element = arrowElement.element.ref
                )
              )
            )
          }

          computePosition(
            reference = target.ref,
            floating = ctx.thisNode.ref,
            options = ComputePositionConfig(
              placement = "top",
              middleware = middlewares
            )
          ).onComplete {
            case Failure(exception) => println(exception)
            case Success(result) =>
              println(s"x: ${result.x}, y: ${result.y}")
              portal.ref.style.left = s"${result.x}px"
              portal.ref.style.top = s"${result.y}px"
              portal.ref.style.display = "block"

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
                    arrowElement.element.ref.style.left = s"${x}px"
                  }

                  // Apply y position if available
                  arrowData.y.foreach { y =>
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
      }
    }
  )

  private val portalRoot: DetachedRoot[Div] = renderDetached(
    portal,
    activateNow = true
  )

  def show() = {
    portal.ref.style.display = "block"
  }

  def hide() = {
    portal.ref.style.display = "none"
  }

  def mount() = {
    if (!mounted) {
      mounted = true
      dom.document.body.appendChild(portalRoot.ref)
      portalRoot.activate()
    }
  }

  def unmount() = {
    if (mounted) {
      mounted = false
      portalRoot.deactivate()
      dom.document.body.removeChild(portalRoot.ref)
    }
  }

  def onHoverChange(isHovering: Boolean) = {
    if (isHovering) mount() else unmount()
  }

  def setClassName(value: String) = {
    contentWrapper.amend(
      cls := value
    )
  }

  def updateClassName(values: Source[String]) = {
    contentWrapper.amend(
      cls <-- values.toObservable
    )
  }

  def setContent(value: HtmlElement) = {
    contentWrapper.amend(
      value
    )
  }

  def updateContent(values: Source[HtmlElement]) = {
    contentWrapper.amend(
      child <-- values.toObservable
    )
  }

  def setRoot(value: TooltipRoot) = {}
}

object TooltipContent {
  object ClassNameProp extends ComponentProp[String, TooltipContent] {
    private[primitives] def setProp(component: TooltipContent, value: String): Unit = {
      component.setClassName(value)
    }

    private[primitives] def updateProp(component: TooltipContent, values: Source[String]): Unit = {
      component.updateClassName(values)
    }
  }

  object ContentProp extends ComponentProp[HtmlElement, TooltipContent] {
    private[primitives] def setProp(component: TooltipContent, value: HtmlElement): Unit = {
      component.setContent(value)
    }

    private[primitives] def updateProp(component: TooltipContent, values: Source[HtmlElement]): Unit = {
      component.updateContent(values)
    }
  }

  object Props {
    type Selector = Props.type => ComponentModifier[TooltipContent]

    lazy val className: ClassNameProp.type = ClassNameProp
    lazy val content: ContentProp.type = ContentProp
  }

  def apply(mods: Props.Selector*)(children: HtmlElement)(using root: TooltipRoot): TooltipContent = {
    val resolvedMods: Seq[ComponentModifier[TooltipContent]] = mods.map(_(Props))
    val tooltipContent = new TooltipContent(root = root)
    resolvedMods.foreach(_(tooltipContent))

    root.setupContent(tooltipContent)

    tooltipContent
  }
}
