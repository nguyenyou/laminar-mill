package io.github.nguyenyou.laminar.nodes

import io.github.nguyenyou.laminar.DomApi
import org.scalajs.dom

class TextNode(initialText: String) extends ChildNode[dom.Text] {

  final override val ref: dom.Text = DomApi.createTextNode(initialText)

  final def text: String = ref.data
}
