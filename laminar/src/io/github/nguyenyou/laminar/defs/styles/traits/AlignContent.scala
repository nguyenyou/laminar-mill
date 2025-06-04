package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait AlignContent extends FlexPosition { this: StyleProp[?] =>

  lazy val spaceBetween: StyleSetter = this := "space-between"

  lazy val spaceAround: StyleSetter = this := "space-around"

  lazy val spaceEvenly: StyleSetter = this := "space-evenly"

}
