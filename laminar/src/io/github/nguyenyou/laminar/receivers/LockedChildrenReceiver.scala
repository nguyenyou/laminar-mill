package io.github.nguyenyou.laminar.receivers

import io.github.nguyenyou.airstream.core.Source
import io.github.nguyenyou.laminar
import io.github.nguyenyou.laminar.api.L.children
import io.github.nguyenyou.laminar.inserters.DynamicInserter
import io.github.nguyenyou.laminar.nodes.ChildNode

class LockedChildrenReceiver(
  val nodes: laminar.Seq[ChildNode.Base]
) {

  /** If `include` is true, the nodes will be added. */
  @inline def apply(include: Boolean): laminar.Seq[ChildNode.Base] = {
    this := include
  }

  /** If `include` is true, the nodes will be added. */
  def :=(include: Boolean): laminar.Seq[ChildNode.Base] = {
    if (include) nodes else laminar.Seq.empty
  }

  /** If `includeSource` emits true, node will be added. Otherwise, it will be removed. */
  def <--(includeSource: Source[Boolean]): DynamicInserter = {
    children <-- includeSource.toObservable.map(if (_) nodes else laminar.Seq.empty)
  }

}
