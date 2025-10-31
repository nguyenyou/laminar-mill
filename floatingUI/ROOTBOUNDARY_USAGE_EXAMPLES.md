# RootBoundary Type Usage Examples

This document demonstrates the new `RootBoundary` type capabilities in the floatingUI module. The `RootBoundary` type now supports all three TypeScript variants, providing complete parity with the upstream Floating UI library.

## Overview

The `RootBoundary` type is a Scala 3 union type that accepts:

```scala
type RootBoundary = String | Rect
```

This allows you to specify the root clipping boundary in three ways:
1. **String "viewport"** - Uses the browser viewport as the root boundary (default)
2. **String "document"** - Uses the entire document as the root boundary
3. **Rect** - Custom rectangle as the root boundary (new functionality!)

## Example 1: Using "viewport" (Default - Backward Compatible)

The viewport is the visible area of the browser window:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

// Default: use viewport as root boundary
val options1 = FlipOptions(
  rootBoundary = "viewport"  // Explicit
)

// Or rely on default value
val options2 = ShiftOptions()  // rootBoundary defaults to "viewport"

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

## Example 2: Using "document"

The document boundary includes the entire scrollable document area:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

// Use document as root boundary - allows floating element to extend beyond viewport
val options = FlipOptions(
  rootBoundary = "document"
)

// Useful for tooltips that should be visible even when scrolling
val result = computePosition(
  referenceEl,
  floatingEl,
  ComputePositionConfig(
    placement = Placement.Top,
    middleware = Seq(
      flip(Left(options)),
      shift(Left(ShiftOptions(rootBoundary = "document")))
    ),
    platform = DOMPlatform
  )
)
```

## Example 3: Using Custom Rect (New Functionality!)

Define a custom root clipping area programmatically:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

// Define a custom root boundary (e.g., safe area on mobile devices)
val safeArea = Rect(
  x = 0,       // Start at left edge
  y = 60,      // 60px from top (status bar + notch)
  width = dom.window.innerWidth,
  height = dom.window.innerHeight - 100  // Exclude bottom navigation
)

val options = AutoPlacementOptions(
  rootBoundary = safeArea  // Type: RootBoundary (accepts Rect)
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

## Example 4: Mobile Safe Area Pattern

Common pattern for mobile apps with notches and navigation bars:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

def getMobileSafeArea(): Rect = {
  // Account for status bar, notch, and bottom navigation
  val topInset = 44    // iPhone notch height
  val bottomInset = 34 // iPhone home indicator
  
  Rect(
    x = 0,
    y = topInset,
    width = dom.window.innerWidth,
    height = dom.window.innerHeight - topInset - bottomInset
  )
}

// Use safe area for all tooltips on mobile
val tooltipConfig = ComputePositionConfig(
  placement = Placement.Top,
  middleware = Seq(
    flip(Left(FlipOptions(
      rootBoundary = getMobileSafeArea()
    ))),
    shift(Left(ShiftOptions(
      rootBoundary = getMobileSafeArea()
    )))
  ),
  platform = DOMPlatform
)
```

## Example 5: Dynamic Root Boundary Selection

Choose root boundary based on runtime conditions:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

def createTooltipConfig(
  allowOverflow: Boolean,
  isMobile: Boolean,
  customArea: Option[Rect]
): ComputePositionConfig = {
  
  // Choose root boundary based on runtime conditions
  val rootBoundary: RootBoundary = (allowOverflow, isMobile, customArea) match {
    case (true, _, _) => 
      // Allow tooltip to extend beyond viewport
      "document"
      
    case (false, true, Some(area)) => 
      // Mobile with custom safe area
      area
      
    case (false, true, None) => 
      // Mobile without custom area - use viewport
      "viewport"
      
    case (false, false, _) => 
      // Desktop - use viewport
      "viewport"
  }
  
  ComputePositionConfig(
    placement = Placement.Top,
    middleware = Seq(
      flip(Left(FlipOptions(rootBoundary = rootBoundary))),
      shift(Left(ShiftOptions(rootBoundary = rootBoundary)))
    ),
    platform = DOMPlatform
  )
}
```

## Example 6: Fullscreen Modal Pattern

Constrain tooltips within a fullscreen modal:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

// Get modal dimensions
val modal = dom.document.querySelector(".fullscreen-modal").asInstanceOf[dom.HTMLElement]
val modalRect = modal.getBoundingClientRect()

// Create custom root boundary matching modal
val modalBoundary = Rect(
  x = modalRect.left,
  y = modalRect.top,
  width = modalRect.width,
  height = modalRect.height
)

// Tooltips inside modal will be constrained to modal area
val tooltipConfig = ComputePositionConfig(
  placement = Placement.Bottom,
  middleware = Seq(
    flip(Left(FlipOptions(
      rootBoundary = modalBoundary
    ))),
    shift(Left(ShiftOptions(
      rootBoundary = modalBoundary,
      padding = Left(16) // 16px padding from modal edges
    )))
  ),
  platform = DOMPlatform
)
```

## Example 7: All Middleware Options Support RootBoundary

All middleware options that extend `DetectOverflowOptions` support the new `RootBoundary` type:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

val customRoot = Rect(x = 0, y = 0, width = 1920, height = 1080)

// FlipOptions
val flipOpts = FlipOptions(rootBoundary = customRoot)

// ShiftOptions
val shiftOpts = ShiftOptions(rootBoundary = customRoot)

// AutoPlacementOptions
val autoOpts = AutoPlacementOptions(rootBoundary = customRoot)

// HideOptions
val hideOpts = HideOptions(rootBoundary = customRoot)

// SizeOptions
val sizeOpts = SizeOptions(rootBoundary = customRoot)

// All work seamlessly with the new RootBoundary type
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

## Example 8: Combining Boundary and RootBoundary

Use both custom boundary and custom root boundary together:

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

// Custom clipping boundary (e.g., scrollable container)
val scrollContainer = dom.document.querySelector("#scroll-area").asInstanceOf[dom.Element]

// Custom root boundary (e.g., application viewport)
val appViewport = Rect(
  x = 0,
  y = 80,  // Below header
  width = dom.window.innerWidth,
  height = dom.window.innerHeight - 80
)

val options = FlipOptions(
  boundary = scrollContainer,      // Clip to scroll container
  rootBoundary = appViewport       // But don't exceed app viewport
)

val result = computePosition(
  referenceEl,
  floatingEl,
  ComputePositionConfig(
    middleware = Seq(flip(Left(options))),
    platform = DOMPlatform
  )
)
```

## Migration Guide

### No Breaking Changes

This enhancement is **100% backward compatible**. All existing code continues to work:

```scala
// Old code (still works):
FlipOptions(rootBoundary = "viewport")
ShiftOptions(rootBoundary = "document")

// New code (now also supported):
FlipOptions(rootBoundary = Rect(0, 0, 1920, 1080))
ShiftOptions(rootBoundary = customSafeArea)
```

### Type Safety Benefits

The new implementation provides:
- ✅ **Type-safe** - Compiler checks rootBoundary types
- ✅ **Exhaustive matching** - Internal pattern matching is compiler-verified
- ✅ **IDE support** - Better autocomplete and type hints
- ✅ **TypeScript parity** - Matches upstream Floating UI API exactly
- ✅ **New functionality** - Custom Rect support (previously not available)

## Comparison: viewport vs document vs custom Rect

| Root Boundary | Use Case | Behavior |
|--------------|----------|----------|
| **"viewport"** | Default, most common | Constrains to visible browser window |
| **"document"** | Scrollable content | Allows overflow beyond viewport |
| **Custom Rect** | Mobile safe areas, modals | Precise control over clipping area |

## Technical Details

### Internal Implementation

The implementation uses a hybrid approach:
- **Public API**: Scala 3 union type for ergonomics
- **Internal**: Sealed trait for type-safe pattern matching

```scala
// Public API (ergonomic)
type RootBoundary = String | Rect

// Internal (type-safe)
private[floatingUI] sealed trait RootBoundaryInternal
private[floatingUI] object RootBoundaryInternal {
  case object Viewport extends RootBoundaryInternal
  case object Document extends RootBoundaryInternal
  case class CustomRect(rect: Rect) extends RootBoundaryInternal
  
  def fromRootBoundary(rootBoundary: RootBoundary): RootBoundaryInternal = { ... }
}
```

This provides TypeScript-like ergonomics while maintaining Scala's type safety guarantees.

