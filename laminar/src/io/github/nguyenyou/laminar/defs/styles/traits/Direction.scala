package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait Direction { this: StyleProp[?] =>

  /** Text and other elements go from left to right. */
  lazy val ltr: StyleSetter = this := "ltr"

  /** Text and other elements go from right to left. */
  lazy val rtl: StyleSetter = this := "rtl"

}
