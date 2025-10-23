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

package java.nio

import scala.scalajs.js.typedarray._

object DoubleBuffer {
  private final val HashSeed = 2140173175 // "java.nio.DoubleBuffer".##

  def allocate(capacity: Int): DoubleBuffer = {
    GenBuffer.validateAllocateCapacity(capacity)
    wrap(new Array[Double](capacity))
  }

  def wrap(array: Array[Double], offset: Int, length: Int): DoubleBuffer =
    HeapDoubleBuffer.wrap(array, 0, array.length, offset, length, false)

  def wrap(array: Array[Double]): DoubleBuffer =
    wrap(array, 0, array.length)

  // Extended API

  def wrapFloat64Array(array: Float64Array): DoubleBuffer =
    TypedArrayDoubleBuffer.wrapFloat64Array(array)
}

abstract class DoubleBuffer private[nio] (
    _capacity: Int, private[nio] val _array: Array[Double],
    private[nio] val _arrayOffset: Int)
    extends Buffer(_capacity) with Comparable[DoubleBuffer] {

  private[nio] type ElementType = Double
  private[nio] type BufferType = DoubleBuffer
  private[nio] type TypedArrayType = Float64Array

  def this(_capacity: Int) = this(_capacity, null, -1)

  def slice(): DoubleBuffer

  def duplicate(): DoubleBuffer

  def asReadOnlyBuffer(): DoubleBuffer

  def get(): Double

  def put(d: Double): DoubleBuffer

  def get(index: Int): Double

  def put(index: Int, d: Double): DoubleBuffer

  @noinline
  def get(dst: Array[Double], offset: Int, length: Int): DoubleBuffer =
    GenBuffer(this).generic_get(dst, offset, length)

  def get(dst: Array[Double]): DoubleBuffer =
    get(dst, 0, dst.length)

  @noinline
  def put(src: DoubleBuffer): DoubleBuffer =
    GenBuffer(this).generic_put(src)

  @noinline
  def put(src: Array[Double], offset: Int, length: Int): DoubleBuffer =
    GenBuffer(this).generic_put(src, offset, length)

  final def put(src: Array[Double]): DoubleBuffer =
    put(src, 0, src.length)

  @inline final def hasArray(): Boolean =
    GenBuffer(this).generic_hasArray()

  @inline final def array(): Array[Double] =
    GenBuffer(this).generic_array()

  @inline final def arrayOffset(): Int =
    GenBuffer(this).generic_arrayOffset()

  @inline override def position(newPosition: Int): DoubleBuffer = {
    super.position(newPosition)
    this
  }

  @inline override def limit(newLimit: Int): DoubleBuffer = {
    super.limit(newLimit)
    this
  }

  @inline override def mark(): DoubleBuffer = {
    super.mark()
    this
  }

  @inline override def reset(): DoubleBuffer = {
    super.reset()
    this
  }

  @inline override def clear(): DoubleBuffer = {
    super.clear()
    this
  }

  @inline override def flip(): DoubleBuffer = {
    super.flip()
    this
  }

  @inline override def rewind(): DoubleBuffer = {
    super.rewind()
    this
  }

  def compact(): DoubleBuffer

  def isDirect(): Boolean

  // toString(): String inherited from Buffer

  @noinline
  override def hashCode(): Int =
    GenBuffer(this).generic_hashCode(DoubleBuffer.HashSeed)

  override def equals(that: Any): Boolean = that match {
    case that: DoubleBuffer => compareTo(that) == 0
    case _                  => false
  }

  @noinline
  def compareTo(that: DoubleBuffer): Int =
    GenBuffer(this).generic_compareTo(that)(java.lang.Double.compare(_, _))

  def order(): ByteOrder

  // Internal API

  private[nio] def load(index: Int): Double

  private[nio] def store(index: Int, elem: Double): Unit

  @inline
  private[nio] def load(startIndex: Int,
      dst: Array[Double], offset: Int, length: Int): Unit =
    GenBuffer(this).generic_load(startIndex, dst, offset, length)

  @inline
  private[nio] def store(startIndex: Int,
      src: Array[Double], offset: Int, length: Int): Unit =
    GenBuffer(this).generic_store(startIndex, src, offset, length)
}
