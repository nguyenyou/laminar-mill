package www.ui

import scala.language.implicitConversions
import io.github.nguyenyou.laminar.api.L
import io.github.nguyenyou.laminar.api.eventPropToProcessor
import org.scalajs.dom
import io.github.nguyenyou.laminar.api.textToTextNode

// The scope that provides access to the current parent element
class Parent(val parent: L.HtmlElement)

// Type alias for context function that carries both Parent and Option[Parent]
type ParentScope = (Parent, Option[Parent]) ?=> Unit

// div creates element, runs children (who amend to it), then amends itself to parent
// Uses Option[Parent] with default None so it can be used at top-level without a parent
def div(content: ParentScope)(using parentScope: Option[Parent] = None): L.HtmlElement = {
  val element = L.div(
    L.dataAttr("name") := "div"
  )
  // Only amend to parent if we have one
  parentScope.foreach(_.parent.amend(element))
  // Provide scope for content block and nested div calls
  given Parent = Parent(element)
  given Option[Parent] = Some(Parent(element))
  content
  element
}

object onClick {
  def apply(f: () => Unit)(using scope: Parent) = {
    scope.parent.amend(
      L.onClick --> L.Observer { _ => f() }
    )
  }

  def -->(f: dom.MouseEvent => Unit)(using scope: Parent) = {
    scope.parent.amend(
      L.onClick --> L.Observer[dom.MouseEvent](f)
    )
  }
}

object height {
  def apply(value: String)(using scope: Parent) = {
    scope.parent.amend(
      L.height := value
    )
  }

  def :=(value: String)(using scope: Parent) = apply(value)

  object px {
    def apply(value: Int)(using scope: Parent) = {
      scope.parent.amend(
        L.height.px := value
      )
    }

    def :=(value: Int)(using scope: Parent) = apply(value)
  }

}

object ui {
  def apply(value: String)(using scope: Parent) = {
    scope.parent.amend(
      value
    )
  }
}
