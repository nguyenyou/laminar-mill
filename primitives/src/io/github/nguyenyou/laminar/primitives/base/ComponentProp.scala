package io.github.nguyenyou.laminar.primitives.base
import io.github.nguyenyou.laminar.api.L

abstract class ComponentProp[V, Component](val name: String) {
  def applyValue(component: Component, value: V): Unit
  def applySignal(component: Component, signalSource: L.SignalSource[V]): Unit

  inline def apply(value: V): ComponentPropSetter[V, Component] = this := value

  def :=(value: V): ComponentPropSetter[V, Component] = {
    new ComponentPropSetter(this, value)
  }

  def <--(signalSource: L.SignalSource[V]): ComponentPropUpdater[V, Component] = {
    new ComponentPropUpdater(this, signalSource)
  }
}
