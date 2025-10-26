package io.github.nguyenyou.laminar.primitives.utils.floating

import scala.scalajs.js
import org.scalajs.dom

/** Core types for floating element positioning.
  * 
  * Ported from @floating-ui/utils and @floating-ui/core
  */
object Types {
  
  // ============================================================================
  // Basic Types
  // ============================================================================
  
  /** Alignment of the floating element relative to the reference element. */
  type Alignment = "start" | "end"
  
  /** Side of the reference element where the floating element is placed. */
  type Side = "top" | "right" | "bottom" | "left"
  
  /** Placement of the floating element. Can be a side or a side with alignment. */
  type Placement = String
  
  /** Positioning strategy: "absolute" or "fixed". */
  type Strategy = "absolute" | "fixed"
  
  /** Axis for positioning: "x" or "y". */
  type Axis = "x" | "y"
  
  /** Length dimension: "width" or "height". */
  type Length = "width" | "height"
  
  // ============================================================================
  // Coordinate and Dimension Types
  // ============================================================================
  
  /** Coordinates on x and y axes. */
  case class Coords(x: Double, y: Double)
  
  /** Dimensions (width and height). */
  case class Dimensions(width: Double, height: Double)
  
  /** Rectangle with coordinates and dimensions. */
  case class Rect(x: Double, y: Double, width: Double, height: Double)
  
  /** Side object with values for each side. */
  case class SideObject(top: Double, right: Double, bottom: Double, left: Double)
  
  /** Client rectangle object (combines Rect and SideObject). */
  case class ClientRectObject(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    top: Double,
    right: Double,
    bottom: Double,
    left: Double
  )
  
  /** Element rectangles for reference and floating elements. */
  case class ElementRects(reference: Rect, floating: Rect)
  
  /** Padding can be a number or a partial side object. */
  type Padding = Double | PartialSideObject
  
  /** Partial side object for padding. */
  case class PartialSideObject(
    top: Option[Double] = None,
    right: Option[Double] = None,
    bottom: Option[Double] = None,
    left: Option[Double] = None
  )
  
  // ============================================================================
  // Middleware Types
  // ============================================================================
  
  /** Middleware data returned from middleware functions. */
  case class MiddlewareData(
    arrow: Option[ArrowData] = None,
    offset: Option[OffsetData] = None,
    shift: Option[ShiftData] = None,
    flip: Option[FlipData] = None
  )
  
  /** Arrow middleware data. */
  case class ArrowData(
    x: Option[Double] = None,
    y: Option[Double] = None,
    centerOffset: Double,
    alignmentOffset: Option[Double] = None
  )
  
  /** Offset middleware data. */
  case class OffsetData(x: Double, y: Double, placement: Placement)
  
  /** Shift middleware data. */
  case class ShiftData(x: Double, y: Double)
  
  /** Flip middleware data. */
  case class FlipData(index: Option[Int] = None)
  
  /** Middleware state passed to middleware functions. */
  case class MiddlewareState(
    x: Double,
    y: Double,
    initialPlacement: Placement,
    placement: Placement,
    strategy: Strategy,
    middlewareData: MiddlewareData,
    elements: Elements,
    rects: ElementRects,
    platform: Platform
  )
  
  /** Middleware return value. */
  case class MiddlewareReturn(
    x: Option[Double] = None,
    y: Option[Double] = None,
    data: Option[Map[String, Any]] = None,
    reset: Option[ResetValue] = None
  )
  
  /** Reset value for middleware. */
  case class ResetValue(
    placement: Option[Placement] = None,
    rects: Option[Either[Boolean, ElementRects]] = None
  )
  
  /** Middleware object. */
  trait Middleware {
    def name: String
    def fn(state: MiddlewareState): MiddlewareReturn
  }
  
  /** Elements object containing reference and floating elements. */
  case class Elements(reference: dom.Element, floating: dom.HTMLElement)
  
  // ============================================================================
  // Platform Interface
  // ============================================================================
  
  /** Platform interface for DOM operations. */
  trait Platform {
    def getElementRects(reference: dom.Element, floating: dom.HTMLElement, strategy: Strategy): ElementRects
    def getDimensions(element: dom.Element): Dimensions
    def getClippingRect(element: dom.Element, boundary: String, rootBoundary: String, strategy: Strategy): Rect
  }
  
  // ============================================================================
  // Configuration and Return Types
  // ============================================================================
  
  /** Configuration for computePosition. */
  case class ComputePositionConfig(
    placement: Placement = "bottom",
    strategy: Strategy = "absolute",
    middleware: Seq[Middleware] = Seq.empty,
    platform: Platform
  )
  
  /** Return value from computePosition. */
  case class ComputePositionReturn(
    x: Double,
    y: Double,
    placement: Placement,
    strategy: Strategy,
    middlewareData: MiddlewareData
  )
  
  // ============================================================================
  // Middleware Options
  // ============================================================================
  
  /** Options for offset middleware. */
  case class OffsetOptions(
    mainAxis: Double = 0,
    crossAxis: Double = 0,
    alignmentAxis: Option[Double] = None
  )
  
  /** Options for shift middleware. */
  case class ShiftOptions(
    mainAxis: Boolean = true,
    crossAxis: Boolean = false,
    padding: Padding = 0
  )
  
  /** Options for flip middleware. */
  case class FlipOptions(
    mainAxis: Boolean = true,
    crossAxis: Boolean = true,
    fallbackPlacements: Option[Seq[Placement]] = None,
    fallbackStrategy: String = "bestFit",
    padding: Padding = 0
  )
  
  /** Options for arrow middleware. */
  case class ArrowOptions(
    element: dom.HTMLElement,
    padding: Padding = 0
  )
  
  /** Detect overflow options. */
  case class DetectOverflowOptions(
    boundary: String = "clippingAncestors",
    rootBoundary: String = "viewport",
    elementContext: String = "floating",
    altBoundary: Boolean = false,
    padding: Padding = 0
  )
}

