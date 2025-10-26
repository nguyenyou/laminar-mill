package io.github.nguyenyou.laminar.primitives.base

import io.github.nguyenyou.laminar.nodes.ReactiveHtmlElement
import io.github.nguyenyou.laminar.modifiers.RenderableNode

import io.github.nguyenyou.laminar.api.L.*

trait Component {
  lazy val element = render()

  def setClassName(value: String) = {
    element.amend(
      cls := value
    )
  }

  def updateClassName(values: Source[String]) = {
    element.amend(
      cls <-- values.toObservable
    )
  }

  def render(): HtmlElement
}

object Component {
  implicit val renderable: RenderableNode[Component] = RenderableNode(_.element)

}
