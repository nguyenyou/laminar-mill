package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter
import io.github.nguyenyou.laminar.defs.styles.{units => u}
import io.github.nguyenyou.laminar.keys.DerivedStyleProp

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait BackgroundSize extends Auto, u.Length[DerivedStyleProp, Int] { this: StyleProp[?] =>

  /** This keyword specifies that the background image should be scaled to be as small as possible while ensuring both its dimensions are
    * greater than or equal to the corresponding dimensions of the background positioning area.
    */
  lazy val cover: StyleSetter = this := "cover"

  /** This keyword specifies that the background image should be scaled to be as large as possible while ensuring both its dimensions are
    * less than or equal to the corresponding dimensions of the background positioning area.
    */
  lazy val contain: StyleSetter = this := "contain"

}
