package io.github.nguyenyou.airstream.misc

import io.github.nguyenyou.airstream.common.{InternalTryObserver, SingleParentStream}
import io.github.nguyenyou.airstream.core.{Protected, Signal, Transaction}

import scala.util.Try

class StreamFromSignal[A](
  override protected val parent: Signal[A],
  changesOnly: Boolean
) extends SingleParentStream[A, A] with InternalTryObserver[A] {

  override protected val topoRank: Int = Protected.topoRank(parent) + 1

  private var lastSeenParentUpdateId: Int = 0

  private var isFirstPull: Boolean = true

  override protected def onStart(): Unit = {
    val newParentLastUpdateId = Protected.lastUpdateId(parent)
    if (isFirstPull && changesOnly) {
      lastSeenParentUpdateId = newParentLastUpdateId
    } else {
      if (newParentLastUpdateId != lastSeenParentUpdateId) {
        // #TODO[Integrity] In this branch, should lastSeenParentUpdateId be updated immediately,
        //  or once the transaction executes? If the latter – suppose the transaction doesn't execute,
        //  because this stream was stopped for whatever weird reason (is that even possible?).
        //  This would mean that on next onStart, this stream would treat the same
        //  `newParentLastUpdateId` value as unseen, and try to pull it again. Seems reasonable?
        Transaction.onStart.add { trx =>
          if (isStarted) {
            // #TODO[Integrity] Do we need to check `newParentLastUpdateId != lastSeenParentUpdateId` again here?
            // We fetch new parent value and new corresponding lastUpdateId, just in case,
            // to make sure that we're getting the latest value.
            fireTry(parent.tryNow(), trx)
            lastSeenParentUpdateId = Protected.lastUpdateId(parent)
          }
        }
      }
    }
    isFirstPull = false

    super.onStart()
  }

  override protected def onTry(nextValue: Try[A], transaction: Transaction): Unit = {
    fireTry(nextValue, transaction)
    lastSeenParentUpdateId = Protected.lastUpdateId(parent)
    isFirstPull = false
  }

}
