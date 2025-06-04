package io.github.nguyenyou.airstream.fixtures

import io.github.nguyenyou.airstream.ownership.{Owner, Subscription}

class TestableSubscription(owner: Owner) {

  var killCount = 0

  val subscription = new Subscription(owner, cleanup = () => {
    killCount += 1
  })
}
