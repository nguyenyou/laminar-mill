package io.github.nguyenyou.laminar.keys

import io.github.nguyenyou.airstream.core.{EventStream, Observable, Observer, Sink}
import io.github.nguyenyou.laminar.api.UnitArrowsFeature
import io.github.nguyenyou.laminar.modifiers.Binder
import io.github.nguyenyou.laminar.nodes.ReactiveElement
import org.scalajs.dom

class LockedEventKey[Ev <: dom.Event, -In, +Out](
  eventProcessor: EventProcessor[Ev, In],
  composer: EventStream[In] => Observable[Out]
) {

  def -->(sink: Sink[Out]): Binder.Base = {
    Binder { el =>
      val observable = composer(el.events(eventProcessor))
      ReactiveElement.bindSink[Out](el, observable)(sink)
    }
  }

  @inline def -->(onNext: Out => Unit): Binder.Base = {
    this --> (Observer(onNext))
  }

  @inline def -->(onNext: => Unit)(implicit evidence: UnitArrowsFeature): Binder.Base = {
    this --> (Observer[Any](_ => onNext))
  }
}
