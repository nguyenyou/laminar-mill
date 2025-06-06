package io.github.nguyenyou.airstream.extensions

import io.github.nguyenyou.airstream.core.{BaseObservable, Observable}

class BooleanObservable[Self[+_] <: Observable[?]](val observable: BaseObservable[Self, Boolean]) extends AnyVal {

  def invert: Self[Boolean] = {
    observable.map(!_)
  }

  @inline def not: Self[Boolean] = invert

  def foldBoolean[A](
    whenTrue: => A,
    whenFalse: => A
  ): Self[A] = {
    observable map {
      if (_) whenTrue else whenFalse
    }
  }

}
