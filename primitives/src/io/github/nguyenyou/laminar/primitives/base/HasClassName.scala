package io.github.nguyenyou.laminar.primitives.base

import io.github.nguyenyou.laminar.api.L.*

trait HasClassName {
  def setClassName(value: String): Unit
  def updateClassName(values: Source[String]): Unit
}
