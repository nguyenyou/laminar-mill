package io.github.nguyenyou.laminar.primitives.base

import io.github.nguyenyou.laminar.api.L.*

trait HasClassNameProp[A <: HasClassName] {
  object ClassNameProp extends ComponentProp[String, A] {
    private[primitives] def setProp(component: A, value: String): Unit = {
      component.setClassName(value)
    }

    private[primitives] def updateProp(component: A, values: Source[String]): Unit = {
      component.updateClassName(values)
    }
  }
}
