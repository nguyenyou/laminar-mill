package www.floating

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.floatingUI.FloatingUI.computePosition
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.middleware.FlipMiddleware
import io.github.nguyenyou.floatingUI.middleware.ShiftMiddleware
import org.scalajs.dom

def Flip() = {
  val scrollRef = Var[Option[HtmlElement]](None)
  val referenceRef = Var[Option[HtmlElement]](None)
  val floatingRef = Var[Option[HtmlElement]](None)

  div(
    h1("Flip"),
    p(),
    div(
      className := "container",
      div(
        onMountCallback { ctx => scrollRef.set(Some(ctx.thisNode)) },
        className := "scroll",
        dataAttr("x") := "",
        position := "relative",
        // Reference element
        div(
          onMountCallback { ctx => referenceRef.set(Some(ctx.thisNode)) },
          className := "reference",
          "Reference"
        ),
        // Floating element
        div(
          onMountCallback { ctx => floatingRef.set(Some(ctx.thisNode)) },
          className := "floating",
          "Floating"
        )
      )
    )
  )
}
