package io.github.nguyenyou.laminar.defs.styles.units

import io.github.nguyenyou.laminar.keys.DerivedStyleBuilder

trait Calc[DSP[_]] { this: DerivedStyleBuilder[?, DSP] =>

  /** Wrap the provided expression in CSS calc() function.
   *
   * @see https://developer.mozilla.org/en-US/docs/Web/CSS/calc()
   */
  lazy val calc: DSP[String] = derivedStyle(exp => s"calc(${encodeCalcValue(exp)})")
}
