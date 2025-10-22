package www.components

import io.github.nguyenyou.laminar.api.L.*

case class PopoverTrigger() {
  def apply() = {
    button("Click me")
  }
}
