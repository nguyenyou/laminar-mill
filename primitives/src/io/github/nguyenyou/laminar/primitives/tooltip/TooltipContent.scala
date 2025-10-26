package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.api.L
import io.github.nguyenyou.laminar.nodes.DetachedRoot
import org.scalajs.dom

class TooltipContent(val content: HtmlElement, val root: TooltipRoot) {
  private var mounted = false

  val portal = div(
    dataAttr("slot") := "tooltip-content-portal",
    position.fixed,
    width.px(400),
    height.px(400),
    cls("hidden"),
    content
  )

  private val portalRoot: DetachedRoot[Div] = renderDetached(
    portal,
    activateNow = false
  )

  def mount() = {
    if (!mounted) {
      mounted = true
      dom.document.body.appendChild(portalRoot.ref)
      portalRoot.activate()
    }
  }

  def unmount() = {
    if (mounted) {
      mounted = false
      portalRoot.deactivate()
      dom.document.body.removeChild(portalRoot.ref)
    }
  }
}
