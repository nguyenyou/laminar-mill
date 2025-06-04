package io.github.nguyenyou.airstream

package object util {

  val always: Any => Boolean = _ => true

  def hasDuplicateTupleKeys(tuples: Seq[(?, ?)]): Boolean = {
    tuples.size != tuples.map(_._1).toSet.size
  }
}
