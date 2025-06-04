package io.github.nguyenyou.laminar.keys

import io.github.nguyenyou.airstream.core.Source
import io.github.nguyenyou.laminar.DomApi
import io.github.nguyenyou.laminar.api.L.{optionToSetter, HtmlElement}
import io.github.nguyenyou.laminar.codecs.Codec
import io.github.nguyenyou.laminar.modifiers.{KeySetter, KeyUpdater, Setter}
import io.github.nguyenyou.laminar.modifiers.KeySetter.HtmlAttrSetter
import io.github.nguyenyou.laminar.modifiers.KeyUpdater.HtmlAttrUpdater

/**
  * This class represents an HTML Element Attribute. Meaning the key that can be set, not the whole a key-value pair.
  *
  * @tparam V type of values that this Attribute can be set to
  */
class HtmlAttr[V](
  override val name: String,
  val codec: Codec[V, String]
) extends Key {

  @inline def apply(value: V): HtmlAttrSetter[V] = {
    this := value
  }

  def maybe(value: Option[V]): Setter[HtmlElement] = {
    optionToSetter(value.map(v => this := v))
  }

  def :=(value: V): HtmlAttrSetter[V] = {
    new KeySetter[HtmlAttr[V], V, HtmlElement](this, value, DomApi.setHtmlAttribute)
  }

  def <--(values: Source[V]): HtmlAttrUpdater[V] = {
    new KeyUpdater[HtmlElement, HtmlAttr[V], V](
      key = this,
      values = values.toObservable,
      update = (el, v, _) => DomApi.setHtmlAttribute(el, this, v)
    )
  }

}
