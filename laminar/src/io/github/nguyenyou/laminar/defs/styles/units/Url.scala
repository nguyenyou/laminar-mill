package io.github.nguyenyou.laminar.defs.styles.units

import io.github.nguyenyou.laminar.keys.DerivedStyleBuilder

/** @see https://developer.mozilla.org/en-US/docs/Web/CSS/url */
trait Url[DSP[_]] { this: DerivedStyleBuilder[?, DSP] =>

  /** Provide a URL to wrap into the CSS `url()` function. */
  lazy val url: DSP[String] = derivedStyle { s =>
    s"""url(${encodeUrlValue(s)})"""
  }
}
