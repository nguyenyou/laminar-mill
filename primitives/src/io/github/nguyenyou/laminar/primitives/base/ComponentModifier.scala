package io.github.nguyenyou.laminar.primitives.base

trait ComponentModifier[A] {
  def applyTo(component: A): Unit
}
