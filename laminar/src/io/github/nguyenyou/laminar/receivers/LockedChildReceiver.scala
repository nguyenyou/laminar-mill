package io.github.nguyenyou.laminar.receivers

import io.github.nguyenyou.airstream.core.Source
import io.github.nguyenyou.laminar.api.L.child
import io.github.nguyenyou.laminar.inserters.DynamicInserter
import io.github.nguyenyou.laminar.nodes.{ChildNode, CommentNode}

class LockedChildReceiver(
  node: ChildNode.Base
) {

  /** If `include` is true, the node will be added. Otherwise, an empty node will be added. */
  @inline def apply(include: Boolean): ChildNode.Base = {
    this := include
  }

  /** If `include` is true, the node will be added. Otherwise, an empty node will be added. */
  def :=(include: Boolean): ChildNode.Base = {
    if (include) node else new CommentNode("")
  }

  /** If `includeSource` emits true, node will be added. Otherwise, it will be removed. */
  def <--(includeSource: Source[Boolean]): DynamicInserter = {
    lazy val emptyNode = new CommentNode("")
    child <-- includeSource.toObservable.map(if (_) node else emptyNode)
  }

}
