package www

import io.github.nguyenyou.laminar.api.L.*
import www.components.popover.{Popover}

case class App() {
  val openVar = Var(false)
  // val store = PopoverStore(
  //   openVar.signal,
  //   openVar.writer
  // )

  val openVar2 = Var(false)

  // val sideVar = Var(PopoverContent.Side.Top)

  def apply() = {
    div(
      // button(
      //   onClick --> Observer { _ =>
      //     sideVar.set(PopoverContent.Side.Top)
      //   },
      //   "Set Top"
      // ),
      // button(
      //   onClick --> Observer { _ =>
      //     sideVar.set(PopoverContent.Side.Bottom)
      //   },
      //   "Set Bottom"
      // ),
      Popover() {
        Popover.Trigger()("Green")
        // PopoverTrigger()(
        //   button("Green")
        // )
        Popover.Content(
          _.side.bottom
        )(
          div(
            width.percent(100),
            height.percent(100),
            backgroundColor.green
          )
        )
      }
      // Popover() {
      //   PopoverTrigger()(
      //     button("Red")
      //   )
      //   PopoverContent(
      //     _.side.top
      //   )(
      //     div(
      //       width.percent(100),
      //       height.percent(100),
      //       backgroundColor.red
      //     )
      //   )
      // },
      // Popover() {
      //   PopoverTrigger()(
      //     button("Yellow")
      //   )
      //   PopoverContent(
      //     _.side <-- sideVar
      //   )(
      //     div(
      //       width.percent(100),
      //       height.percent(100),
      //       backgroundColor.yellow
      //     )
      //   )
      // }
      // Popover() {
      //   Popover.Trigger(
      //     button("Red")
      //   )
      //   Popover.Content(
      //     div(
      //       width.percent(100),
      //       height.percent(100),
      //       backgroundColor.red
      //     )
      //   )
      // },
      // Popover(store) {
      //   Popover.Trigger(
      //     button("Blue")
      //   )
      //   Popover.Content(
      //     div(
      //       width.percent(100),
      //       height.percent(100),
      //       backgroundColor.blue
      //     )
      //   )
      // },
      // Popover(
      //   openVar2.signal,
      //   openVar2.writer
      // ) {
      //   Popover.Trigger { store =>
      //     button(
      //       onClick(_.sample(store.openSignal)) --> Observer[Boolean] { open =>
      //         store.onChangeOpen.onNext(!open)
      //       },
      //       "Yellow"
      //     )
      //   }
      //   Popover.Content(
      //     div(
      //       width.percent(100),
      //       height.percent(100),
      //       backgroundColor.yellow
      //     )
      //   )
      // }
    )
  }
}
