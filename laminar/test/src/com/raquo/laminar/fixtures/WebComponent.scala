package io.github.nguyenyou.laminar.fixtures

import io.github.nguyenyou.laminar.keys.EventProcessor
import io.github.nguyenyou.laminar.modifiers.Modifier
import io.github.nguyenyou.laminar.nodes.ReactiveHtmlElement
import io.github.nguyenyou.laminar.tags.CustomHtmlTag
import org.scalajs.dom

abstract class WebComponent(tagName: String) {

  type Ref <: dom.HTMLElement

  type Element = ReactiveHtmlElement[Ref]

  type ModFunction = this.type => Modifier[Element]

  protected lazy val tag: CustomHtmlTag[Ref] = new CustomHtmlTag(tagName)

  def on[Ev <: dom.Event, V](ev: EventProcessor[Ev, V]): EventProcessor[Ev, V] = ev

  def of(mods: ModFunction*): Element = {
    val el = tag()
    mods.foreach(_(this)(el))
    el
  }
}
