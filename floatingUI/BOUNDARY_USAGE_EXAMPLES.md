# Boundary Type Usage Examples

This document demonstrates the new `Boundary` type capabilities in the floatingUI module. The `Boundary` type now supports all four TypeScript variants, providing complete parity with the upstream Floating UI library.

## Overview

The `Boundary` type is a Scala 3 union type that accepts:

```scala
type Boundary = String | dom.Element | js.Array[dom.Element] | Rect
```

This allows you to specify clipping boundaries in multiple ways:
1. **String "clippingAncestors"** - Uses the element's overflow ancestors (default)
2. **String (CSS selector)** - Queries the DOM for a boundary element
3. **dom.Element** - Single DOM element as boundary
4. **js.Array[dom.Element]** - Multiple DOM elements as boundaries
5. **Rect** - Custom rectangle as boundary

## Example 1: Using String (Backward Compatible)

All existing code continues to work without changes:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

// Default: use clipping ancestors
val options1 = FlipOptions(
  boundary = "clippingAncestors"
)

// CSS selector: query for a container element
val options2 = ShiftOptions(
  boundary = "#scroll-container"
)

// Use in computePosition
val result = computePosition(
  referenceEl,
  floatingEl,
  ComputePositionConfig(
    placement = Placement.Bottom,
    middleware = Seq(
      flip(Left(options1)),
      shift(Left(options2))
    ),
    platform = DOMPlatform
  )
)
```

## Example 2: Using DOM Element

Pass a DOM element directly without string conversion:

```scala
import org.scalajs.dom
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

// Get a container element
val container = dom.document.querySelector("#scroll-container")
  .asInstanceOf[dom.Element]

// Pass element directly - type-safe!
val options = FlipOptions(
  boundary = container  // Type: Boundary (accepts Element)
)

// The floating element will be constrained to this container
val result = computePosition(
  referenceEl,
  floatingEl,
  ComputePositionConfig(
    placement = Placement.Bottom,
    middleware = Seq(flip(Left(options))),
    platform = DOMPlatform
  )
)
```

## Example 3: Using Multiple Elements

Constrain to multiple boundary elements:

```scala
import scala.scalajs.js
import org.scalajs.dom
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

// Get multiple container elements
val container1 = dom.document.querySelector("#container-1").asInstanceOf[dom.Element]
val container2 = dom.document.querySelector("#container-2").asInstanceOf[dom.Element]
val container3 = dom.document.querySelector("#container-3").asInstanceOf[dom.Element]

// Create array of boundaries
val boundaries = js.Array(container1, container2, container3)

// Use multiple boundaries
val options = ShiftOptions(
  boundary = boundaries  // Type: Boundary (accepts js.Array[Element])
)

// The floating element will be constrained by all three containers
val result = computePosition(
  referenceEl,
  floatingEl,
  ComputePositionConfig(
    middleware = Seq(shift(Left(options))),
    platform = DOMPlatform
  )
)
```

## Example 4: Using Custom Rect

Define a custom clipping area programmatically:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

// Define a custom clipping area (e.g., safe area on mobile)
val safeArea = Rect(
  x = 20,      // 20px from left
  y = 50,      // 50px from top (status bar)
  width = 360, // 360px wide
  height = 700 // 700px tall
)

val options = AutoPlacementOptions(
  boundary = safeArea  // Type: Boundary (accepts Rect)
)

// Floating element will be constrained to this custom rectangle
val result = computePosition(
  referenceEl,
  floatingEl,
  ComputePositionConfig(
    middleware = Seq(autoPlacement(Left(options))),
    platform = DOMPlatform
  )
)
```

## Example 5: Dynamic Boundary Selection

Choose boundary based on runtime conditions:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

def createTooltipConfig(
  useCustomBoundary: Boolean,
  containerEl: Option[dom.Element],
  isMobile: Boolean
): ComputePositionConfig = {
  
  // Choose boundary based on runtime conditions
  val boundary: Boundary = (useCustomBoundary, containerEl, isMobile) match {
    case (true, Some(el), _) => 
      // Use specific container element
      el
      
    case (true, None, true) => 
      // Mobile: use safe area rect
      Rect(x = 10, y = 40, width = dom.window.innerWidth - 20, height = dom.window.innerHeight - 80)
      
    case (true, None, false) => 
      // Desktop: use viewport minus margins
      Rect(x = 20, y = 20, width = dom.window.innerWidth - 40, height = dom.window.innerHeight - 40)
      
    case (false, _, _) => 
      // Use default clipping ancestors
      "clippingAncestors"
  }
  
  ComputePositionConfig(
    placement = Placement.Top,
    middleware = Seq(
      flip(Left(FlipOptions(boundary = boundary))),
      shift(Left(ShiftOptions(boundary = boundary)))
    ),
    platform = DOMPlatform
  )
}
```

## Example 6: Scrollable Container Pattern

Common pattern for tooltips in scrollable containers:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

// Get the scrollable container
val scrollContainer = dom.document.querySelector(".scrollable-content")
  .asInstanceOf[dom.Element]

// Configure tooltip to stay within scroll container
val tooltipConfig = ComputePositionConfig(
  placement = Placement.Top,
  strategy = Strategy.Absolute,
  middleware = Seq(
    // Flip if it doesn't fit in the scroll container
    flip(Left(FlipOptions(
      boundary = scrollContainer,
      fallbackStrategy = FallbackStrategy.BestFit
    ))),
    
    // Shift to stay within scroll container
    shift(Left(ShiftOptions(
      boundary = scrollContainer,
      padding = Left(8) // 8px padding from edges
    ))),
    
    // Hide if reference is scrolled out of view
    hide(Left(HideOptions(
      boundary = scrollContainer,
      strategy = "referenceHidden"
    )))
  ),
  platform = DOMPlatform
)
```

## Example 7: All Middleware Options Support Boundary

All middleware options that extend `DetectOverflowOptions` support the new `Boundary` type:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

val container = dom.document.querySelector("#container").asInstanceOf[dom.Element]

// FlipOptions
val flipOpts = FlipOptions(boundary = container)

// ShiftOptions
val shiftOpts = ShiftOptions(boundary = container)

// AutoPlacementOptions
val autoOpts = AutoPlacementOptions(boundary = container)

// HideOptions
val hideOpts = HideOptions(boundary = container)

// SizeOptions
val sizeOpts = SizeOptions(boundary = container)

// All work seamlessly with the new Boundary type
val config = ComputePositionConfig(
  middleware = Seq(
    flip(Left(flipOpts)),
    shift(Left(shiftOpts)),
    hide(Left(hideOpts)),
    size(Left(sizeOpts))
  ),
  platform = DOMPlatform
)
```

## Migration Guide

### No Breaking Changes

This enhancement is **100% backward compatible**. All existing code continues to work:

```scala
// Old code (still works):
FlipOptions(boundary = "clippingAncestors")
ShiftOptions(boundary = "#container")

// New code (now also supported):
FlipOptions(boundary = containerElement)
ShiftOptions(boundary = js.Array(el1, el2))
AutoPlacementOptions(boundary = Rect(0, 0, 100, 100))
```

### Type Safety Benefits

The new implementation provides:
- ✅ **Type-safe** - Compiler checks boundary types
- ✅ **Exhaustive matching** - Internal pattern matching is compiler-verified
- ✅ **IDE support** - Better autocomplete and type hints
- ✅ **TypeScript parity** - Matches upstream Floating UI API exactly
- ✅ **Flexible** - Choose the most convenient boundary representation

## Technical Details

### Internal Implementation

The implementation uses a hybrid approach:
- **Public API**: Scala 3 union type for ergonomics
- **Internal**: Sealed trait for type-safe pattern matching

```scala
// Public API (ergonomic)
type Boundary = String | dom.Element | js.Array[dom.Element] | Rect

// Internal (type-safe)
private[floatingUI] sealed trait BoundaryInternal
private[floatingUI] object BoundaryInternal {
  case object ClippingAncestors extends BoundaryInternal
  case class Element(element: dom.Element) extends BoundaryInternal
  case class Elements(elements: js.Array[dom.Element]) extends BoundaryInternal
  case class CustomRect(rect: Rect) extends BoundaryInternal
  
  def fromBoundary(boundary: Boundary): BoundaryInternal = { ... }
}
```

This provides TypeScript-like ergonomics while maintaining Scala's type safety guarantees.

