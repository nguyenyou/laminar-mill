package www

import org.scalajs.dom
import io.github.nguyenyou.laminar.api.L.*

@main def main(): Unit = {
  val counterVar = Var(0)

  render(
    dom.document.getElementById("app"),
    App()()
  )
}
