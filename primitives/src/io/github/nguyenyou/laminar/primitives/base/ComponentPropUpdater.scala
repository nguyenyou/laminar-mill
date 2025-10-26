package io.github.nguyenyou.laminar.primitives.base

import io.github.nguyenyou.laminar.api.L

final class ComponentPropUpdater[V, Component](val prop: ComponentProp[V, Component], val values: L.Source[V])
    extends ComponentModifier[Component] {
  def apply(component: Component): Unit = prop.updateProp(component, values)
}
