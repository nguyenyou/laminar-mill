package io.github.nguyenyou.laminar.receivers

import io.github.nguyenyou.airstream.core.Source.EventSource
import io.github.nguyenyou.laminar.inserters.{ChildrenCommandInserter, CollectionCommand, DynamicInserter}
import io.github.nguyenyou.laminar.modifiers.RenderableNode

import scala.scalajs.js

object ChildrenCommandReceiver {

  def <--[Component](
    commands: EventSource[CollectionCommand[Component]]
  )(implicit
    renderableNode: RenderableNode[Component]
  ): DynamicInserter = {
    ChildrenCommandInserter(commands.toObservable, renderableNode, initialHooks = js.undefined)
  }
}
