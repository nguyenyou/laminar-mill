package www

import io.github.nguyenyou.laminar.api.L
import www.components.popover.*
import www.components.tooltip.*
import www.floating.Flip
import www.ui.{view, div, onClick, height, ui}
import io.github.nguyenyou.laminar.api.eventPropToProcessor

def App() =
  view {
    div {
      ui("Hello, World!")
    }
    div {
      div {
        height.px := 100
        onClick --> { event => println(event) }
      }
      div {}
    }
    div {}
  }
