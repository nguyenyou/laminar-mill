/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.scalajs.js.typedarray

import scala.scalajs.js
import scala.scalajs.js.annotation._

/** <span class="badge badge-ecma6" style="float: right;">ECMAScript 6</span>
 *  A [[TypedArray]] of signed 8-bit integers
 */
@js.native
@JSGlobal
class Int8Array private[this] () extends TypedArray[Byte, Int8Array] {

  /** Constructs a Int8Array with the given length. Initialized to all 0 */
  def this(length: Int) = this()

  /** Creates a new Int8Array with the same elements than the given TypedArray
   *
   *  The elements are converted before being stored in the new Int8Array.
   */
  def this(typedArray: Int8Array) = this()

  /** Creates a new Int8Array with the elements in the given array */
  def this(array: js.Iterable[Byte]) = this()

  /** Creates a Int8Array view on the given ArrayBuffer */
  def this(buffer: ArrayBuffer, byteOffset: Int = 0, length: Int = ???) = this()

}

/** <span class="badge badge-ecma6" style="float: right;">ECMAScript 6</span>
 *  [[Int8Array]] companion
 */
@js.native
@JSGlobal
object Int8Array extends TypedArrayStatic[Byte, Int8Array]
