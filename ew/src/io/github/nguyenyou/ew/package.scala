package io.github.nguyenyou

import io.github.nguyenyou.ew.JsArray.RichScalaJsArray
import io.github.nguyenyou.ew.JsIterable.RichScalaJsIterable
import io.github.nguyenyou.ew.JsMap.RichScalaJsMap
import io.github.nguyenyou.ew.JsSet.RichScalaJsSet
import io.github.nguyenyou.ew.JsString.RichString

import scala.scalajs.js

package object ew {

  implicit def ewString(str: String): RichString = new RichString(str)

  implicit def ewIterable[A](arr: js.Array[A]): RichScalaJsIterable[A] = new RichScalaJsIterable(arr)

  implicit def ewArray[A](arr: js.Array[A]): RichScalaJsArray[A] = new RichScalaJsArray(arr)

  implicit def ewSet[A](set: js.Set[A]): RichScalaJsSet[A] = new RichScalaJsSet(set)

  implicit def ewMap[K, V](map: js.Map[K, V]): RichScalaJsMap[K, V] = new RichScalaJsMap(map)
}
