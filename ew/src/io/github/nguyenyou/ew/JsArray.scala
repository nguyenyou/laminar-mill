package io.github.nguyenyou.ew

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.|

/**
  * To construct a new array with uninitialized elements, use the constructor
  * of this class. To construct a new array with specified elements, as if
  * you used the array literal syntax in JavaScript, use the companion object's
  * `apply` method instead.
  *
  * Note that Javascript `===` equality semantics apply. JsArray does not know
  * anything about Scala `equals` method or the case classes structural equality.
  *
  * [[https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/length Array @ MDN]]
  *
  * @tparam A Type of the elements of the array
  * @constructor Creates a new array of length 0.
  */
@js.native
@JSGlobal("Array")
class JsArray[A] extends JsIterable[A] {

  /**
    * Create a new array with the given length
    * (filled with `js.undefined` irrespective of the type argument `A`!).
    *
    * See companion object for more factories.
    *
    * @param arrayLength Initial length of the array.
    */
  def this(arrayLength: Int) = this()

  /** Length of the array. */
  def length: Int = js.native

  /**
    * Set the length of the array.
    *
    * If the new length is bigger than the old length, created slots are
    * filled with `undefined` (irrespective of the type argument `A`!).
    *
    * If the new length is smaller than the old length, the array is shrunk.
    *
    * [[https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/length Array.length @ MDN]]
    */
  def length_=(v: Int): Unit = js.native

  /** Access the element at the given index. */
  @JSBracketAccess
  def apply(index: Int): A = js.native

  /** Set the element at the given index. */
  @JSBracketAccess
  def update(index: Int, value: A): Unit = js.native

  /**
    * Create a new array populated with the results of calling a provided function
    * on every element in the calling array.
    */
  def map[B](project: js.Function1[A, B]): JsArray[B] = js.native

  @JSName("map")
  def mapWithIndex[B](project: js.Function2[A, Int, B]): JsArray[B] = js.native

  /**
    * Create a shallow copy of a portion of a given array, filtered down to just the elements
    * from the given array that pass the test implemented by the provided function.
    */
  def filter(passes: js.Function1[A, Boolean]): JsArray[A] = js.native

  @JSName("filter")
  def filterWithIndex(passes: js.Function2[A, Int, Boolean]): JsArray[A] = js.native

  /**
    * If array only has one item, array[0] is returned without calling `f`.
    *
    * Note: throws exception if array is empty.
    *
    * @param f (accumulator, nextValue) => nextAccumulator
    *          On first call of `f`, `accumulator` is `array[0]`, and `nextValue` is `array[1]`.
    */
  def reduce[B](f: js.Function2[B, A, B]): B = js.native

  /**
    * If array is empty or only has one item, `initial` is returned without calling `f`.
    *
    * @param f (accumulator, nextValue) => nextAccumulator
    *          On first call of `f`, `accumulator` is `initial`, and `nextValue` is `array[0]`
    */
  def reduce[B](f: js.Function2[B, A, B], initial: B): B = js.native

  /**
    * @param f (accumulator, nextValue, nextIndex) => nextAccumulator
    *          On first call of `f`, `accumulator` is `array[0]`, and `nextValue` is `array[1]`.
    *
    *          If array only has one item, array[0] is returned without calling `f`.
    *
    *          Note: throws exception if array is empty.
    */
  @JSName("reduce")
  def reduceWithIndex[B](f: js.Function3[B, A, Int, B]): B = js.native

  /**
    * If array is empty or only has one item, `initial` is returned without calling `f`.
    *
    * @param f (accumulator, nextValue, nextIndex) => nextAccumulator
    *          On first call of `f`, `accumulator` is `initial`, and `nextValue` is `array[0]`
    */
  @JSName("reduce")
  def reduceWithIndex[B](f: js.Function3[B, A, Int, B], initial: B): B = js.native

  /**
    * Create a new array consisting of the elements in the this object
    * on which it is called, followed in order by, for each argument, the
    * elements of that argument
    */
  def concat[B >: A](items: (JsArray[? <: B] | JsVector[? <: B] | js.Array[? <: B])*): JsArray[B] = js.native

  def indexOf(item: A, fromIndex: Int = 0): Int = js.native

  /**
    * Join all elements of an array into a string.
    *
    * separator specifies a string to separate each element of the array.
    * The separator is converted to a string if necessary. If omitted, the
    * array elements are separated with a comma.
    */
  def join(seperator: String = ","): String = js.native

  /**
    * Remove the last element from an array and returns that element.
    *
    * Returns js.undefined if array is empty.
    */
  def pop(): A = js.native

  /**
    * Mutate an array by appending the given elements and returning the new
    * length of the array.
    */
  def push(items: A*): Int = js.native

  /**
    * Reverse an array in place. The first array element
    * becomes the last and the last becomes the first.
    */
  def reverse(): JsArray[A] = js.native

  /**
    * Remove the first element from an array and return that element.
    *
    * Returns js.undefined if array is empty.
    */
  def shift(): A = js.native

  /**
    * Return a shallow copy of a portion of an array.
    */
  def slice(start: Int = 0, end: Int = Int.MaxValue): JsArray[A] = js.native

  /**
    * Sort the elements of an array in place and return the
    * array. The default sort order is lexicographic (not numeric).
    *
    * !! The sort is not stable in IE!
    *
    * If compareFn is not supplied, elements are sorted by converting them
    * to strings and comparing strings in lexicographic ("dictionary" or "telephone
    * book," not numerical) order. For example, "80" comes before "9" in
    * lexicographic order, but in a numeric sort 9 comes before 80.
    *
    * [[https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort Array.sort @ MDN]]
    */
  def sort(compareFn: js.Function2[A, A, Int]): JsArray[A] = js.native

  /** Remove and add new elements at a given index in the array.
    *
    * This method first removes `deleteCount` elements starting from the index
    * `index`, then inserts the new elements `items` at that index.
    *
    * If `index` is negative, it is treated as that number of elements starting
    * from the end of the array.
    *
    * @param index       Index where to start changes
    * @param deleteCount Number of elements to delete from index
    * @param items       Elements to insert at index
    * @return An array of the elements that were deleted
    */
  def splice(index: Int, deleteCount: Int, items: A*): JsArray[A] = js.native

  /**
    * Add one or more elements to the beginning of the array
    * and return the new length of the array.
    */
  def unshift(items: A*): Int = js.native

}

object JsArray {

  // @TODO The `apply` method calls into js.Array because instantiating an array of
  //  one integer requires syntax like `[5]` in Javascript, and I don't know how
  //  to get Scala.js to generate such JS code aside from using js.Array.
  //    Note: `Array(5)` in JS creates an array with 5 empty slots instead of an
  //    array with an element `5` in it, as [5] does.

  /**
    * Creates a new array with the given items, equivalent to `[item1, item2, ...]` literal
    *
    * Note:If you want to preallocate N items, use `new JsArray(N)`
    *
    * [[https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array Array @ MDN]]
    */
  def apply[A](items: A*): JsArray[A] = js.Array(items*).asInstanceOf[JsArray[A]]

  /**
    * Returns true if the given value is an array.
    *
    * [[https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/isArray Array.isArray @ MDN]]
    */
  def isArray(arg: scala.Any): Boolean = rawJsArray.isArray(arg)

  /**
    * Creates a new array from a JS iterable (array, set, map, etc.).
    * !! Not supported by IE !!
    */
  def from[A](arr: JsIterable[A]): JsArray[A] = rawJsArray.from(arr)

  /**
    * Creates a new array from a JS iterable (array, set, map, etc.).
    * !! Not supported by IE !!
    */
  def from[A, B](arr: JsIterable[A], mapFn: js.Function2[A, Int, B]): JsArray[B] = rawJsArray.from(arr, mapFn)

  /**
    * Creates a new array from a JS iterable (array, set, map, etc.).
    * !! Not supported by IE !!
    */
  def from[A](arr: js.Iterable[A]): JsArray[A] = rawJsArray.from(arr)

  /**
    * Creates a new array from a JS iterable (array, set, map, etc.).
    * !! Not supported by IE !!
    */
  def from[A, B](arr: js.Iterable[A], mapFn: js.Function2[A, Int, B]): JsArray[B] = rawJsArray.from(arr, mapFn)

  /** Copy an array into a new array */
  def from[A](items: Iterable[A]): JsArray[A] = {
    val arr = JsArray[A]()
    items.foreach(arr.push(_))
    arr
  }

  /** Copy an array into a new array */
  def from[A](items: scala.Array[A]): JsArray[A] = {
    val arr = JsArray[A]()
    items.foreach(arr.push(_))
    arr
  }

  /** Make a new array out of js.UndefOr */
  def from[A](maybeItem: js.UndefOr[A])(implicit dummyImplicit: DummyImplicit): JsArray[A] = {
    maybeItem.fold(JsArray[A]())(JsArray(_))
  }

  // --

  implicit class RichJsArray[A](val arr: JsArray[A]) extends AnyVal {

    /** Note: the browser's native `includes` method does not work in IE11. This one does. */
    def includes(item: A, fromIndex: Int = 0): Boolean = {
      arr.indexOf(item, fromIndex) != -1
    }

    /** Note: this implementation is faster than calling into JS native `forEach`. */
    def forEach(cb: js.Function1[A, Any]): Unit = {
      var i = 0
      val len = arr.length
      while (i < len) {
        cb(arr(i))
        i += 1
      }
    }

    /** Similar to the native two-argument version of forEach */
    def forEachWithIndex(cb: js.Function2[A, Int, Any]): Unit = {
      var i = 0
      val len = arr.length
      while (i < len) {
        cb(arr(i), i)
        i += 1
      }
    }

    @inline def asScalaJs: js.Array[A] = arr.asInstanceOf[js.Array[A]]

    /**
      * Unsafe because JsVector users assume it's immutable,
      * but the original array can be used to mutate it.
      */
    @inline def unsafeAsJsVector: JsVector[A] = arr.asInstanceOf[JsVector[A]]
  }

  // --

  class RichScalaJsArray[A](val arr: js.Array[A]) extends AnyVal {

    @inline def ew: JsArray[A] = arr.asInstanceOf[JsArray[A]]

    /**
      * Unsafe because JsVector users assume it's immutable,
      * but the original array can be used to mutate it.
      */
    @inline def unsafeAsJsVector: JsVector[A] = arr.asInstanceOf[JsVector[A]]
  }

  // --

  @js.native
  @JSGlobal("Array")
  private[ew] object rawJsArray extends js.Object {

    def isArray(arg: scala.Any): Boolean = js.native

    /** Create a shallow copy of the array */
    def from[A](arr: JsIterable[A]): JsArray[A] = js.native

    /** Create a shallow copy of the array */
    def from[A, B](arr: JsIterable[A], mapFn: js.Function2[A, Int, B]): JsArray[B] = js.native

    /** Create a shallow copy of the array */
    def from[A](arr: js.Iterable[A]): JsArray[A] = js.native

    /** Create a shallow copy of the array */
    def from[A, B](arr: js.Iterable[A], mapFn: js.Function2[A, Int, B]): JsArray[B] = js.native
  }

}
