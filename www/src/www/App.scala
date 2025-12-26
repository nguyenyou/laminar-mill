package www

import www.components.popover.*
import www.components.tooltip.*
import www.floating.Flip
import www.ui.{div, onClick, height, str, ParentScope, Parent}
import io.github.nguyenyou.laminar.api.eventPropToProcessor

def App() =
  // Render methods can be nested inside App to demonstrate local component patterns

  // Simple render method - no content, just returns a styled div
  def renderHeader(text: String)(using parentScope: Option[Parent] = None) =
    div:
      height.px := 50
      str(text)

  // Render method with content - forwards parent context for nested DSL
  def renderCard(content: ParentScope)(using parentScope: Option[Parent] = None) =
    div:
      height.px := 200
      content

  // Render method that composes other render methods
  def renderSection(title: String)(content: ParentScope)(using parentScope: Option[Parent] = None) =
    div:
      renderHeader(title)
      renderCard:
        content

  // Render method with no parameters
  def renderDivider()(using parentScope: Option[Parent] = None) =
    div:
      height.px := 2

  // Main app structure using the render methods
  div:
    renderHeader("Welcome!")
    renderCard:
      str("I'm inside a card!")
    renderDivider()
    renderSection("My Section"):
      str("Content inside a section")
    div:
      str("Regular div still works")
      onClick --> { event => println(event) }
