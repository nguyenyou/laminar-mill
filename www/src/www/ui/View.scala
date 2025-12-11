package www.ui

import scala.language.implicitConversions
import io.github.nguyenyou.laminar.api.L
import io.github.nguyenyou.laminar.api.eventPropToProcessor
import org.scalajs.dom
import io.github.nguyenyou.laminar.api.textToTextNode

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

object onClick {
  def apply(f: () => Unit)(using scope: View) = {
    scope.parent.amend(
      L.onClick --> L.Observer { _ => f() }
    )
  }

  def -->(f: dom.MouseEvent => Unit)(using scope: View) = {
    scope.parent.amend(
      L.onClick --> L.Observer[dom.MouseEvent](f)
    )
  }
}

object height {
  def apply(value: String)(using scope: View) = {
    scope.parent.amend(
      L.height := value
    )
  }

  def :=(value: String)(using scope: View) = apply(value)

  object px {
    def apply(value: Int)(using scope: View) = {
      scope.parent.amend(
        L.height.px := value
      )
    }

    def :=(value: Int)(using scope: View) = apply(value)
  }

}

object ui {
  def apply(value: String)(using scope: View) = {
    scope.parent.amend(
      value
    )
  }
}
