package io.github.nguyenyou.laminar.primitives.base

import io.github.nguyenyou.laminar.api.L

final class ComponentPropUpdater[V, Component](val prop: ComponentProp[V, Component], val signalSource: L.SignalSource[V])
    extends ComponentModifier[Component] {
  def applyTo(component: Component): Unit = prop.applySignal(component, signalSource)
}
