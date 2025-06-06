package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait EmptyCells { this: StyleProp[?] =>

  /** Borders and backgrounds should be drawn like in a normal cells. */
  lazy val show: StyleSetter = this := "show"

  /** No border or backgrounds of empty cells should be drawn. */
  lazy val hide: StyleSetter = this := "hide"

}
