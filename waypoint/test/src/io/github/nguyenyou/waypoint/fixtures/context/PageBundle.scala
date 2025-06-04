package io.github.nguyenyou.waypoint.fixtures.context

import io.github.nguyenyou.waypoint.fixtures.AppPage
import upickle.default._

case class PageBundle(page: AppPage, context: SharedParams)

object PageBundle {

  implicit val rw: ReadWriter[PageBundle] = macroRW[PageBundle]
}
