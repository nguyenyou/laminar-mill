package io.github.nguyenyou.laminar.primitives.base

trait ComponentModifier[Component] {
  def applyTo(component: Component): Unit
}
