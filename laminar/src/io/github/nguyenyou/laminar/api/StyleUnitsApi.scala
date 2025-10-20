package io.github.nguyenyou.laminar.api

import io.github.nguyenyou.laminar.api.StyleUnitsApi.StyleEncoder
import io.github.nguyenyou.laminar.defs.styles.units
import io.github.nguyenyou.laminar.keys.DerivedStyleBuilder

trait StyleUnitsApi
    extends DerivedStyleBuilder[String, StyleEncoder],
      units.Color[String, StyleEncoder],
      units.Length[StyleEncoder, Int],
      units.Time[StyleEncoder],
      units.Url[StyleEncoder] {

  override protected def styleSetter(value: String): String = value

  override protected def derivedStyle[A](encode: A => String): StyleEncoder[A] = new StyleEncoder[A] {
    override def apply(v: A): String = encode(v)
  }
}

object StyleUnitsApi {

  /** This marker trait is used for implicit conversions. For all intents and purposes it's just a function. */
  trait StyleEncoder[A] extends Function1[A, String]
}
