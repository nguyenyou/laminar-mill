# FlipMiddleware Implementation - Complete Feature Set

## Overview

This document describes the complete implementation of the FlipMiddleware in the Scala.js port of Floating UI, matching the functionality of the original TypeScript implementation.

## Implementation Summary

The FlipMiddleware has been fully implemented with all features from the TypeScript original:

### 1. **Overflow Tracking** ✅

The middleware now tracks overflow data for each placement attempt in an `overflowsData` array:

```scala
case class PlacementOverflow(
  placement: Placement,
  overflows: Seq[Double]
)

case class FlipData(
  index: Option[Int] = None,
  overflows: Seq[PlacementOverflow] = Seq.empty
)
```

Each placement attempt stores:
- The placement string (e.g., "top", "bottom-start")
- An array of overflow values (main axis overflow, cross-axis overflow 1, cross-axis overflow 2)

### 2. **Proper bestFit Strategy** ✅

The bestFit algorithm now:
1. First tries to find placements that fit on the main axis (overflow[0] <= 0)
2. Sorts those by cross-axis overflow (overflow[1])
3. If no placement fits on main axis, calculates total positive overflow for each placement
4. Filters placements based on `fallbackAxisSideDirection` if enabled
5. Applies a bias toward the y-axis for horizontal reading directions
6. Selects the placement with minimum total overflow

```scala
// First, find candidates that fit on the main axis
var resetPlacement = overflowsData
  .filter(d => d.overflows.headOption.exists(_ <= 0))
  .sortBy(d => d.overflows.lift(1).getOrElse(0.0))
  .headOption
  .map(_.placement)

// If no placement fits on main axis, use fallback strategy
if (resetPlacement.isEmpty) {
  options.fallbackStrategy match {
    case "bestFit" =>
      val placementWithOverflow = overflowsData
        .filter { d =>
          if (hasFallbackAxisSideDirection) {
            val currentSideAxis = getSideAxis(d.placement)
            currentSideAxis == initialSideAxis || currentSideAxis == "y"
          } else {
            true
          }
        }
        .map { d =>
          val totalOverflow = d.overflows.filter(_ > 0).sum
          (d.placement, totalOverflow)
        }
        .sortBy(_._2)
        .headOption
      resetPlacement = placementWithOverflow.map(_._1)
    // ...
  }
}
```

### 3. **flipAlignment Option** ✅

Added support for controlling alignment flipping (default: true):

```scala
case class FlipOptions(
  // ...
  flipAlignment: Boolean = true,
  // ...
)
```

When `flipAlignment = true`, the middleware includes opposite alignment variants in the fallback placements:
- For "top-start": tries "top-end", "bottom", "bottom-start", "bottom-end"
- For "top-start" with `flipAlignment = false`: only tries "bottom"

### 4. **fallbackAxisSideDirection Option** ✅

Added support for perpendicular axis fallback ('none' | 'start' | 'end'):

```scala
case class FlipOptions(
  // ...
  fallbackAxisSideDirection: String = "none",
  // ...
)
```

When set to "start" or "end", the middleware adds placements on the perpendicular axis:
- For "top" with direction="start": adds "left", "right"
- For "top" with direction="end": adds "right", "left"
- For "left" with direction="start": adds "top", "bottom"

Implemented via the new `getOppositeAxisPlacements` utility function:

```scala
def getOppositeAxisPlacements(
  placement: Placement,
  flipAlignment: Boolean,
  direction: String,
  rtl: Boolean = false
): Seq[Placement]
```

### 5. **Cross-axis Alignment Mode** ✅

The `crossAxis` option now supports both Boolean and String ("alignment"):

```scala
type FlipCrossAxis = Boolean | String

case class FlipOptions(
  // ...
  crossAxis: FlipCrossAxis = true,
  // ...
)
```

When `crossAxis = "alignment"`:
- Cross-axis overflow is only checked when staying on the same axis
- When moving to a perpendicular axis, cross-axis overflow is ignored
- This allows the middleware to leave the current main axis only if every placement on that axis overflows

```scala
val ignoreCrossAxisOverflow = checkCrossAxis match {
  case s: String if s == "alignment" =>
    initialSideAxis != getSideAxis(nextPlacement.get)
  case _ => false
}

val shouldTryNext = !ignoreCrossAxisOverflow || {
  overflowsData.forall { d =>
    if (getSideAxis(d.placement) == initialSideAxis) {
      d.overflows.headOption.exists(_ > 0)
    } else {
      true
    }
  }
}
```

### 6. **Overflow Data in Return** ✅

The middleware now includes the complete `overflowsData` array in the return data:

```scala
MiddlewareReturn(
  data = Some(Map(
    "index" -> nextIndex,
    "overflows" -> overflowsData
  )),
  reset = Some(ResetValue(placement = nextPlacement))
)
```

This allows:
- Debugging and inspection of all placement attempts
- Understanding why a particular placement was chosen
- Building custom fallback strategies

## Edge Cases Handled

### 1. Empty Placements Array
If no fallback placements are provided and the initial placement is a base placement (no alignment), the middleware defaults to the opposite placement:

```scala
val fallbackPlacements = specifiedFallbackPlacements.getOrElse {
  if (isBasePlacement || !options.flipAlignment) {
    Seq(getOppositePlacement(state.initialPlacement))
  } else {
    getExpandedPlacements(state.initialPlacement)
  }
}
```

### 2. All Placements Overflowing
When all placements overflow, the fallback strategy determines the final placement:
- **bestFit**: Selects the placement with minimum total overflow
- **initialPlacement**: Returns to the initial placement

### 3. Cross-axis Alignment Mode with Perpendicular Axis Changes
The middleware correctly handles the case where `crossAxis = "alignment"` and the next placement is on a perpendicular axis:

```scala
val ignoreCrossAxisOverflow = checkCrossAxis match {
  case s: String if s == "alignment" =>
    initialSideAxis != getSideAxis(nextPlacement.get)
  case _ => false
}
```

### 4. Arrow Alignment Offset
The middleware skips flip logic if an arrow middleware caused an alignment offset:

```scala
if (state.middlewareData.arrow.flatMap(_.alignmentOffset).isDefined) {
  return MiddlewareReturn()
}
```

### 5. RTL Support
All placement calculations respect right-to-left text direction:

```scala
val rtl = state.platform.isRTL(state.elements.floating)
```

## Integration with Reset Mechanism

The middleware properly integrates with the existing reset mechanism:

1. **Index Tracking**: Maintains the current placement index in middleware data
2. **Placement Reset**: Requests placement reset when a better placement is found
3. **Overflow Persistence**: Preserves overflow data across middleware runs

```scala
var overflowsData = state.middlewareData.flip.map(_.overflows).getOrElse(Seq.empty)
overflowsData = overflowsData :+ PlacementOverflow(
  placement = state.placement,
  overflows = overflows.toSeq
)
```

## New Utility Functions

### getOppositeAxisPlacements

Added to `Utils.scala` to support `fallbackAxisSideDirection`:

```scala
def getOppositeAxisPlacements(
  placement: Placement,
  flipAlignment: Boolean,
  direction: String,
  rtl: Boolean = false
): Seq[Placement]
```

This function:
- Determines the perpendicular axis based on the current placement
- Returns placements on that axis in the specified direction
- Optionally includes opposite alignment variants
- Respects RTL text direction

## Type Updates

### FlipOptions
```scala
case class FlipOptions(
  mainAxis: Boolean = true,
  crossAxis: FlipCrossAxis = true,
  fallbackPlacements: Option[Seq[Placement]] = None,
  fallbackStrategy: String = "bestFit",
  fallbackAxisSideDirection: String = "none",
  flipAlignment: Boolean = true,
  padding: Padding = 0
)
```

### FlipData
```scala
case class FlipData(
  index: Option[Int] = None,
  overflows: Seq[PlacementOverflow] = Seq.empty
)
```

### PlacementOverflow
```scala
case class PlacementOverflow(
  placement: Placement,
  overflows: Seq[Double]
)
```

## Testing Recommendations

To verify the implementation, test the following scenarios:

1. **Basic Flip**: Element overflows on one side, flips to opposite
2. **Alignment Flip**: Element with alignment overflows, tries opposite alignment
3. **Best Fit**: All placements overflow, selects one with minimum overflow
4. **Fallback Axis**: Element overflows on main axis, tries perpendicular axis
5. **Cross-axis Alignment Mode**: Element stays on main axis until all placements overflow
6. **RTL Support**: Placements are correct for RTL languages
7. **Overflow Data**: Middleware returns complete overflow data for debugging

## Comparison with TypeScript Original

The Scala.js implementation now matches the TypeScript original (lines 62-227 of `flip.ts`) with the following differences:

1. **Type Safety**: Uses Scala's type system for better compile-time safety
2. **Immutability**: Uses immutable data structures where possible
3. **Pattern Matching**: Uses Scala's pattern matching instead of switch statements
4. **Option Types**: Uses `Option` instead of nullable values

All core logic, algorithms, and edge case handling are identical to the TypeScript version.

