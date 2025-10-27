package io.github.nguyenyou.floatingUI.middleware

import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.Utils.*

/** Inline middleware - improved positioning for inline elements.
  *
  * Ported from @floating-ui/core/src/middleware/inline.ts
  */
object InlineMiddleware {

  /** Get bounding rect from multiple client rects. */
  private def getBoundingRect(rects: Seq[ClientRectObject]): ClientRectObject = {
    if (rects.isEmpty) {
      return ClientRectObject(0, 0, 0, 0, 0, 0, 0, 0)
    }

    val minX = rects.map(_.left).min
    val minY = rects.map(_.top).min
    val maxX = rects.map(_.right).max
    val maxY = rects.map(_.bottom).max

    ClientRectObject(
      x = minX,
      y = minY,
      width = maxX - minX,
      height = maxY - minY,
      top = minY,
      right = maxX,
      bottom = maxY,
      left = minX
    )
  }

  /** Group rects by line. */
  private def getRectsByLine(rects: Seq[ClientRectObject]): Seq[ClientRectObject] = {
    if (rects.isEmpty) return Seq.empty

    val sortedRects = rects.sortBy(_.y)
    var groups: Seq[Seq[ClientRectObject]] = Seq.empty
    var prevRect: Option[ClientRectObject] = None

    sortedRects.foreach { rect =>
      prevRect match {
        case None =>
          groups = groups :+ Seq(rect)
        case Some(prev) =>
          if (rect.y - prev.y > prev.height / 2) {
            groups = groups :+ Seq(rect)
          } else {
            groups = groups.init :+ (groups.last :+ rect)
          }
      }
      prevRect = Some(rect)
    }

    groups.map(group =>
      rectToClientRect(
        Rect(
          getBoundingRect(group).x,
          getBoundingRect(group).y,
          getBoundingRect(group).width,
          getBoundingRect(group).height
        )
      )
    )
  }

  /** Create inline middleware. */
  def inline(options: InlineOptions = InlineOptions()): Middleware = new Middleware {
    override def name: String = "inline"

    override def fn(state: MiddlewareState): MiddlewareReturn = {
      val placement = state.placement
      val elements = state.elements
      val rects = state.rects
      val strategy = state.strategy

      // Default padding is 2 pixels (handles MouseEvent client coords being up to 2px off)
      // Evaluate derivable values
      val padding = evaluate(options.padding, state) match {
        case p: Double => p
        case _         => 2.0
      }
      val paddingObject = getPaddingObject(padding)
      val x = options.x.map(evaluate(_, state))
      val y = options.y.map(evaluate(_, state))

      // Get client rects for the reference element
      val nativeClientRects = state.platform.getClientRects(elements.reference)
      val clientRects = getRectsByLine(nativeClientRects)
      val fallback = getBoundingRect(nativeClientRects)

      // Determine the best bounding rect based on placement and options
      val bestRect: ClientRectObject = {
        // Two disjoined rects with x,y coordinates provided
        if (
          clientRects.length == 2 &&
          clientRects(0).left > clientRects(1).right &&
          x.isDefined && y.isDefined
        ) {
          val xVal = x.get
          val yVal = y.get

          // Find rect that contains the point
          clientRects
            .find { rect =>
              xVal > rect.left - paddingObject.left &&
              xVal < rect.right + paddingObject.right &&
              yVal > rect.top - paddingObject.top &&
              yVal < rect.bottom + paddingObject.bottom
            }
            .getOrElse(fallback)
        }
        // Two or more connected rects
        else if (clientRects.length >= 2) {
          if (getSideAxis(placement) == "y") {
            val firstRect = clientRects.head
            val lastRect = clientRects.last
            val isTop = getSide(placement) == "top"

            val top = firstRect.top
            val bottom = lastRect.bottom
            val left = if (isTop) firstRect.left else lastRect.left
            val right = if (isTop) firstRect.right else lastRect.right
            val width = right - left
            val height = bottom - top

            ClientRectObject(
              top = top,
              bottom = bottom,
              left = left,
              right = right,
              width = width,
              height = height,
              x = left,
              y = top
            )
          } else {
            val isLeftSide = getSide(placement) == "left"
            val maxRight = clientRects.map(_.right).max
            val minLeft = clientRects.map(_.left).min
            val measureRects = clientRects.filter { rect =>
              if (isLeftSide) rect.left == minLeft else rect.right == maxRight
            }

            val top = measureRects.head.top
            val bottom = measureRects.last.bottom
            val left = minLeft
            val right = maxRight
            val width = right - left
            val height = bottom - top

            ClientRectObject(
              top = top,
              bottom = bottom,
              left = left,
              right = right,
              width = width,
              height = height,
              x = left,
              y = top
            )
          }
        } else {
          fallback
        }
      }

      // Convert to Rect
      val newReferenceRect = Rect(bestRect.x, bestRect.y, bestRect.width, bestRect.height)

      // Check if the reference rect has changed
      if (
        rects.reference.x != newReferenceRect.x ||
        rects.reference.y != newReferenceRect.y ||
        rects.reference.width != newReferenceRect.width ||
        rects.reference.height != newReferenceRect.height
      ) {

        // Return reset with new rects
        val newRects = ElementRects(
          reference = newReferenceRect,
          floating = rects.floating
        )

        MiddlewareReturn(
          reset = Some(
            ResetValue(
              rects = Some(Right(newRects))
            )
          )
        )
      } else {
        MiddlewareReturn()
      }
    }
  }
}
