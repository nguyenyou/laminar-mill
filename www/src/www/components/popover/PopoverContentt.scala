package www.components.popover

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.api.L
import www.components.popover.PopoverRoot
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import org.scalajs.dom

class PopoverContent(val content: HtmlElement, val root: PopoverRoot) {
  private var initialized = false

  content.amend(
    dataAttr("slot") := "popover-content"
  )

  val portal = div(
    dataAttr("slot") := "popover-content-portal",
    position.fixed,
    left.px(0),
    bottom.px(0),
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

  def setSide(side: PopoverContent.Side) = {
    // Apply the side value to the button
  }

  def setSide(side: L.Source[PopoverContent.Side]) = {
    // Apply the side source to the button
  }

}

object PopoverContent {
  sealed trait PopoverContentModifier {
    def applyTo(popoverContent: PopoverContent): Unit
  }

  def apply(content: HtmlElement)(using root: PopoverRoot): Unit = {
    val popoverContent: PopoverContent = new PopoverContent(content, root)
    root.setContent(popoverContent)
  }

  // Concrete modifier types with built-in application logic
  final class PropSetter[V](val prop: Prop[V], val initialValue: V) extends PopoverContentModifier {
    def applyTo(popoverContent: PopoverContent): Unit = prop.applyValue(popoverContent, initialValue)
  }

  final class PropUpdater[V](val prop: Prop[V], val source: L.Source[V]) extends PopoverContentModifier {
    def applyTo(popoverContent: PopoverContent): Unit = prop.applySource(popoverContent, source)
  }

  // Abstract Prop class with type-safe application methods
  abstract class Prop[V](val name: String) {
    // Abstract methods that subclasses must implement for type-safe application
    def applyValue(popoverContent: PopoverContent, value: V): Unit
    def applySource(popoverContent: PopoverContent, source: L.Source[V]): Unit

    inline def apply(value: V): PropSetter[V] = this := value

    def :=(value: V): PropSetter[V] = PropSetter(this, value)

    def <--(source: L.Source[V]): PropUpdater[V] = PropUpdater(this, source)
  }

  enum Side {
    case Top, Bottom, Left, Right
  }

  object Side extends Prop[Side]("side") {
    def applyValue(popoverContent: PopoverContent, value: Side): Unit = {
      // Apply the side value to the button
    }

    def applySource(popoverContent: PopoverContent, source: L.Source[Side]): Unit = {
      // Apply the side source to the button
    }
    type Selector = Side.type => Side

    lazy val top = Side(Side.Top)
    lazy val bottom = Side(Side.Bottom)
    lazy val left = Side(Side.Left)
    lazy val right = Side(Side.Right)
  }

  object Props {
    type Selector = Props.type => PopoverContentModifier
  }

  // def apply(side: Side)(content: HtmlElement)(using root: PopoverRoot): Unit = {
  //   root.setContent(content)
  // }

  // def apply(side: Side.Selector)(content: HtmlElement)(using root: PopoverRoot): Unit = {
  //   root.setContent(content)
  // }

  def apply(mods: Props.Selector*)(content: HtmlElement)(using root: PopoverRoot): Unit = {
    val popoverContent: PopoverContent = new PopoverContent(content, root)
    val resolvedMods: Seq[PopoverContentModifier] = mods.map(_(Props))
    resolvedMods.foreach(_.applyTo(popoverContent))

    root.setContent(popoverContent)
  }

  // def apply(mods: Props.Selector)(render: PopoverStore => HtmlElement)(using root: PopoverRoot): Unit = {
  //   root.setContent(render(root.store))
  // }
}
