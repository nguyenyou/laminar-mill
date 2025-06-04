package io.github.nguyenyou.laminar.nodes

import io.github.nguyenyou.laminar.keys.Key
import io.github.nguyenyou.laminar.tags.SvgTag
import org.scalajs.dom

class ReactiveSvgElement[+Ref <: dom.svg.Element](
  override val tag: SvgTag[Ref],
  final override val ref: Ref
) extends ReactiveElement[Ref] {

  override private[laminar] def onBoundKeyUpdater(key: Key): Unit = ()

  override def toString: String = {
    // `ref` is not available inside ReactiveElement's constructor due to initialization order, so fall back to `tag`.
    s"ReactiveSvgElement(${if (ref != null) ref.outerHTML else s"tag=${tag.name}"})"
  }
}

object ReactiveSvgElement {

  type Base = ReactiveSvgElement[dom.svg.Element]
}
