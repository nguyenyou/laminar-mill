package io.github.nguyenyou.airstream.state

import io.github.nguyenyou.airstream.ownership.Subscription

// @TODO[API] Should we expose `killOriginalSubscription` to end users?
trait OwnedSignal[+A] extends StrictSignal[A] {

  protected val subscription: Subscription

  /** This only kills the subscription, but this signal might also have other listeners */
  def killOriginalSubscription(): Unit = subscription.kill()
}
