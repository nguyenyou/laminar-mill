package www

import io.github.nguyenyou.laminar.api.L.*
import www.components.*

case class App() {
  val openVar = Var(false)
  val store = Popover.Store(
    openVar.signal,
    openVar.writer
  )

  val openVar2 = Var(false)

  def apply() = {
    div(
      Popover() {
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
      Popover() {
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
      Popover(store) {
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
      },
      Popover(
        openVar2.signal,
        openVar2.writer
      ) {
        Popover.Trigger { store =>
          button(
            onClick(_.sample(store.openSignal)) --> Observer[Boolean] { open =>
              store.onChangeOpen.onNext(!open)
            },
            "Yellow"
          )
        }
        Popover.Content(
          div(
            width.percent(100),
            height.percent(100),
            backgroundColor.yellow
          )
        )
      }
    )
  }
}
