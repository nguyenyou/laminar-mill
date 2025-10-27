package www

import io.github.nguyenyou.laminar.api.L.*
import www.components.popover.*
import www.components.tooltip.*
import www.floating.Flip

case class App() {
  def apply() = {
    div(
      mainTag(
        Flip()
      ),
      navTag()
    )
  }
}
