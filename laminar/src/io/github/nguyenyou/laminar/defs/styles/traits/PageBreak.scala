package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait PageBreak extends Auto { this: StyleProp[?] =>

  /** Always force page breaks. */
  lazy val always: StyleSetter = this := "always"

  /** Avoid page breaks. */
  lazy val avoid: StyleSetter = this := "avoid"

  /** Force page breaks so that the next page is formatted as a left page. */
  lazy val left: StyleSetter = this := "left"

  /** Force page breaks so that the next page is formatted as a right page. */
  lazy val right: StyleSetter = this := "right"

}
