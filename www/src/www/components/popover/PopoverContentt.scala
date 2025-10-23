package www.components.popover

import io.github.nguyenyou.laminar.api.L.*
import www.components.popover.PopoverRoot

object PopoverContent {
  def apply(content: HtmlElement)(using root: PopoverRoot) = {
    root.setContent(content)
  }

  enum Side {
    case Top, Bottom, Left, Right
  }

  object Side {
    type Selector = Side.type => Side
  }

  def apply(side: Side)(content: HtmlElement)(using root: PopoverRoot) = {
    root.setContent(content)
  }

  def apply(side: Side.Selector)(content: HtmlElement)(using root: PopoverRoot) = {
    root.setContent(content)
  }
}
