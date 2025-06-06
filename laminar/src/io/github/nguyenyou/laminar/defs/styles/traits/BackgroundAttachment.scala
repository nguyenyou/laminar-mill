package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait BackgroundAttachment { this: StyleProp[?] =>

  /**
    * The background is fixed relative to the viewport. Even if an element has
    * a scrolling mechanism, the background doesn't move with the element.
    * (This is not compatible with background-clip: text.)
    */
  lazy val fixed: StyleSetter = this := "fixed"

  /**
    * The background is fixed relative to the element's contents. If the element
    * has a scrolling mechanism, the background scrolls with the element's
    * contents, and the background painting area and background positioning area
    * are relative to the scrollable area of the element rather than to the
    * border framing them.
    */
  lazy val local: StyleSetter = this := "local"

  /**
    * The background is fixed relative to the element itself and does not scroll
    * with its contents. (It is effectively attached to the element's border.)
    */
  lazy val scroll: StyleSetter = this := "scroll"

}
