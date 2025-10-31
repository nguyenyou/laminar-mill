package io.github.nguyenyou.floatingUI.middleware

import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.DetectOverflow
import io.github.nguyenyou.floatingUI.Utils.evaluate

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
  def hide(options: Derivable[HideOptions] = Left(HideOptions())): Middleware = new Middleware {
    override def name: String = "hide"

    override def fn(state: MiddlewareState): MiddlewareReturn = {
      // Evaluate derivable options
      val evaluatedOptions = evaluate(options, state)

      // Extract strategy and detect overflow options
      val strategy = evaluatedOptions.strategy

      strategy match {
        case HideStrategy.ReferenceHidden =>
          // Spread all DetectOverflowOptions and override elementContext
          val detectOverflowOptions = DetectOverflowOptions(
            boundary = evaluatedOptions.boundary,
            rootBoundary = evaluatedOptions.rootBoundary,
            elementContext = ElementContext.Reference, // Override to Reference
            altBoundary = evaluatedOptions.altBoundary,
            padding = evaluatedOptions.padding
          )

          val overflow = DetectOverflow.detectOverflow(state, Left(detectOverflowOptions))
          val offsets = getSideOffsets(overflow, state.rects.reference)

          MiddlewareReturn(
            data = Some(
              Map(
                "referenceHiddenOffsets" -> offsets,
                "referenceHidden" -> isAnySideFullyClipped(offsets)
              )
            ),
            reset = None
          )

        case HideStrategy.Escaped =>
          // Spread all DetectOverflowOptions and override altBoundary
          val detectOverflowOptions = DetectOverflowOptions(
            boundary = evaluatedOptions.boundary,
            rootBoundary = evaluatedOptions.rootBoundary,
            elementContext = evaluatedOptions.elementContext,
            altBoundary = true, // Override to true
            padding = evaluatedOptions.padding
          )

          val overflow = DetectOverflow.detectOverflow(state, Left(detectOverflowOptions))
          val offsets = getSideOffsets(overflow, state.rects.floating)

          MiddlewareReturn(
            data = Some(
              Map(
                "escapedOffsets" -> offsets,
                "escaped" -> isAnySideFullyClipped(offsets)
              )
            ),
            reset = None
          )

        case _ =>
          MiddlewareReturn(reset = None)
      }
    }
  }
}
