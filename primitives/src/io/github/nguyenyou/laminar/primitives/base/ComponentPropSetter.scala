package io.github.nguyenyou.laminar.primitives.base

import io.github.nguyenyou.laminar.api.L

// Concrete modifier types with built-in application logic
final class ComponentPropSetter[V, A](val prop: ComponentProp[V, A], val value: V) extends ComponentModifier[A] {
  def applyTo(component: A): Unit = prop.applyValue(component, value)
}
