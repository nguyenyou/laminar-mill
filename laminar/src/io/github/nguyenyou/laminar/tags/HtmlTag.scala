package io.github.nguyenyou.laminar.tags

import io.github.nguyenyou.laminar.DomApi
import io.github.nguyenyou.laminar.modifiers.Modifier
import io.github.nguyenyou.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom

class HtmlTag[+Ref <: dom.html.Element](
  override val name: String,
  override val void: Boolean = false
) extends Tag[ReactiveHtmlElement[Ref]] {

  def apply(modifiers: Modifier[ReactiveHtmlElement[Ref]]*): ReactiveHtmlElement[Ref] = {
    val element = build()
    modifiers.foreach(modifier => modifier(element))
    element
  }

  override def jsTagName: String = name.toUpperCase

  /** Create a Scala DOM Builder element from this Tag */
  protected def build(): ReactiveHtmlElement[Ref] = new ReactiveHtmlElement(this, DomApi.createHtmlElement(this))
}
