package www.floating

import io.github.nguyenyou.laminar.api.L.*

def Flip() = {
  div(
    h1("Flip"),
    p(),
    div(
      className("container"),
      div(
        className("scroll"),
        position.relative,
        div(
          className("reference"),
          "Reference"
        ),
        div(
          className("floating"),
          "Floating"
        )
      )
    )
  )
}
