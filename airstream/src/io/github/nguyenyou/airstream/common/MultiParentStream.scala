package io.github.nguyenyou.airstream.common

import io.github.nguyenyou.airstream.core.{Observable, Protected, WritableStream}
import com.raquo.ew.JsArray

/** A simple stream that has multiple parents. */
trait MultiParentStream[I, O] extends WritableStream[O] {

  /** This array is read-only, never update it. */
  protected val parents: JsArray[Observable[I]]

  override protected def onWillStart(): Unit = {
    parents.forEach(Protected.maybeWillStart(_))
  }

}
