package io.github.nguyenyou.airstream.common

import io.github.nguyenyou.airstream.core.{Observable, Protected, Signal, Transaction, WritableSignal}

import scala.util.Try

/** A simple stream that only has one parent. */
trait SingleParentSignal[I, O] extends WritableSignal[O] with InternalTryObserver[I] {

  protected val parent: Observable[I]

  protected val parentIsSignal: Boolean = parent.isInstanceOf[Signal[?]]

  // Note: `-1` here means that we've never synced up with the parent.
  //       I am not sure if -1 vs 0 has a practical difference, but looking
  //       at our onWillStart code, it seems that using -1 here would be more
  //       prudent. If using 0, the initial onWillStart may not detect the
  //       "change" (from no value to parent signal's initial value), and the
  //       signal's value would only be updated in tryNow().
  protected var _parentLastUpdateId: Int = -1

  /** Note: this is overriden in:
    *  - [[io.github.nguyenyou.airstream.misc.SignalFromStream]] because parent can be stream, and it has cacheInitialValue logic
    *  - [[io.github.nguyenyou.airstream.split.SplitChildSignal]] because its parent is a special timing stream, not the real parent
    */
  override protected def onWillStart(): Unit = {
    // dom.console.log(s"${this} > onWillStart (SPS)")
    Protected.maybeWillStart(parent)
    if (parentIsSignal) {
      val newParentLastUpdateId = Protected.lastUpdateId(parent.asInstanceOf[Signal[?]])
      if (newParentLastUpdateId != _parentLastUpdateId) {
        updateCurrentValueFromParent(
          currentValueFromParent(),
          newParentLastUpdateId
        )
      }
    }
  }

  /** Note: this is overridden in:
   *  - [[io.github.nguyenyou.airstream.split.SplitChildSignal]] to clear cached initial value (if any)
   *  - [[io.github.nguyenyou.airstream.distinct.DistinctSignal]] to filter out isSame events
   */
  protected def updateCurrentValueFromParent(
    nextValue: Try[O],
    nextParentLastUpdateId: Int
  ): Unit = {
    // dom.console.log(s"${this} > updateCurrentValueFromParent")
    setCurrentValue(nextValue)
    _parentLastUpdateId = nextParentLastUpdateId
  }

  /** Note: this is overridden in:
    *  - [[io.github.nguyenyou.airstream.split.SplitChildSignal]] because its parent is a special timing stream, not the real parent
    */
  override protected def onTry(nextParentValue: Try[I], transaction: Transaction): Unit = {
    if (parentIsSignal) {
      _parentLastUpdateId = Protected.lastUpdateId(parent.asInstanceOf[Signal[?]])
    }
  }

  override protected def onStart(): Unit = {
    // println(s"${this} > onStart")
    parent.addInternalObserver(this, shouldCallMaybeWillStart = false)
    super.onStart()
  }

  override protected def onStop(): Unit = {
    parent.removeInternalObserver(observer = this)
    super.onStop()
  }
}
