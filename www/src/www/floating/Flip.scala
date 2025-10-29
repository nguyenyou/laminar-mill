package www.floating

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.floatingUI.FloatingUI.computePosition
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.middleware.FlipMiddleware
import io.github.nguyenyou.floatingUI.middleware.ShiftMiddleware
import org.scalajs.dom

def Flip() = {

  div(
    h1("Flip"),
    p(),
    div(
      className := "container",
      div(
        className := "scroll",
        dataAttr("x") := "",
        position := "relative",
        // Reference element
        div(
          className := "reference",
          "Reference"
        ),
        // Floating element
        div(
          className := "floating",
          "Floating"
        )
      )
    )
  )
}
