package www

import org.scalajs.dom
import io.github.nguyenyou.laminar.api.L
import www.ui.View
// import www.examples.floatingui.*

@main def main(): Unit = {
  L.render(
    dom.document.getElementById("app"),
    // BasicTooltipExample.simpleTooltip()
    App()()
  )
}
