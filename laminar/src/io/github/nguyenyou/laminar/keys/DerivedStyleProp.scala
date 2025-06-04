package io.github.nguyenyou.laminar.keys

import io.github.nguyenyou.airstream.core.Source
import io.github.nguyenyou.laminar.DomApi
import io.github.nguyenyou.laminar.api.L.{optionToSetter, HtmlElement}
import io.github.nguyenyou.laminar.modifiers.{KeySetter, KeyUpdater, Setter}
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter
import io.github.nguyenyou.laminar.modifiers.KeyUpdater.DerivedStyleUpdater
import io.github.nguyenyou.laminar.nodes.ReactiveHtmlElement

/** This class represents derived style props like `height.px` or `backgroundImage.url` */
class DerivedStyleProp[InputV](
  val key: StyleProp[?],
  val encode: InputV => String
) {

  @inline def apply(value: InputV): StyleSetter = {
    this := value
  }

  def :=(value: InputV): StyleSetter = {
    new KeySetter[StyleProp[?], String, HtmlElement](
      key,
      encode(value),
      DomApi.setHtmlStringStyle
    )
  }

  def maybe(value: Option[InputV]): Setter[HtmlElement] = {
    optionToSetter(value.map(v => this := v))
  }

  def <--(values: Source[InputV]): DerivedStyleUpdater[InputV] = {
    new KeyUpdater[ReactiveHtmlElement.Base, StyleProp[?], InputV](
      key = key,
      values = values.toObservable,
      update = (el, v, _) => DomApi.setHtmlStringStyle(el, key, encode(v))
    )
  }
}
