package io.github.nguyenyou.laminar.primitives.base

trait ComponentModifier[Component] {
  def apply(component: Component): Unit
}
