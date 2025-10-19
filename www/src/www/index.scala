package www

import org.scalajs.dom
import io.github.nguyenyou.laminar.api.L.*

@main def main(): Unit = {
  val counterVar = Var(0)

  render(
    dom.document.getElementById("app"),
    div(
      button("-1", onClick --> Observer { _ => counterVar.update(_ - 1) }),
      div(text <-- counterVar),
      button("+1", onClick --> counterVar.updater((curr, _) => curr + 1))
    )
  )
}
