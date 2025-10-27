# Floating UI Scala.js Port - Bug Report and Fixes

## Executive Summary

This document details the bugs found in the Scala.js port of the Floating UI library (originally written in TypeScript) and the fixes applied. The review identified **10 critical bugs** that could cause incorrect behavior, runtime errors, or performance issues.

---

## Bug #1: FlipMiddleware Has Wrong Name ✅ FIXED

**Severity:** Critical  
**File:** `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/middleware/FlipMiddleware.scala`  
**Line:** 14

### Issue
The flip middleware was incorrectly named "shift" instead of "flip".

```scala
// BEFORE (WRONG)
override def name: String = "shift"

// AFTER (CORRECT)
override def name: String = "flip"
```

### Impact
- Middleware data was stored/retrieved under the wrong key
- Flip functionality would not work correctly
- Middleware data lookups would fail
- Could cause infinite loops or incorrect placement decisions

### Root Cause
Copy-paste error from ShiftMiddleware implementation.

---

## Bug #2: Missing RTL (Right-to-Left) Support ✅ FIXED

**Severity:** High  
**Files:** 
- `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/Types.scala`
- `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/ComputePosition.scala`
- `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/middleware/OffsetMiddleware.scala`
- `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/middleware/FlipMiddleware.scala`

### Issue
RTL text direction was hardcoded to `false` throughout the codebase, and the Platform interface didn't implement `isRTL`.

```scala
// BEFORE (WRONG)
val rtl = false  // Hardcoded
val crossAxisMulti = if (isVertical) -1 else 1 // No RTL support

// AFTER (CORRECT)
val rtl = config.platform.isRTL(floating)
val crossAxisMulti = if (rtl && isVertical) -1 else 1
```

### Impact
- Incorrect positioning for RTL languages (Arabic, Hebrew, Persian, etc.)
- Alignment calculations would be wrong for RTL content
- Cross-axis multiplier calculations would be incorrect

### Changes Made
1. Added `isRTL` method to Platform trait with default implementation
2. Updated ComputePosition to check RTL from platform
3. Updated OffsetMiddleware to use RTL for crossAxisMulti calculation
4. Updated FlipMiddleware to use RTL for alignment sides calculation

---

## Bug #3: Arrow Middleware Reset Logic Incorrect ✅ FIXED

**Severity:** High  
**File:** `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/middleware/ArrowMiddleware.scala`  
**Line:** 89

### Issue
When the arrow causes an alignment offset, the reset value didn't request rect recalculation.

```scala
// BEFORE (WRONG)
reset = if (shouldAddOffset) Some(ResetValue()) else None

// AFTER (CORRECT)
reset = if (shouldAddOffset) Some(ResetValue(rects = Some(Left(true)))) else None
```

### Impact
- Element rects wouldn't be recalculated when arrow causes alignment offset
- Could lead to incorrect positioning after arrow adjustment
- Violates the TypeScript implementation's behavior

### Root Cause
The TypeScript version returns `reset: shouldAddOffset` (boolean), which the platform interprets as "recalculate rects". The Scala port needs to explicitly request this via `ResetValue(rects = Some(Left(true)))`.

---

## Bug #4: Shift Middleware Missing "enabled" Field ✅ FIXED

**Severity:** Medium  
**File:** `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/middleware/ShiftMiddleware.scala`  
**Lines:** 64-67

### Issue
The shift middleware data didn't include the `enabled` field that tracks which axes were checked.

```scala
// BEFORE (WRONG)
data = Some(Map(
  "x" -> (limitedCoords.x - state.x),
  "y" -> (limitedCoords.y - state.y)
))

// AFTER (CORRECT)
data = Some(Map(
  "x" -> (limitedCoords.x - state.x),
  "y" -> (limitedCoords.y - state.y),
  "enabled" -> Map(
    mainAxis -> options.mainAxis,
    crossAxis -> options.crossAxis
  )
))
```

### Impact
- Consumers can't determine which axes were actually shifted
- Breaks compatibility with TypeScript API
- Other middleware that depend on this information would fail

---

## Bug #5: Missing Reset.rects Handling ✅ FIXED

**Severity:** High  
**File:** `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/ComputePosition.scala`  
**Lines:** 85-98

### Issue
When middleware returns `reset.rects`, the implementation didn't handle recalculating or updating the rects.

```scala
// BEFORE (WRONG)
result.reset.foreach { reset =>
  resetCount += 1
  reset.placement.foreach { newPlacement =>
    statefulPlacement = newPlacement
  }
  coords = computeCoordsFromPlacement(rects, statefulPlacement, rtl = false)
  i = -1
}

// AFTER (CORRECT)
result.reset.foreach { reset =>
  resetCount += 1
  reset.placement.foreach { newPlacement =>
    statefulPlacement = newPlacement
  }
  // Recalculate rects if requested
  reset.rects.foreach {
    case Left(true) =>
      rects = config.platform.getElementRects(reference, floating, config.strategy)
    case Right(newRects) =>
      rects = newRects
    case Left(false) =>
      ()
  }
  coords = computeCoordsFromPlacement(rects, statefulPlacement, rtl)
  i = -1
}
```

### Impact
- Middleware that needs to recalculate element rects won't work properly
- Arrow middleware's reset wouldn't trigger rect recalculation
- Could cause stale rect data to be used in calculations

### Additional Change
Changed `rects` from `val` to `var` to allow reassignment.

---

## Bug #6: Null Case in Pattern Match ✅ FIXED

**Severity:** Low  
**File:** `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/ComputeCoordsFromPlacement.scala`  
**Line:** 42

### Issue
Pattern match had `case null` which will never match in Scala (placement is a String).

```scala
// BEFORE (WRONG)
case null =>
  Coords(x = reference.x, y = reference.y)

// AFTER (CORRECT)
case _ =>
  // Default case for any other placement (shouldn't happen in practice)
  Coords(x = reference.x, y = reference.y)
```

### Impact
- Dead code (null case never matches)
- Indicates incomplete porting from TypeScript
- Could hide bugs if unexpected placement values are passed

---

## Bug #7: Missing Passive Scroll Listeners ✅ FIXED

**Severity:** Medium (Performance)  
**File:** `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/AutoUpdate.scala`  
**Lines:** 57-71, 133-142

### Issue
Scroll event listeners weren't marked as passive, which can block rendering.

```scala
// BEFORE (WRONG)
ancestor.addEventListener("scroll", scrollHandler, useCapture = false)

// AFTER (CORRECT)
val scrollOptions = js.Dynamic.literal("passive" -> true).asInstanceOf[dom.EventListenerOptions]
ancestor.addEventListener("scroll", scrollHandler, scrollOptions)
```

### Impact
- Performance degradation during scrolling
- Scroll events block rendering pipeline
- Browser can't optimize scroll performance
- Violates modern web performance best practices

### Changes Made
1. Created `scrollOptions` with `passive: true`
2. Updated addEventListener calls to use scrollOptions
3. Updated removeEventListener calls to match

---

## Bug #8: Missing Async/Promise Support ⚠️ ARCHITECTURAL ISSUE

**Severity:** Critical (Architectural)  
**Files:** All files  

### Issue
The TypeScript implementation uses async/await and Promises extensively, but the Scala.js port is completely synchronous.

```typescript
// TypeScript (ORIGINAL)
export const computePosition: ComputePosition = async (reference, floating, config) => {
  const rtl = await platform.isRTL?.(floating);
  let rects = await platform.getElementRects({reference, floating, strategy});
  // ... more async operations
}

// Scala.js (CURRENT - SYNCHRONOUS)
def computePosition(reference: dom.Element, floating: dom.HTMLElement, config: ComputePositionConfig): ComputePositionReturn = {
  val rtl = config.platform.isRTL(floating)
  var rects = config.platform.getElementRects(reference, floating, config.strategy)
  // ... synchronous operations
}
```

### Impact
- **Cannot support async platform operations**
- Platform methods that need to be async (e.g., measuring elements in certain scenarios) won't work
- Breaks compatibility with async middleware
- Fundamental architectural difference from TypeScript version

### Status
**NOT FIXED** - This is an architectural decision. The current implementation assumes all platform operations are synchronous, which works for DOM operations but limits extensibility.

### Recommendation
If async support is needed in the future:
1. Change return types to `Future[T]` or use Scala.js Promises
2. Update all middleware to support async operations
3. Update Platform interface methods to return `Future[T]`
4. This would be a major breaking change

---

## Bug #9: Incomplete Flip Middleware Implementation ⚠️ PARTIAL FIX

**Severity:** High  
**File:** `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/middleware/FlipMiddleware.scala`

### Issues
1. ✅ **FIXED:** Wrong middleware name ("shift" instead of "flip")
2. ✅ **FIXED:** Missing RTL support
3. ⚠️ **NOT FIXED:** Missing overflow tracking (overflowsData)
4. ⚠️ **NOT FIXED:** bestFit strategy doesn't actually calculate best fit
5. ⚠️ **NOT FIXED:** Missing flipAlignment option support
6. ⚠️ **NOT FIXED:** Missing fallbackAxisSideDirection option support

### Current Implementation Limitations
```scala
// Current simplified implementation
if (options.fallbackStrategy == "bestFit") {
  // Find placement with least overflow
  val bestPlacement = placements.headOption.getOrElse(state.initialPlacement)
  return MiddlewareReturn(reset = Some(ResetValue(placement = Some(bestPlacement))))
}
```

The TypeScript version:
- Tracks all overflow data for each placement attempt
- Calculates actual best fit by summing positive overflows
- Supports complex fallback strategies
- Handles cross-axis alignment flipping

### Impact
- Flip middleware works for basic cases but not complex scenarios
- bestFit strategy just returns first placement, not actual best fit
- Missing advanced flip options

---

## Bug #10: Missing Limiter Support in Shift Middleware ⚠️ NOT FIXED

**Severity:** Medium  
**File:** `primitives/src/io/github/nguyenyou/laminar/primitives/utils/floating/middleware/ShiftMiddleware.scala`

### Issue
The TypeScript version supports a `limiter` option (like `limitShift`) to prevent detachment, but the Scala port doesn't.

```typescript
// TypeScript (ORIGINAL)
const limitedCoords = limiter.fn({
  ...state,
  [mainAxis]: mainAxisCoord,
  [crossAxis]: crossAxisCoord,
});
```

### Impact
- Can't use `limitShift` functionality
- Floating element may detach from reference in some scenarios
- Missing important positioning constraint feature

### Status
**NOT FIXED** - Would require implementing limitShift and updating ShiftOptions type.

---

## Summary of Fixes Applied

### ✅ Fixed (7 bugs)
1. FlipMiddleware wrong name
2. Missing RTL support (Platform, ComputePosition, OffsetMiddleware, FlipMiddleware)
3. Arrow middleware reset logic
4. Shift middleware missing "enabled" field
5. Missing reset.rects handling
6. Null case in pattern match
7. Missing passive scroll listeners

### ⚠️ Not Fixed - Architectural/Scope Issues (3 bugs)
8. Missing async/Promise support (architectural decision)
9. Incomplete flip middleware (partial fix - basic functionality works)
10. Missing limiter support in shift middleware (feature not implemented)

---

## Testing Recommendations

After these fixes, the following should be tested:

1. **RTL Support:** Test with Arabic/Hebrew content
2. **Arrow Positioning:** Test arrow with alignment offsets
3. **Shift Middleware:** Verify enabled field is populated correctly
4. **Flip Middleware:** Test basic flip scenarios (complex scenarios may still have issues)
5. **AutoUpdate Performance:** Verify scroll performance with passive listeners
6. **Reset Handling:** Test middleware that triggers resets with rect recalculation

---

## Compatibility Notes

The Scala.js port is now more compatible with the TypeScript version for:
- RTL text direction support
- Middleware data structures
- Reset behavior with rect recalculation
- Performance optimizations (passive listeners)

However, it still differs in:
- Synchronous vs asynchronous execution model
- Simplified flip middleware implementation
- Missing limiter support

---

**Report Generated:** 2025-10-27  
**Reviewer:** Claude (Augment Agent)  
**Files Modified:** 7 files

