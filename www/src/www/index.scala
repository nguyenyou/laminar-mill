package www

import org.scalajs.dom
import io.github.nguyenyou.laminar.api.L.*

@main def main(): Unit = {
  render(dom.document.getElementById("app"), App()())
}
  
