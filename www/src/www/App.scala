package www

import io.github.nguyenyou.laminar.api.L.*
import www.components.*

case class App() {
  val countVar = Var(0)
  val countSignal = countVar.signal.distinct

  def apply() = {
    div(
      Popover {
        Popover.Trigger(
          button("Click me!")
        )
        Popover.Content(
          div(
            width.percent(100),
            height.percent(100),
            backgroundColor.green
          )
        )
      },
      Popover {
        Popover.Trigger(
          button("Click meeeee!")
        )
        Popover.Content(
          div(
            width.percent(100),
            height.percent(100),
            backgroundColor.red
          )
        )
      }
    )
  }
}
