package io.github.nguyenyou.laminar.receivers

import io.github.nguyenyou.airstream.core.Source.EventSource
import io.github.nguyenyou.laminar.modifiers.Binder
import io.github.nguyenyou.laminar.nodes.{ReactiveElement, ReactiveHtmlElement}

object FocusReceiver {

  def <--(isFocused: EventSource[Boolean]): Binder[ReactiveHtmlElement.Base] = {
    Binder { element =>
      ReactiveElement.bindFn(element, isFocused.toObservable) { shouldFocus =>
        if (shouldFocus) element.ref.focus() else element.ref.blur()
      }
    }
  }
}
