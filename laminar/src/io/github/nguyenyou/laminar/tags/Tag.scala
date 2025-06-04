package io.github.nguyenyou.laminar.tags

import io.github.nguyenyou.laminar.nodes.ReactiveElement

trait Tag[+El <: ReactiveElement.Base] {

  val name: String

  val void: Boolean

  /** The string returned by `element.tagName` in JS DOM.
    * It's uppercase for HTML elements, and case-preserving for SVG elements.
    */
  def jsTagName: String
}

object Tag {

  type Base = Tag[ReactiveElement.Base]
}
