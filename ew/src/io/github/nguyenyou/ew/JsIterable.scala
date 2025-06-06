package io.github.nguyenyou.ew

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

@js.native
trait JsIterable[+A] extends js.Object {

  /** !! Not supported by IE !! */
  @JSName(js.Symbol.iterator)
  def iterator(): js.Iterator[A] = js.native
}

object JsIterable {

  implicit class RichJsIterable[A](val iterable: JsIterable[A]) extends AnyVal {

    def ext: JsIterableExt[A] = new JsIterableExt(iterable)

    @inline def asScalaJsIterable: js.Iterable[A] = iterable.asInstanceOf[js.Iterable[A]]
  }

  class JsIterableExt[A](val iterable: JsIterable[A]) extends AnyVal {

    // #TODO[API] Strictly speaking, this method does not exist on JS iterables, but it
    //  does exist on its subclasses Array, Set, and Map that we care about. Regardless,
    //  this method is hidden behind `ext` mostly because we don't want this method
    //  implementation to override the implementation of `forEach` in JsArray. Maybe
    //  there's a better way to reliably accomplish this, not sure.
    def forEach(f: js.Function1[A, Unit]): Unit = {
      val iterator = iterable.iterator()
      var entry = iterator.next()
      while (!entry.done) {
        f(entry.value)
        entry = iterator.next()
      }
    }
  }

  class RichScalaJsIterable[A](val iterable: js.Iterable[A]) extends AnyVal {

    @inline def asJsIterable: JsIterable[A] = iterable.asInstanceOf[JsIterable[A]]
  }
}
