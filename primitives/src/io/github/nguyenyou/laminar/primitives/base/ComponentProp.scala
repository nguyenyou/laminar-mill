package io.github.nguyenyou.laminar.primitives.base
import io.github.nguyenyou.laminar.api.L

trait ComponentProp[V, Component] {
  private[primitives] def setProp(component: Component, value: V): Unit
  private[primitives] def updateProp(component: Component, values: L.Source[V]): Unit

  inline def apply(value: V): ComponentPropSetter[V, Component] = this := value

  def :=(value: V): ComponentPropSetter[V, Component] = {
    new ComponentPropSetter(this, value)
  }

  def <--(values: L.Source[V]): ComponentPropUpdater[V, Component] = {
    new ComponentPropUpdater(this, values)
  }
}
