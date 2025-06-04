package io.github.nguyenyou.laminar.tests.basic

import io.github.nguyenyou.laminar.api.L._
import io.github.nguyenyou.laminar.utils.UnitSpec
import org.scalajs.dom

class RefSpec extends UnitSpec {

  it("creates ref right away") {
    val node = div()
    node.ref.isInstanceOf[dom.Element] shouldBe true
    node.ref.parentNode shouldBe null

    mount(node)
    node.ref.parentNode shouldBe containerNode

    unmount()
    node.ref.parentNode shouldBe null
  }
}
