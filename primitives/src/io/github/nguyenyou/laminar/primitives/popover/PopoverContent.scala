package io.github.nguyenyou.laminar.primitives.popover

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.api.L
import io.github.nguyenyou.laminar.primitives.popover.PopoverRoot
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import io.github.nguyenyou.laminar.primitives.base.*
import org.scalajs.dom

class PopoverContent(val content: HtmlElement, val root: PopoverRoot) {
  private var initialized = false

  content.amend(
    dataAttr("slot") := "popover-content"
  )

  val portal = div(
    dataAttr("slot") := "popover-content-portal",
    position.fixed,
    width.px(400),
    height.px(400),
    cls("hidden"),
    content
  )

  portal.amend(
    root.store.openSignal --> Observer[Boolean] { open =>
      if (open) {
        show()
      } else {
        hide()
      }
    }
  )

  private val portalRoot: DetachedRoot[Div] = renderDetached(
    portal,
    activateNow = false
  )

  def show() = {
    portal.ref.style.display = "block"
  }

  def hide() = {
    portal.ref.style.display = "none"
  }

  def mount() = {
    if (!initialized) {
      dom.document.body.appendChild(portalRoot.ref)
      portalRoot.activate()
      initialized = true
    }
  }
  def unmount() = {
    if (initialized) {
      portalRoot.deactivate()
      dom.document.body.removeChild(portalRoot.ref)
      initialized = false
    }
  }

  def onOpenChange(isOpen: Boolean) = {
    if (isOpen) mount() else unmount()
  }

  def processSize(side: PopoverContent.Side) = {
    side match
      case PopoverContent.Side.Top    => "top-0 left-0"
      case PopoverContent.Side.Bottom => "bottom-0 left-0"
  }

  def setSide(side: PopoverContent.Side) = {
    println("set side")
    portal.amend(
      cls := processSize(side)
    )
  }

  def setSide(side: L.Source[PopoverContent.Side]) = {
    portal.amend(
      cls <-- side.toObservable.map { processSize }
    )
  }

}

object PopoverContent {
  def apply(content: HtmlElement)(using root: PopoverRoot): Unit = {
    val popoverContent: PopoverContent = new PopoverContent(content, root)
    root.setContent(popoverContent)
  }

  enum Side {
    case Top, Bottom
  }

  object SideProp extends ComponentProp[Side, PopoverContent] {
    def setProp(popoverContent: PopoverContent, value: Side): Unit = {
      popoverContent.setSide(value)
    }

    def updateProp(popoverContent: PopoverContent, values: L.Source[Side]): Unit = {
      popoverContent.setSide(values)
    }

    type Selector = SideProp.type => Side

    lazy val top = SideProp(Side.Top)
    lazy val bottom = SideProp(Side.Bottom)
  }

  object Props {
    type Selector = Props.type => ComponentModifier[PopoverContent]

    lazy val side: SideProp.type = SideProp
  }

  // def apply(side: Side)(content: HtmlElement)(using root: PopoverRoot): Unit = {
  //   root.setContent(content)
  // }

  // def apply(side: Side.Selector)(content: HtmlElement)(using root: PopoverRoot): Unit = {
  //   root.setContent(content)
  // }

  def apply(mods: Props.Selector*)(content: HtmlElement)(using root: PopoverRoot): Unit = {
    val popoverContent: PopoverContent = new PopoverContent(content, root)
    val resolvedMods: Seq[ComponentModifier[PopoverContent]] = mods.map(_(Props))
    resolvedMods.foreach(_(popoverContent))

    root.setContent(popoverContent)
  }

  // def apply(mods: Props.Selector)(render: PopoverStore => HtmlElement)(using root: PopoverRoot): Unit = {
  //   root.setContent(render(root.store))
  // }
}
