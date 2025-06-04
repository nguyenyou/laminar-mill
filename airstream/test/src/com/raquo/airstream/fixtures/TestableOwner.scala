package io.github.nguyenyou.airstream.fixtures

import io.github.nguyenyou.airstream.ownership.{Owner, Subscription}

class TestableOwner extends Owner {

  def _testSubscriptions: List[Subscription] = subscriptions.asScalaJs.toList

  override def killSubscriptions(): Unit = {
    super.killSubscriptions()
  }
}
