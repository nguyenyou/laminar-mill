package io.github.nguyenyou.laminar.keys

import io.github.nguyenyou.airstream.core.Source
import io.github.nguyenyou.laminar.DomApi
import io.github.nguyenyou.laminar.api.L.{optionToSetter, Element}
import io.github.nguyenyou.laminar.codecs.Codec
import io.github.nguyenyou.laminar.modifiers.{KeySetter, KeyUpdater, Setter}
import io.github.nguyenyou.laminar.modifiers.KeySetter.AriaAttrSetter
import io.github.nguyenyou.laminar.modifiers.KeyUpdater.AriaAttrUpdater

/**
 * This class represents an HTML Element Attribute. Meaning the key that can be set, not the whole a key-value pair.
 *
 * @tparam V type of values that this Attribute can be set to
 */
class AriaAttr[V](
  suffix: String,
  val codec: Codec[V, String]
) extends Key {

  override val name: String = "aria-" + suffix

  def :=(value: V): AriaAttrSetter[V] = {
    new KeySetter[AriaAttr[V], V, Element](this, value, DomApi.setAriaAttribute)
  }

  @inline def apply(value: V): AriaAttrSetter[V] = {
    this := value
  }

  def maybe(value: Option[V]): Setter[Element] = {
    optionToSetter(value.map(v => this := v))
  }

  def <--(values: Source[V]): AriaAttrUpdater[V] = {
    new KeyUpdater[Element, AriaAttr[V], V](
      key = this,
      values = values.toObservable,
      update = (el, v, _) => DomApi.setAriaAttribute(el, this, v)
    )
  }

}
