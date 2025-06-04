package io.github.nguyenyou.laminar.nodes

import io.github.nguyenyou.laminar.DomApi
import org.scalajs.dom

class CommentNode(initialText: String) extends ChildNode[dom.Comment] {

  final override val ref: dom.Comment = DomApi.createCommentNode(initialText)

  final def text: String = ref.data
}
