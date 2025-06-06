package io.github.nguyenyou.airstream.extensions

import io.github.nguyenyou.airstream.core.Observable
import io.github.nguyenyou.airstream.flatten._

import scala.annotation.unused

class MetaObservable[A, Outer[+_] <: Observable[?], Inner[_]](
  val parent: Outer[Inner[A]]
) extends AnyVal {

  @inline def flatten[Output[+_] <: Observable[?]](
    implicit strategy: SwitchingStrategy[Outer, Inner, Output],
    @unused allowFlatMap: AllowFlatten
  ): Output[A] = {
    strategy.flatten(parent)
  }

  @inline def flattenSwitch[Output[+_] <: Observable[?]](
    implicit strategy: SwitchingStrategy[Outer, Inner, Output]
  ): Output[A] = {
    strategy.flatten(parent)
  }

  @inline def flattenMerge[Output[+_] <: Observable[?]](
    implicit strategy: MergingStrategy[Outer, Inner, Output]
  ): Output[A] = {
    strategy.flatten(parent)
  }

  @inline def flattenCustom[Output[+_] <: Observable[?]](
    strategy: FlattenStrategy[Outer, Inner, Output]
  ): Output[A] = {
    strategy.flatten(parent)
  }

}
