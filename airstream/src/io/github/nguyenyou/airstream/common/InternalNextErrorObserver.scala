package io.github.nguyenyou.airstream.common

import io.github.nguyenyou.airstream.core.{InternalObserver, Transaction}

import scala.util.Try

/** Observer that requires you to define `onNext` and `onError` */
trait InternalNextErrorObserver[A] extends InternalObserver[A] {

  final override protected def onTry(nextValue: Try[A], transaction: Transaction): Unit = {
    nextValue.fold(
      onError(_, transaction),
      onNext(_, transaction)
    )
  }
}
