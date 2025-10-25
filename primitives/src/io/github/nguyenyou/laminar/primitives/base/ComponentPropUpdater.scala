package io.github.nguyenyou.laminar.primitives.base

import io.github.nguyenyou.laminar.api.L

final class ComponentPropUpdater[V, A](val prop: ComponentProp[V, A], val signalSource: L.SignalSource[V]) extends ComponentModifier[A] {
  def applyTo(component: A): Unit = prop.applySignal(component, signalSource)
}
