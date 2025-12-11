package www.ui

import io.github.nguyenyou.laminar.api.L

// The scope that provides access to the current parent element
class View(val parent: L.HtmlElement)

// Entry point - creates a root and provides scope to children
def view(content: View ?=> Unit): L.HtmlElement = {
  val root = L.div(
    L.dataAttr("name") := "view"
  )
  given View = View(root)
  content // Children will receive root as parent
  root
}

// div creates element, runs children (who amend to it), then amends itself to parent
def div(content: View ?=> Unit)(using scope: View): L.HtmlElement = {
  val element = L.div(
    L.dataAttr("name") := "div"
  )
  scope.parent.amend(element) // This element amends to its parent
  given View = View(element)
  content // Children amend to this element
  element
}
