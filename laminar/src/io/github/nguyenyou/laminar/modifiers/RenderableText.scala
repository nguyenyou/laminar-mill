package io.github.nguyenyou.laminar.modifiers

import io.github.nguyenyou.laminar.nodes.TextNode

import scala.annotation.implicitNotFound

/** `RenderableText[A]` is evidence that you can convert a value of type A to a string for the purpose of rendering it as a TextNode.
  *
  * If you have an given of RenderableText[A], Laminar can render your `A` type values by converting them to strings (and ultimately into
  * [[io.github.nguyenyou.laminar.nodes.TextNode]]), and will accept your values as a valid [[Modifier]], and in `child.text <--`.
  *
  * See also – [[RenderableNode]]
  */
@implicitNotFound(
  "Implicit instance of RenderableText[${TextLike}] not found. If you want to render `${TextLike}` as a string, define an implicit `RenderableText[${TextLike}]` instance for it."
)
trait RenderableText[-TextLike] {

  // override this default value, it's only here to prevent this from qualifying as a SAM trait
  def asString(value: TextLike): String = ""
}

object RenderableText {

  def apply[A](render: A => String): RenderableText[A] = new RenderableText[A] {
    override def asString(value: A): String = render(value)
  }

  // #Note[API] if you want Renderable[TextNode], please let me know why.

  given stringRenderable: RenderableText[String] = RenderableText[String](identity)

  given intRenderable: RenderableText[Int] = RenderableText[Int](_.toString)

  given doubleRenderable: RenderableText[Double] = RenderableText[Double](_.toString)

  given boolRenderable: RenderableText[Boolean] = RenderableText[Boolean](_.toString)

  // --

  /** #Warning: Using this naively in ChildTextInserter is not efficient. When we encounter this renderable instance, we use ChildInserter
    * instead.
    */
  given textNodeRenderable: RenderableText[TextNode] = RenderableText[TextNode](_.text)

  // --

  given charRenderable: RenderableText[Char] = RenderableText[Char](_.toString)

  given byteRenderable: RenderableText[Byte] = RenderableText[Byte](_.toString)

  given shortRenderable: RenderableText[Short] = RenderableText[Short](_.toString)

  given longRenderable: RenderableText[Long] = RenderableText[Long](_.toString)

  given floatRenderable: RenderableText[Float] = RenderableText[Float](_.toString)
}
