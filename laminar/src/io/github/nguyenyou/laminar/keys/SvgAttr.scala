package io.github.nguyenyou.laminar.keys

import io.github.nguyenyou.airstream.core.Source
import io.github.nguyenyou.laminar.DomApi
import io.github.nguyenyou.laminar.api.L.{optionToSetter, SvgElement}
import io.github.nguyenyou.laminar.codecs.Codec
import io.github.nguyenyou.laminar.modifiers.{KeySetter, KeyUpdater, Setter}
import io.github.nguyenyou.laminar.modifiers.KeySetter.SvgAttrSetter
import io.github.nguyenyou.laminar.modifiers.KeyUpdater.SvgAttrUpdater

/**
  * This class represents an Svg Element Attribute. Meaning the key that can be set, not the whole a key-value pair.
  *
  * @param localName - Unqualified name, without the namespace prefix
  * @param namespacePrefix - e.g. "xlink". For namespace URI, see `namespaceUri`
  *
  * @tparam V type of values that this Attribute can be set to
  */
class SvgAttr[V](
  val localName: String,
  val codec: Codec[V, String],
  val namespacePrefix: Option[String]
) extends Key {

  /** Qualified name, including namespace */
  override val name: String = namespacePrefix.map(_ + ":" + localName).getOrElse(localName)

  val namespaceUri: Option[String] = namespacePrefix.map(SvgAttr.namespaceUri)

  def :=(value: V): SvgAttrSetter[V] = {
    new KeySetter[SvgAttr[V], V, SvgElement](this, value, DomApi.setSvgAttribute)
  }

  @inline def apply(value: V): SvgAttrSetter[V] = {
    this := value
  }

  def maybe(value: Option[V]): Setter[SvgElement] = {
    optionToSetter(value.map(v => this := v))
  }

  def <--(values: Source[V]): SvgAttrUpdater[V] = {
    new KeyUpdater[SvgElement, SvgAttr[V], V](
      key = this,
      values = values.toObservable,
      update = (el, v, _) => DomApi.setSvgAttribute(el, this, v)
    )
  }

}

object SvgAttr {

  // For SVG namespaces info see https://developer.mozilla.org/en-US/docs/Web/SVG/Namespaces_Crash_Course

  final def namespaceUri(namespace: String): String = {
    namespace match {
      case "svg" => svgNamespaceUri
      case "xlink" => xlinkNamespaceUri
      case "xml" => xmlNamespaceUri
      case "xmlns" => xmlnsNamespaceUri
    }
  }

  final val svgNamespaceUri: String = "http://www.w3.org/2000/svg"

  final val xlinkNamespaceUri: String = "http://www.w3.org/1999/xlink"

  final val xmlNamespaceUri: String = "http://www.w3.org/XML/1998/namespace"

  final val xmlnsNamespaceUri: String = "http://www.w3.org/2000/xmlns/"
}
