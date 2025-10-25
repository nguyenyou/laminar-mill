package io.github.nguyenyou.laminar.primitives.base
import io.github.nguyenyou.laminar.api.L

// Abstract Prop class with type-safe application methods
abstract class ComponentProp[V, A](val name: String) {
  // Abstract methods that subclasses must implement for type-safe application
  def applyValue(component: A, value: V): Unit
  def applySignal(component: A, source: L.SignalSource[V]): Unit

  inline def apply(value: V): ComponentPropSetter[V, A] = this := value

  def :=(value: V): ComponentPropSetter[V, A] = {
    new ComponentPropSetter(this, value)
  }

  def <--(source: L.SignalSource[V]): ComponentPropModifier[V, A] = {
    new ComponentPropModifier(this, source)
  }
}
