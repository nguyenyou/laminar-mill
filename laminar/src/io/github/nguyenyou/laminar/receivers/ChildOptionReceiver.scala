package io.github.nguyenyou.laminar.receivers

import io.github.nguyenyou.airstream.core.Source
import io.github.nguyenyou.laminar.api.L.child
import io.github.nguyenyou.laminar.inserters.DynamicInserter
import io.github.nguyenyou.laminar.modifiers.RenderableNode
import io.github.nguyenyou.laminar.nodes.{ChildNode, CommentNode}

object ChildOptionReceiver {

  def <--(maybeChildSource: Source[Option[ChildNode.Base]]): DynamicInserter = {
    lazy val emptyNode = new CommentNode("")
    child <-- maybeChildSource.toObservable.map(_.getOrElse(emptyNode))
  }

  implicit class RichChildOptionReceiver(val self: ChildOptionReceiver.type) extends AnyVal {

    def <--[Component](
      maybeChildSource: Source[Option[Component]]
    )(implicit
      renderable: RenderableNode[Component]
    ): DynamicInserter = {
      lazy val emptyNode = new CommentNode("")
      child <-- {
        maybeChildSource
          .toObservable
          .map { maybeComponent =>
            renderable.asNode(maybeComponent, default = emptyNode)
          }
      }
    }
  }
}
