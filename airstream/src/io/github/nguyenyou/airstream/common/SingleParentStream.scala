package io.github.nguyenyou.airstream.common

import io.github.nguyenyou.airstream.core.{InternalObserver, Observable, Protected, WritableStream}

/** A simple stream that only has one parent. */
trait SingleParentStream[I, O] extends WritableStream[O], InternalObserver[I] {

  protected val parent: Observable[I]

  override protected def onWillStart(): Unit = {
    // println(s"${this} > onWillStart")
    Protected.maybeWillStart(parent)
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
