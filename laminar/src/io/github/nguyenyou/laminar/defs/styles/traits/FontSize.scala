package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter
import io.github.nguyenyou.laminar.defs.styles.{units => u}
import io.github.nguyenyou.laminar.keys.DerivedStyleProp

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait FontSize extends u.Length[DerivedStyleProp, Int] { this: StyleProp[?] =>

  lazy val xxSmall: StyleSetter = this := "xx-small"

  lazy val xSmall: StyleSetter = this := "x-small"

  lazy val small: StyleSetter = this := "small"

  lazy val medium: StyleSetter = this := "medium"

  lazy val large: StyleSetter = this := "large"

  lazy val xLarge: StyleSetter = this := "x-large"

  lazy val xxLarge: StyleSetter = this := "xx-large"

  /**
    * Larger than the parent element's font size, by roughly the ratio used to
    * separate the absolute size keywords above.
    */
  lazy val larger: StyleSetter = this := "larger"

  /**
    * Smaller than the parent element's font size, by roughly the ratio used to
    * separate the absolute size keywords above.
    */
  lazy val smaller: StyleSetter = this := "smaller"

}
