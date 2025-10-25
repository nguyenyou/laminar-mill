package io.github.nguyenyou.laminar.primitives.base

import io.github.nguyenyou.laminar.api.L

final class ComponentPropModifier[V, A](val prop: ComponentProp[V, A], val signal: L.SignalSource[V]) extends ComponentModifier[A] {
  def applyTo(component: A): Unit = prop.applySignal(component, signal)
}
