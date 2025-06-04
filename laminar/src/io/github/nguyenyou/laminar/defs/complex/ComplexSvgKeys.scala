package io.github.nguyenyou.laminar.defs.complex

import io.github.nguyenyou.laminar.DomApi
import io.github.nguyenyou.laminar.codecs.StringAsIsCodec
import io.github.nguyenyou.laminar.defs.complex.ComplexSvgKeys._
import io.github.nguyenyou.laminar.keys.{CompositeKey, SvgAttr}
import io.github.nguyenyou.laminar.nodes.ReactiveSvgElement

trait ComplexSvgKeys {

  /**
   * This attribute is a list of the classes of the element.
   * Classes allow CSS and Javascript to select and access specific elements
   * via the class selectors or functions like the DOM method
   * document.getElementsByClassName
   */
  val className: CompositeSvgAttr = stringCompositeSvgAttr("class", separator = " ")

  val cls: CompositeSvgAttr = className

  lazy val role: CompositeSvgAttr = stringCompositeSvgAttr("role", separator = " ")

  // --

  protected def stringCompositeSvgAttr(name: String, separator: String): CompositeSvgAttr = {
    val attr = new SvgAttr(name, StringAsIsCodec, namespacePrefix = None)
    new CompositeKey(
      name = attr.name,
      getRawDomValue = el => DomApi.getSvgAttribute(el, attr).getOrElse(""),
      setRawDomValue = (el, value) => DomApi.setSvgAttribute(el, attr, value),
      separator = separator
    )
  }
}

object ComplexSvgKeys {

  type CompositeSvgAttr = CompositeKey[SvgAttr[String], ReactiveSvgElement.Base]
}
