package www

import io.github.nguyenyou.laminar.api.L.*

case class App() {
  val countVar = Var(0)
  val countSignal = countVar.signal.distinct

  def apply() = {
    div(
      button("+", onClick --> Observer { _ =>
        countVar.update(_ + 1)
      }),
      span(
        text <-- countSignal
      ),
      button("-", onClick --> Observer { _ =>
        countVar.update(_ - 1)
      }),
    )
  }
}
