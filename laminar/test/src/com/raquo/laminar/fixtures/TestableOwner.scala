package io.github.nguyenyou.laminar.fixtures

import io.github.nguyenyou.airstream.ownership.{Owner, Subscription}
import io.github.nguyenyou.ew.JsArray

// @TODO[Elegance] This duplicates a fixture defined in Airstream
class TestableOwner extends Owner {

  def _testSubscriptions: JsArray[Subscription] = subscriptions

  override def killSubscriptions(): Unit = {
    super.killSubscriptions()
  }
}
