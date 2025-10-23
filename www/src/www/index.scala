package www

import org.scalajs.dom
import io.github.nguyenyou.laminar.api.L.*
import www.examples.floatingui.*

@main def main(): Unit = {

  render(
    dom.document.getElementById("app"),
    AutoUpdateExample.demo()
  )
}
