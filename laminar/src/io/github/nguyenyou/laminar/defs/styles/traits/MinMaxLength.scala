package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter
import io.github.nguyenyou.laminar.defs.styles.{units => u}
import io.github.nguyenyou.laminar.keys.DerivedStyleProp

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait MinMaxLength extends u.Length[DerivedStyleProp, Int] { this: StyleProp[?] =>

  /** The intrinsic preferred length. */
  lazy val maxContent: StyleSetter = this := "max-content"

  /** The intrinsic minimum length. */
  lazy val minContent: StyleSetter = this := "min-content"

  /** Defined as min(max-content, max(min-content, fill-available)). */
  lazy val fitContent: StyleSetter = this := "fit-content"

  /** The containing block width minus margin, border and padding. */
  lazy val fillAvailable: StyleSetter = this := "fill-available"

}
