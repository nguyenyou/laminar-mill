package io.github.nguyenyou.airstream.distinct

import io.github.nguyenyou.airstream.common.{InternalTryObserver, SingleParentStream}
import io.github.nguyenyou.airstream.core.{EventStream, Protected, Transaction}

import scala.scalajs.js
import scala.util.Try

/** Emits only values that are distinct from the last emitted value, according to isSame function */
class DistinctStream[A](
  override protected val parent: EventStream[A],
  isSame: (Try[A], Try[A]) => Boolean,
  resetOnStop: Boolean
) extends SingleParentStream[A, A] with InternalTryObserver[A] {

  override protected val topoRank: Int = Protected.topoRank(parent) + 1

  private var maybeLastSeenValue: js.UndefOr[Try[A]] = js.undefined

  override protected def onTry(nextValue: Try[A], transaction: Transaction): Unit = {
    val isDistinct = maybeLastSeenValue.map(!isSame(_, nextValue)).getOrElse(true)
    maybeLastSeenValue = nextValue
    if (isDistinct) {
      fireTry(nextValue, transaction)
    }
  }

  override protected def onStop(): Unit = {
    if (resetOnStop) {
      maybeLastSeenValue = js.undefined
    }
    super.onStop()
  }
}
