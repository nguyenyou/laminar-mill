package io.github.nguyenyou.laminar.modifiers

import io.github.nguyenyou.airstream.core.Observable
import io.github.nguyenyou.airstream.ownership.DynamicSubscription
import io.github.nguyenyou.laminar.keys.{AriaAttr, HtmlAttr, HtmlProp, Key, StyleProp, SvgAttr}
import io.github.nguyenyou.laminar.nodes.{ReactiveElement, ReactiveHtmlElement, ReactiveSvgElement}

import scala.scalajs.js.|

/**
 * A modifier that updates a key from a source, e.g. `value <-- valueStream`
 *
 * @param update (element, newValue, reason) => ()
 *               The reason is used for updating CompositeKey-s.
 */
class KeyUpdater[-El <: ReactiveElement.Base, +K <: Key, V](
  val key: K,
  val values: Observable[V],
  val update: (El, V, Modifier.Any) => Unit
) extends Binder[El] { self =>

  override def bind(element: El): DynamicSubscription = {
    element.onBoundKeyUpdater(key)
    ReactiveElement.bindFn(element, values) { value =>
      update(element, value, self)
    }
  }
}

object KeyUpdater {

  type PropUpdater[V, DomV] = KeyUpdater[ReactiveHtmlElement.Base, HtmlProp[V, DomV], V]

  type HtmlAttrUpdater[V] = KeyUpdater[ReactiveHtmlElement.Base, HtmlAttr[V], V]

  type SvgAttrUpdater[V] = KeyUpdater[ReactiveSvgElement.Base, SvgAttr[V], V]

  type AriaAttrUpdater[V] = KeyUpdater[ReactiveElement.Base, AriaAttr[V], V]

  type StyleUpdater[V] = KeyUpdater[ReactiveHtmlElement.Base, StyleProp[V], V | String]

  type DerivedStyleUpdater[InputV] = KeyUpdater[ReactiveHtmlElement.Base, StyleProp[?], InputV]
}
