package www.ui

import scala.language.implicitConversions
import io.github.nguyenyou.laminar.api.L
import io.github.nguyenyou.laminar.api.eventPropToProcessor
import org.scalajs.dom
import io.github.nguyenyou.laminar.api.textToTextNode

// The scope that provides access to the current parent element
class View(val parent: L.HtmlElement)

// div creates element, runs children (who amend to it), then amends itself to parent
// Uses Option[View] with default None so it can be used at top-level without a parent
def div(content: View ?=> Unit)(using parentScope: Option[View] = None): L.HtmlElement = {
  val element = L.div(
    L.dataAttr("name") := "div"
  )
  // Only amend to parent if we have one
  parentScope.foreach(_.parent.amend(element))
  // Provide scope for content block and nested div calls
  given View = View(element)
  given Option[View] = Some(View(element))
  content
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
