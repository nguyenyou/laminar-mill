package io.github.nguyenyou.laminar.receivers

import io.github.nguyenyou.airstream.core.Source
import io.github.nguyenyou.laminar.inserters.{ChildInserter, DynamicInserter}
import io.github.nguyenyou.laminar.modifiers.RenderableNode
import io.github.nguyenyou.laminar.nodes.ChildNode

import scala.scalajs.js

object ChildReceiver {

  val maybe: ChildOptionReceiver.type = ChildOptionReceiver

  val text: ChildTextReceiver.type = ChildTextReceiver

  /** Example usages:
    *     child(element) <-- signalOfBoolean
    *     child(component) <-- signalOfBoolean
    */
  def apply(node: ChildNode.Base): LockedChildReceiver = {
    new LockedChildReceiver(node)
  }

  def <--(childSource: Source[ChildNode.Base]): DynamicInserter = {
    ChildInserter(childSource.toObservable, RenderableNode.nodeRenderable, initialHooks = js.undefined)
  }

  implicit class RichChildReceiver(val self: ChildReceiver.type) extends AnyVal {

    def <--[Component](
      childSource: Source[Component]
    )(implicit
      renderable: RenderableNode[Component]
    ): DynamicInserter = {
      ChildInserter(childSource.toObservable, renderable, initialHooks = js.undefined)
    }

    // #TODO[Scala,Ergonomics] If user provides Source[A] for which
    //  RenderableNode[A] does not exist, we don't get the nice
    //  @implicitNotFound message, the compiler only says that
    //  A is not ChildNode.Base. Not sure how to improve the situation.

  }

}
