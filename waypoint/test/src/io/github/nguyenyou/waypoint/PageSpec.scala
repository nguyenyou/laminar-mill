package io.github.nguyenyou.waypoint

import io.github.nguyenyou.waypoint.fixtures.{AppPage, UnitSpec}
import io.github.nguyenyou.waypoint.fixtures.AppPage.{HomePage, TextPage}
import upickle.default._

class PageSpec extends UnitSpec {

  it("read write") {

    write(HomePage) shouldBe "\"io.github.nguyenyou.waypoint.fixtures.AppPage.HomePage\""

    write[AppPage](TextPage("abc123")) shouldBe "{\"$type\":\"io.github.nguyenyou.waypoint.fixtures.AppPage.TextPage\",\"text\":\"abc123\"}"

    write[AppPage](HomePage)(using AppPage.rw) shouldBe "\"io.github.nguyenyou.waypoint.fixtures.AppPage.HomePage\""

  }
}
