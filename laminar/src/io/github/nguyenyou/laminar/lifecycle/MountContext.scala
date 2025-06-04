package io.github.nguyenyou.laminar.lifecycle

import io.github.nguyenyou.airstream.ownership.Owner
import io.github.nguyenyou.laminar.nodes.ReactiveElement

class MountContext[+El <: ReactiveElement.Base](
  val thisNode: El,
  implicit val owner: Owner
) {

  // @TODO I can't get this to work, unfortunately. Would have been a nice alias.
  // @inline def ref: El#Ref = thisNode.ref
}
