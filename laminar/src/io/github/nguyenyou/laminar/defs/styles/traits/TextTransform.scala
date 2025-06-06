package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait TextTransform extends None { this: StyleProp[?] =>

  /**
    * Forces the first letter of each word to be converted to
    * uppercase. Other characters are unchanged.
    */
  lazy val capitalize: StyleSetter = this := "capitalize"

  /** Forces all characters to be converted to uppercase. */
  lazy val uppercase: StyleSetter = this := "uppercase"

  /** Forces all characters to be converted to lowercase. */
  lazy val lowercase: StyleSetter = this := "lowercase"

}
