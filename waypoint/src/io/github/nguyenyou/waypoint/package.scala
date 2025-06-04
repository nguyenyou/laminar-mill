package io.github.nguyenyou

import urldsl.errors._
import urldsl.vocabulary.{PathQueryFragmentMatching, UrlMatching}

/** This is the public API. Import io.github.nguyenyou.waypoint or io.github.nguyenyou.waypoint._ */
package object waypoint extends Waypoint(
  DummyError.dummyErrorIsPathMatchingError,
  DummyError.dummyErrorIsParamMatchingError,
  DummyError.dummyErrorIsFragmentMatchingError
) {

  // @TODO[URL-DSL] PatternArgs should be FragmentPatternArgs with FragmentArgs=Unit, not different types

  /** A bundle of path args and query param args */
  type PatternArgs[PathArgs, QueryArgs] = UrlMatching[PathArgs, QueryArgs]

  /** A bundle of path args, query param args, and fragment args */
  type FragmentPatternArgs[PathArgs, QueryArgs, FragmentArgs] = PathQueryFragmentMatching[PathArgs, QueryArgs, FragmentArgs]

  @inline def PatternArgs: UrlMatching.type = UrlMatching

  @inline def FragmentPatternArgs: PathQueryFragmentMatching.type = PathQueryFragmentMatching
}
