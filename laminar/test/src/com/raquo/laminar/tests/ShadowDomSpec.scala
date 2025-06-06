package io.github.nguyenyou.laminar.tests

import com.raquo.domtestutils.matching.ExpectedNode
import io.github.nguyenyou.laminar.DomApi
import io.github.nguyenyou.laminar.api.L._
import io.github.nguyenyou.laminar.nodes.ChildNode
import io.github.nguyenyou.laminar.utils.UnitSpec
import org.scalajs.dom

import scala.scalajs.js

class ShadowDomSpec extends UnitSpec {

  // @TODO This would be nicer if someone contributed Shadow DOM support to scala-js-dom
  //  - https://github.com/scala-js/scala-js-dom/issues/191

  it("Laminar can render into shadow root") {

    val modes = List("open", "closed")

    modes.foreach { mode =>
      withClue(s"MODE=${mode}") {
        val parentDiv = div().ref

        val parentDivDyn = parentDiv.asInstanceOf[js.Dynamic]
        val shadowRoot = parentDivDyn.attachShadow(js.Dynamic.literal("mode" -> mode)).asInstanceOf[dom.Node]

        val childDiv = div().ref
        shadowRoot.appendChild(childDiv)

        assert(shadowRoot.parentNode == null)
        assert(shadowRoot.asInstanceOf[js.Dynamic].host.asInstanceOf[Any] == parentDiv)

        assert(DomApi.isDescendantOf(childDiv, parentDiv))
        assert(DomApi.isDescendantOf(childDiv, shadowRoot))
        assert(!DomApi.isDescendantOf(childDiv, dom.document))

        // --

        dom.document.body.appendChild(parentDiv)

        assert(DomApi.isDescendantOf(childDiv, dom.document))

        // --

        val bus = new EventBus[String]

        val app = div(
          "Hello, ",
          span(child.text <-- bus.events)
        )

        val laminarRoot = render(container = childDiv, app)

        assert(app.ref.parentNode == childDiv)
        assert(DomApi.isDescendantOf(app.ref, parentDiv))
        assert(DomApi.isDescendantOf(app.ref, shadowRoot))
        assert(DomApi.isDescendantOf(app.ref, dom.document))

        expectNode(
          app.ref,
          div.of(
            "Hello, ",
            span.of(sentinel)
          )
        )

        // --

        bus.writer.onNext("world")

        expectNode(
          app.ref,
          div.of(
            "Hello, ",
            span.of("world")
          )
        )

        // --

        bus.writer.onNext("you")

        expectNode(
          app.ref,
          div.of(
            "Hello, ",
            span.of("you")
          )
        )

        // --

        dom.document.body.removeChild(parentDiv)

        assert(DomApi.isDescendantOf(app.ref, parentDiv))
        assert(DomApi.isDescendantOf(app.ref, shadowRoot))
        assert(!DomApi.isDescendantOf(app.ref, dom.document))

        // --

        laminarRoot.unmount()

        assert(app.ref.parentNode == null)
        assert(!DomApi.isDescendantOf(app.ref, parentDiv))
        assert(!DomApi.isDescendantOf(app.ref, shadowRoot))
        assert(!DomApi.isDescendantOf(app.ref, dom.document))
      }
    }
  }
}
