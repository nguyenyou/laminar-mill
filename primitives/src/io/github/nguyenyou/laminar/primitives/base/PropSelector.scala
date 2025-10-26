package io.github.nguyenyou.laminar.primitives.base

trait PropSelector[A] {
  type Self = this.type
  type Selector = Self => ComponentModifier[A]
}
