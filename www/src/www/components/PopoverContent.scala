package www.components

import io.github.nguyenyou.laminar.api.L.*

object PopoverContent {
  def apply(content: HtmlElement)(using root: PopoverRoot) = {
    root.setContent(content)
  }
}
