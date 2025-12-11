package www

import io.github.nguyenyou.laminar.api.L
import www.components.popover.*
import www.components.tooltip.*
import www.floating.Flip
import www.ui.{view, div}

case class App() {
  // val v = view {
  //   div {
  //     div {

  //     }
  //   }
  //   div {
  //     div {}
  //     div {}
  //   }
  // }
  def apply() = {
    view {
      div {}
      div {
        div {}
        div {}
      }
      div {}
    }
    // L.div(
    //   L.mainTag(
    //     Flip()
    //   ),
    //   L.navTag()
    // )
  }
}
