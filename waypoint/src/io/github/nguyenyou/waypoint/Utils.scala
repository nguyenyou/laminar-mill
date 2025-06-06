package io.github.nguyenyou.waypoint

object Utils {

  // @TODO[Security] This is rather ad-hoc. Review later.
  @inline private[waypoint] def isRelative(url: String): Boolean = {
    url.startsWith("/") && !url.startsWith("//")
  }

  @inline private[waypoint] def absoluteUrlMatchesOrigin(origin: String, url: String): Boolean = {
    // In typical browsers, when the user navigates to a "root" URL like `http://example.com`,
    // the browser adds a trailing slash and sets the URL to `http://example.com/`.
    // However, Tauri does not do this, so we also need to check `url == origin`.
    url == origin || url.startsWith(origin + "/")
  }

  private[waypoint] def makeRelativeUrl(origin: String, absoluteUrl: String): String = {
    if (absoluteUrlMatchesOrigin(origin, absoluteUrl)) {
      absoluteUrl.substring(origin.length)
    } else {
      throw new WaypointException(s"Can not make `$absoluteUrl` into relative URL, origin does not match `$origin`.")
    }
  }

  // #Note This is different from basePath.endsWith("#") in case of multiple `#`
  private[waypoint] def basePathHasEmptyFragment(basePath: String): Boolean = {
    basePath.nonEmpty && {
      val hashIndex = basePath.indexOf('#')
      hashIndex == basePath.length - 1
    }
  }

  private[waypoint] def basePathWithoutFragment(basePath: String): String = {
    if (basePath.isEmpty) {
      basePath
    } else {
      val hashIndex = basePath.indexOf('#')
      basePath.substring(0, hashIndex)
    }
  }

  // @TODO[Scala3] The isDefinedOf method of pf1.andThen(pf2) is broken in Scala 2.12, it does not consider pf2.isDefinedOf
  //  - This is fixed in Scala 2.13, but until then we provide an implementation similar to 2.13 (with the same tests as in scala/scala)
  //  - https://github.com/scala/scala/pull/7263
  @inline private[waypoint] def andThenPF[A, B, C](pf: PartialFunction[A, B], k: PartialFunction[B, C]): PartialFunction[A, C] = {
    new PartialFunction[A, C] with Serializable {
      def isDefinedAt(x: A): Boolean = {
        val b = pf.andThen[Option[B]](Some(_)).applyOrElse(x, (_: A) => None)
        b.exists(k.isDefinedAt)
      }

      def apply(x: A): C = k(pf(x))

      override def applyOrElse[A2 <: A, C1 >: C](x: A2, default: A2 => C1): C1 = {
        val pfv = pf.andThen[Option[B]](Some(_)).applyOrElse(x, (_: A2) => None)
        pfv.map(v => k.applyOrElse(v, (_: B) => default(x))).getOrElse(default(x))
      }
    }
  }
}
