package www.floating

import io.github.nguyenyou.laminar.api.L.*

def Flip() = {
  val referenceEle = div(
    className("reference"),
    "Reference"
  )
  val floatingEle = div(
    className("floating"),
    "Floating"
  )

  div(
    h1("Flip"),
    p(),
    div(
      className("container"),
      div(
        className("scroll"),
        position.relative,
        referenceEle,
        floatingEle
      )
    )
  )
}
