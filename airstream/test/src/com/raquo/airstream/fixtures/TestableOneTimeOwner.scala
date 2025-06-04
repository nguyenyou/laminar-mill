package io.github.nguyenyou.airstream.fixtures

import io.github.nguyenyou.airstream.ownership.{OneTimeOwner, Subscription}

class TestableOneTimeOwner(onAccessAfterKilled: () => Unit) extends OneTimeOwner(onAccessAfterKilled) {

  def _testSubscriptions: List[Subscription] = subscriptions.asScalaJs.toList

  override def killSubscriptions(): Unit = super.killSubscriptions()
}
