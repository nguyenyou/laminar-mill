package io.github.nguyenyou.laminar.primitives.base

import io.github.nguyenyou.laminar.api.L

// Concrete modifier types with built-in application logic
final class ComponentPropSetter[V, Component](val prop: ComponentProp[V, Component], val value: V) extends ComponentModifier[Component] {
  def apply(component: Component): Unit = prop.setProp(component, value)
}
