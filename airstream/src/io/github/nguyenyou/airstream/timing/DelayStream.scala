package io.github.nguyenyou.airstream.timing

import io.github.nguyenyou.airstream.common.{InternalNextErrorObserver, SingleParentStream}
import io.github.nguyenyou.airstream.core.{EventStream, Transaction}
import io.github.nguyenyou.ew.JsArray

import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle

class DelayStream[A](
  override protected val parent: EventStream[A],
  delayMs: Int
) extends SingleParentStream[A, A] with InternalNextErrorObserver[A] {

  /** Async stream, so reset rank */
  override protected val topoRank: Int = 1

  private val timerHandles: JsArray[SetTimeoutHandle] = JsArray()

  override protected def onNext(nextValue: A, transaction: Transaction): Unit = {
    var timerHandle: SetTimeoutHandle = null
    timerHandle = js.timers.setTimeout(delayMs.toDouble) {
      // println(s"> init trx from DelayEventStream.onNext($nextValue)")
      timerHandles.splice(timerHandles.indexOf(timerHandle), deleteCount = 1) // Remove handle
      Transaction(fireValue(nextValue, _))
      ()
    }
    timerHandles.push(timerHandle)
  }

  override def onError(nextError: Throwable, transaction: Transaction): Unit = {
    var timerHandle: SetTimeoutHandle = null
    timerHandle = js.timers.setTimeout(delayMs.toDouble) {
      timerHandles.splice(timerHandles.indexOf(timerHandle), deleteCount = 1) // Remove handle
      Transaction(fireError(nextError, _))
      ()
    }
    timerHandles.push(timerHandle)
  }

  override protected def onStop(): Unit = {
    timerHandles.forEach(js.timers.clearTimeout(_))
    timerHandles.length = 0 // Clear array
    super.onStop()
  }
}
