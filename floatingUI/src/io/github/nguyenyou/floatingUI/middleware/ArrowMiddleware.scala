package io.github.nguyenyou.floatingUI.middleware

import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.Utils.*
import org.scalajs.dom
import scala.math.min as mathMin

/** Arrow middleware - positions an arrow element.
  *
  * Ported from @floating-ui/core/src/middleware/arrow.ts
  */
object ArrowMiddleware {

  def arrow(options: Derivable[ArrowOptions]): Middleware = new Middleware {
    override def name: String = "arrow"

    override def fn(state: MiddlewareState): MiddlewareReturn = {
      // Evaluate derivable options
      val evaluatedOptions = evaluate(options, state)

      // Early return if element is null (shouldn't happen with Scala's type system, but for safety)
      if (evaluatedOptions == null || evaluatedOptions.element == null) {
        return MiddlewareReturn(reset = None)
      }

      val element = evaluatedOptions.element
      val padding = evaluate(evaluatedOptions.padding, state)
      val paddingObject = getPaddingObject(padding)
      val coords = Coords(state.x, state.y)
      val axis = getAlignmentAxis(state.placement)
      val length = getAxisLength(axis)
      val arrowDimensions = state.platform.getDimensions(element)
      val isYAxis = axis == "y"
      val minProp = if (isYAxis) "top" else "left"
      val maxProp = if (isYAxis) "bottom" else "right"
      val clientProp = if (isYAxis) "clientHeight" else "clientWidth"

      val refLength = if (length == "width") state.rects.reference.width else state.rects.reference.height
      val refAxis = if (axis == "x") state.rects.reference.x else state.rects.reference.y
      val coordsAxis = if (axis == "x") coords.x else coords.y
      val floatLength = if (length == "width") state.rects.floating.width else state.rects.floating.height
      val arrowLength = if (length == "width") arrowDimensions.width else arrowDimensions.height

      val endDiff = refLength + refAxis - coordsAxis - floatLength
      val startDiff = coordsAxis - refAxis

      // Get the arrow's offset parent and calculate client size
      val arrowOffsetParent = state.platform.getOffsetParent(element)
      var clientSize = arrowOffsetParent
        .flatMap { parent =>
          parent match {
            case htmlElement: dom.HTMLElement =>
              // Get the client size from the offset parent
              if (clientProp == "clientHeight") {
                Some(htmlElement.clientHeight.toDouble)
              } else {
                Some(htmlElement.clientWidth.toDouble)
              }
            case _ => None
          }
        }
        .getOrElse(0.0)

      // DOM platform can return `window` as the `offsetParent`.
      // If clientSize is 0 or the offsetParent is not an element, use floating element's size
      val isOffsetParentElement = arrowOffsetParent.flatMap(state.platform.isElement).getOrElse(false)
      if (clientSize == 0 || !isOffsetParentElement) {
        // state.elements.floating is always an HTMLElement
        if (clientProp == "clientHeight") {
          clientSize = state.elements.floating.clientHeight.toDouble
        } else {
          clientSize = state.elements.floating.clientWidth.toDouble
        }
      }

      val centerToReference = endDiff / 2 - startDiff / 2

      // If the padding is large enough that it causes the arrow to no longer be
      // centered, modify the padding so that it is centered.
      val largestPossiblePadding = clientSize / 2 - arrowLength / 2 - 1
      val minPadding = mathMin(
        if (minProp == "top") paddingObject.top else paddingObject.left,
        largestPossiblePadding
      )
      val maxPadding = mathMin(
        if (maxProp == "bottom") paddingObject.bottom else paddingObject.right,
        largestPossiblePadding
      )

      // Make sure the arrow doesn't overflow the floating element if the center
      // point is outside the floating element's bounds.
      val min = minPadding
      val max = clientSize - arrowLength - maxPadding
      val center = clientSize / 2 - arrowLength / 2 + centerToReference
      val offset = clamp(min, center, max)

      // If the reference is small enough that the arrow's padding causes it to
      // to point to nothing for an aligned placement, adjust the offset of the
      // floating element itself.
      val shouldAddOffset =
        state.middlewareData.arrow.isEmpty &&
          getAlignment(state.placement).isDefined &&
          center != offset &&
          refLength / 2 - (if (center < min) minPadding else maxPadding) - arrowLength / 2 < 0

      val alignmentOffset = if (shouldAddOffset) {
        if (center < min) center - min else center - max
      } else {
        0
      }

      val newCoords = if (axis == "x") {
        Coords(x = coords.x + alignmentOffset, y = coords.y)
      } else {
        Coords(x = coords.x, y = coords.y + alignmentOffset)
      }

      val arrowData = Map(
        axis -> offset,
        "centerOffset" -> (center - offset - alignmentOffset)
      ) ++ (if (shouldAddOffset) Map("alignmentOffset" -> alignmentOffset) else Map.empty)

      MiddlewareReturn(
        x = Some(newCoords.x),
        y = Some(newCoords.y),
        data = Some(arrowData),
        reset = if (shouldAddOffset) Some(Left(true)) else None // Boolean reset
      )
    }
  }
}
