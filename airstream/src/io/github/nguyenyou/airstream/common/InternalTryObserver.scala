package io.github.nguyenyou.airstream.common

import io.github.nguyenyou.airstream.core.{InternalObserver, Transaction}

import scala.util.{Failure, Success}

trait InternalTryObserver[-A] extends InternalObserver[A] {

  final override protected def onNext(nextValue: A, transaction: Transaction): Unit = {
    onTry(Success(nextValue), transaction)
  }

  final override protected def onError(nextError: Throwable, transaction: Transaction): Unit = {
    onTry(Failure(nextError), transaction)
  }
}
