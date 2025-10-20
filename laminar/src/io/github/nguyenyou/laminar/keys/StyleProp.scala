package io.github.nguyenyou.laminar.keys

import io.github.nguyenyou.airstream.core.Source
import io.github.nguyenyou.laminar.DomApi
import io.github.nguyenyou.laminar.api.L.{optionToSetter, HtmlElement}
import io.github.nguyenyou.laminar.defs.styles.traits.GlobalKeywords
import io.github.nguyenyou.laminar.modifiers.{KeySetter, KeyUpdater, Setter}
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter
import io.github.nguyenyou.laminar.modifiers.KeyUpdater.StyleUpdater
import io.github.nguyenyou.laminar.nodes.ReactiveHtmlElement

import scala.scalajs.js.|

class StyleProp[V](
  override val name: String,
  val prefixes: Seq[String] = Nil
) extends Key,
      GlobalKeywords,
      DerivedStyleBuilder[StyleSetter, DerivedStyleProp] {

  def :=(value: V | String): StyleSetter = {
    new KeySetter[StyleProp[?], String, HtmlElement](this, value.toString, DomApi.setHtmlStringStyle)
  }

  @inline def apply(value: V | String): StyleSetter = {
    this := value
  }

  def maybe(value: Option[V | String]): Setter[HtmlElement] = {
    optionToSetter(value.map(v => this := v))
  }

  def <--[A](values: Source[A])(implicit ev: A => V | String): StyleUpdater[V] = {
    new KeyUpdater[ReactiveHtmlElement.Base, StyleProp[V], V | String](
      key = this,
      values = values.asInstanceOf[Source[V | String]].toObservable,
      update = (el, v, _) => DomApi.setHtmlAnyStyle(el, this, v)
    )
  }

  /** Create a new key for this style with these prefixes */
  def withPrefixes(ps: (StyleVendorPrefixes.type => String)*): StyleProp[V] = {
    new StyleProp[V](name, ps.map(_(StyleVendorPrefixes)))
  }

  override protected def styleSetter(value: String): StyleSetter = {
    this := value
  }

  // override protected def valueAsString(value: StyleSetter[V]): String = {
  //   value.value
  // }

  override protected def derivedStyle[InputV](encode: InputV => String): DerivedStyleProp[InputV] = {
    new DerivedStyleProp[InputV](this, encode)
  }
}
