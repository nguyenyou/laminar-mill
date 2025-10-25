package io.github.nguyenyou.laminar.primitives.base
import io.github.nguyenyou.laminar.api.L

abstract class ComponentProp[V, A](val name: String) {
  def applyValue(component: A, value: V): Unit
  def applySignal(component: A, signalSource: L.SignalSource[V]): Unit

  inline def apply(value: V): ComponentPropSetter[V, A] = this := value

  def :=(value: V): ComponentPropSetter[V, A] = {
    new ComponentPropSetter(this, value)
  }

  def <--(signalSource: L.SignalSource[V]): ComponentPropUpdater[V, A] = {
    new ComponentPropUpdater(this, signalSource)
  }
}
