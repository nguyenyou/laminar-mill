package io.github.nguyenyou.laminar

import scala.annotation.implicitNotFound

package object api extends Implicits {

  lazy val A: AirstreamAliases = new AirstreamAliases {}

  val L: Laminar = new Laminar {}

  // --

  @implicitNotFound(
    "You must `import io.github.nguyenyou.laminar.api.features.unitArrows` to allow expressions of type `Unit` " +
      "on the right hand side of `-->` methods, because this is not completely safe in Scala 3. " +
      "Please read the documentation first to learn about the caveats: https://laminar.dev/documentation#-unit-sinks"
  )
  trait UnitArrowsFeature

  object features {

    implicit lazy val unitArrows: UnitArrowsFeature = new UnitArrowsFeature {}
  }
}
