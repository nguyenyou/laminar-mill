package www

import www.components.popover.*
import www.components.tooltip.*
import www.floating.Flip
import www.ui.{div, onClick, height, str}
import io.github.nguyenyou.laminar.api.eventPropToProcessor

def App() =
  div:
    div:
      str("Hello, Worlddd!")
    div:
      div:
        height.px := 100
        onClick --> { event => println(event) }
