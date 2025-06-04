package io.github.nguyenyou.airstream.extensions

import io.github.nguyenyou.airstream.core.{EventStream, Signal}
import io.github.nguyenyou.airstream.split.SplittableOneStream

class BooleanStream(val stream: EventStream[Boolean]) extends AnyVal {

  /**
    * Split a stream of booleans.
    *
    * @param trueF  called when the parent stream switches from `false` to `true`.
    *               The provided signal emits `Unit` on every `true` event emitted by the stream.
    * @param falseF called when the parent stream switches from `true` to `false`.
    *               The provided signal emits `Unit` on every `false` event emitted by the stream.
    */
  def splitBoolean[C](
    trueF: Signal[Unit] => C,
    falseF: Signal[Unit] => C
  ): EventStream[C] = {
    new SplittableOneStream(stream).splitOne(identity) {
      (_, initial, signal) =>
        if (initial)
          trueF(signal.mapToUnit)
        else
          falseF(signal.mapToUnit)
    }
  }
}
