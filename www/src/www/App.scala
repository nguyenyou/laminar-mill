package www

import io.github.nguyenyou.laminar.api.L.*
import www.components.*

case class App() {
  val openVar = Var(false)
  val store = Popover.Store(
    openVar.signal,
    openVar.writer
  )

  def apply() = {
    div(
      Popover.Root() {
        Popover.Trigger(
          button("Green")
        )
        Popover.Content(
          div(
            width.percent(100),
            height.percent(100),
            backgroundColor.green
          )
        )
      },
      Popover.Root() {
        Popover.Trigger(
          button("Red")
        )
        Popover.Content(
          div(
            width.percent(100),
            height.percent(100),
            backgroundColor.red
          )
        )
      },
      Popover.Root(store) {
        Popover.Trigger(
          button("Blue")
        )
        Popover.Content(
          div(
            width.percent(100),
            height.percent(100),
            backgroundColor.blue
          )
        )
      }
    )
  }
}
