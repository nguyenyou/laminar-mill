package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait BorderCollapse { this: StyleProp[?] =>

  /** Use separated-border table rendering model. This is the default. */
  lazy val separate: StyleSetter = this := "separate"

  /** Use collapsed-border table rendering model. */
  lazy val collapse: StyleSetter = this := "collapse"

}
