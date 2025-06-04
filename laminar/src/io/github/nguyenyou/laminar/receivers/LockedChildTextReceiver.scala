package io.github.nguyenyou.laminar.receivers

import io.github.nguyenyou.airstream.core.Source
import io.github.nguyenyou.laminar.api.L.child
import io.github.nguyenyou.laminar.inserters.DynamicInserter
import io.github.nguyenyou.laminar.nodes.{ChildNode, CommentNode, TextNode}

class LockedChildTextReceiver(
  val text: String
) {

  /** If `include` is true, the text will be added. Otherwise, an empty node will be added. */
  @inline def apply(include: Boolean): ChildNode.Base = {
    this := include
  }

  /** If `include` is true, the text will be added. Otherwise, an empty node will be added. */
  def :=(include: Boolean): ChildNode.Base = {
    if (include) new TextNode(text) else new CommentNode("")
  }

  /** If `includeSource` emits true, text will be added. Otherwise, it will be removed. */
  def <--(includeSource: Source[Boolean]): DynamicInserter = {
    child.text <-- includeSource.toObservable.map(if (_) text else "")
  }
}
