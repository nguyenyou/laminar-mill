package io.github.nguyenyou.airstream.state

import io.github.nguyenyou.airstream.core.Observer
import io.github.nguyenyou.airstream.misc.MapSignal
import io.github.nguyenyou.airstream.ownership.{Owner, Subscription}

class DerivedVarSignal[A, B](
  parent: Var[A],
  zoomIn: A => B,
  owner: Owner,
  parentDisplayName: => String
) extends MapSignal[A, B](
  parent.signal,
  project = zoomIn,
  recover = None
) with OwnedSignal[B] {

  // Note that even if owner kills subscription, this signal might remain due to other listeners
  override protected[state] def isStarted: Boolean = super.isStarted

  override protected val subscription: Subscription = this.addObserver(Observer.empty)(owner)

  override protected def defaultDisplayName: String = parentDisplayName + ".signal"
}
