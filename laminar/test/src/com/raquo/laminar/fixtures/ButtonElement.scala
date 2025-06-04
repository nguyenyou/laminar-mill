package io.github.nguyenyou.laminar.fixtures

import io.github.nguyenyou.laminar.nodes.Slot

object ButtonElement extends WebComponent("sl-button") {

  object slots {

    val prefix = new Slot("prefix")
  }
}
