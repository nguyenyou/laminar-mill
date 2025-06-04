package io.github.nguyenyou.airstream.combine

import io.github.nguyenyou.airstream.common.{InternalParentObserver, MultiParentSignal}
import io.github.nguyenyou.airstream.core.{Protected, Signal}
import com.raquo.ew.JsArray

import scala.util.Try

/**
  * @param parents Never update this array - this signal owns it.
  * @param combinator Must not throw! Must be pure.
  */
class CombineSignalN[A, Out](
  override protected val parents: JsArray[Signal[A]],
  protected val combinator: JsArray[A] => Out
) extends MultiParentSignal[A, Out] with CombineObservable[Out] {

  // @TODO[API] Maybe this should throw if parents.isEmpty

  override protected val topoRank: Int = Protected.maxTopoRank(0, parents) + 1

  override protected val parentObservers: JsArray[InternalParentObserver[?]] = {
    parents.map { parent =>
      InternalParentObserver.fromTry[A](parent, (_, trx) => {
        onInputsReady(trx)
      })
    }
  }

  override protected def inputsReady: Boolean = true

  override protected def combinedValue: Try[Out] = {
    CombineObservable.jsArrayCombinator(parents.map(_.tryNow()), combinator)
  }

  override protected def currentValueFromParent(): Try[Out] = combinedValue
}
