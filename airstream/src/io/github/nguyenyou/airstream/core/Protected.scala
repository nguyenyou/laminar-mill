package io.github.nguyenyou.airstream.core

import io.github.nguyenyou.ew.JsArray

import scala.annotation.{implicitNotFound, unused}
import scala.util.Try

@implicitNotFound("Implicit instance of Airstream's `Protected` class not found. You're trying to access a method which is designed to only be accessed from inside a BaseObservable subtype.")
class Protected private ()

object Protected {

  /** This mechanism allows us to define `protected` methods that have more lax access requirements
    * than Scala's `protected` keyword allows.
    *
    * Basically, if you created a custom observable subclass and inside of it you're trying to call
    * topoRank(), tryNow() or now() on another observable, Scala will tell you that you don't have
    * access to do that, but you can use one of these methods to access the required value.
    *
    * For example, instead of calling parentObservable.tryNow() you can call Protected.tryNow(parentObservable)
    *
    * The evidence is implicitly available inside BaseObservable, and so is available inside all
    * BaseObservable subtypes / implementations.
    */
  private[airstream] val protectedAccessEvidence: Protected = new Protected()

  @inline def topoRank[O[+_] <: Observable[?]](observable: BaseObservable[O, ?]): Int = {
    BaseObservable.topoRank(observable)
  }

  // Note: this implementation is not used in Airstream, and is provided
  // only for third party developers who don't want to use JsArray.
  def maxTopoRank[O[+_] <: Observable[?]](observables: Iterable[BaseObservable[O, ?]]): Int = {
    observables.foldLeft(0)((maxRank, parent) => Protected.topoRank(parent) max maxRank)
  }

  @inline def maxTopoRank[O <: Observable[?]](
    observables: JsArray[O]
  ): Int = {
    maxTopoRank(minRank = 0, observables)
  }

  def maxTopoRank[O <: Observable[?]](
    observable: Observable[?],
    observables: JsArray[O]
  ): Int = {
    maxTopoRank(minRank = Protected.topoRank(observable), observables)
  }

  def maxTopoRank[O <: Observable[?]](
    minRank: Int,
    observables: JsArray[O]
  ): Int = {
    var maxRank = minRank
    observables.forEach { observable =>
      val rank = Protected.topoRank(observable)
      if (rank > maxRank) {
        maxRank = rank
      }
    }
    maxRank
  }

  def lastUpdateId(signal: Signal[?])(implicit @unused ev: Protected): Int = signal.lastUpdateId

  @inline def tryNow[A](signal: Signal[A])(implicit @unused ev: Protected): Try[A] = signal.tryNow()

  @inline def now[A](signal: Signal[A])(implicit @unused ev: Protected): A = signal.now()

  @inline def maybeWillStart[O[+_] <: Observable[?]](observable: BaseObservable[O, ?]): Unit = {
    BaseObservable.maybeWillStart(observable)
  }
}
