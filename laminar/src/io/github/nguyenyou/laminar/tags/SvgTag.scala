package io.github.nguyenyou.laminar.tags

import io.github.nguyenyou.laminar.DomApi
import io.github.nguyenyou.laminar.modifiers.Modifier
import io.github.nguyenyou.laminar.nodes.ReactiveSvgElement
import org.scalajs.dom

class SvgTag[+Ref <: dom.svg.Element](
  override val name: String,
  override val void: Boolean = false
) extends Tag[ReactiveSvgElement[Ref]] {

  def apply(modifiers: Modifier[ReactiveSvgElement[Ref]]*): ReactiveSvgElement[Ref] = {
    val element = build()
    modifiers.foreach(modifier => modifier(element))
    element
  }

  override def jsTagName: String = name

  /** Create a Scala DOM Builder element from this Tag */
  def build(): ReactiveSvgElement[Ref] = new ReactiveSvgElement(this, DomApi.createSvgElement(this))
}
