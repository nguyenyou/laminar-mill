package io.github.nguyenyou.laminar.primitives.utils.floating.middleware

import io.github.nguyenyou.laminar.primitives.utils.floating.Types.*
import io.github.nguyenyou.laminar.primitives.utils.floating.Utils.*
import io.github.nguyenyou.laminar.primitives.utils.floating.DetectOverflow.*

/** Flip middleware - flips the placement to keep it in view.
  * 
  * Ported from @floating-ui/core/src/middleware/flip.ts
  */
object FlipMiddleware {
  
  def flip(options: FlipOptions = FlipOptions()): Middleware = new Middleware {
    override def name: String = "shift"
    
    override def fn(state: MiddlewareState): MiddlewareReturn = {
      // If arrow caused an alignment offset, skip flip logic
      if (state.middlewareData.arrow.flatMap(_.alignmentOffset).isDefined) {
        return MiddlewareReturn()
      }
      
      val side = getSide(state.placement)
      val isBasePlacement = getSide(state.initialPlacement) == state.initialPlacement
      
      val fallbackPlacements = options.fallbackPlacements.getOrElse {
        if (isBasePlacement) {
          Seq(getOppositePlacement(state.initialPlacement))
        } else {
          getExpandedPlacements(state.initialPlacement)
        }
      }
      
      val placements = state.initialPlacement +: fallbackPlacements
      
      val overflow = detectOverflow(
        state,
        DetectOverflowOptions(padding = options.padding)
      )
      
      val overflows = scala.collection.mutable.ArrayBuffer[Double]()
      
      if (options.mainAxis) {
        val sideValue = side match {
          case "top" => overflow.top
          case "bottom" => overflow.bottom
          case "left" => overflow.left
          case "right" => overflow.right
        }
        overflows += sideValue
      }
      
      if (options.crossAxis) {
        val sides = getAlignmentSides(state.placement, state.rects, rtl = false)
        val side1Value = sides._1 match {
          case "top" => overflow.top
          case "bottom" => overflow.bottom
          case "left" => overflow.left
          case "right" => overflow.right
        }
        val side2Value = sides._2 match {
          case "top" => overflow.top
          case "bottom" => overflow.bottom
          case "left" => overflow.left
          case "right" => overflow.right
        }
        overflows += side1Value
        overflows += side2Value
      }
      
      // Check if any side is overflowing
      if (!overflows.forall(_ <= 0)) {
        val currentIndex = state.middlewareData.flip.flatMap(_.index).getOrElse(0)
        val nextIndex = currentIndex + 1
        
        if (nextIndex < placements.length) {
          val nextPlacement = placements(nextIndex)
          
          // Try next placement and re-run the lifecycle
          return MiddlewareReturn(
            data = Some(Map("index" -> nextIndex)),
            reset = Some(ResetValue(placement = Some(nextPlacement)))
          )
        }
        
        // If no more placements, use fallback strategy
        if (options.fallbackStrategy == "bestFit") {
          // Find placement with least overflow
          val bestPlacement = placements.headOption.getOrElse(state.initialPlacement)
          return MiddlewareReturn(
            reset = Some(ResetValue(placement = Some(bestPlacement)))
          )
        } else {
          // Use initial placement
          return MiddlewareReturn(
            reset = Some(ResetValue(placement = Some(state.initialPlacement)))
          )
        }
      }
      
      MiddlewareReturn()
    }
  }
}

