package io.github.nguyenyou.floatingUI.middleware

import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.DetectOverflow

/** Hide middleware - provides data to hide floating element when clipped.
  *
  * Ported from @floating-ui/core/src/middleware/hide.ts
  */
object HideMiddleware {

  /** Get side offsets from overflow and rect. */
  private def getSideOffsets(overflow: SideObject, rect: Rect): SideObject = {
    SideObject(
      top = overflow.top - rect.height,
      right = overflow.right - rect.width,
      bottom = overflow.bottom - rect.height,
      left = overflow.left - rect.width
    )
  }

  /** Check if any side is fully clipped. */
  private def isAnySideFullyClipped(overflow: SideObject): Boolean = {
    overflow.top >= 0 || overflow.right >= 0 || overflow.bottom >= 0 || overflow.left >= 0
  }

  /** Create hide middleware. */
  def hide(options: HideOptions = HideOptions()): Middleware = new Middleware {
    override def name: String = "hide"

    override def fn(state: MiddlewareState): MiddlewareReturn = {
      val strategy = options.strategy

      strategy match {
        case "referenceHidden" =>
          val detectOverflowOptions = DetectOverflowOptions(
            boundary = options.boundary,
            rootBoundary = options.rootBoundary,
            padding = options.padding,
            elementContext = "reference"
          )

          val overflow = DetectOverflow.detectOverflow(state, detectOverflowOptions)
          val offsets = getSideOffsets(overflow, state.rects.reference)

          MiddlewareReturn(
            data = Some(
              Map(
                "referenceHiddenOffsets" -> offsets,
                "referenceHidden" -> isAnySideFullyClipped(offsets)
              )
            )
          )

        case "escaped" =>
          val detectOverflowOptions = DetectOverflowOptions(
            boundary = options.boundary,
            rootBoundary = options.rootBoundary,
            padding = options.padding,
            altBoundary = true
          )

          val overflow = DetectOverflow.detectOverflow(state, detectOverflowOptions)
          val offsets = getSideOffsets(overflow, state.rects.floating)

          MiddlewareReturn(
            data = Some(
              Map(
                "escapedOffsets" -> offsets,
                "escaped" -> isAnySideFullyClipped(offsets)
              )
            )
          )

        case _ =>
          MiddlewareReturn()
      }
    }
  }
}
