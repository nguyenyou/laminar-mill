# Flip Component Positioning Test - Summary

## Test Results ✅

**All tests passing!**

```
Run completed in 338 milliseconds.
Total number of tests run: 3
Suites: completed 1, aborted 0
Tests: succeeded 3, failed 0, canceled 0, ignored 0, pending 0
All tests passed.
```

## What Was Created

### 1. Test File
**Location:** `floatingUI/test/src/integration/FlipComponentPositioningTest.scala`

A comprehensive Playwright-based integration test that mimics the Flip component structure from `www/src/www/floating/Flip.scala`.

### 2. Documentation
**Location:** `floatingUI/test/FLIP_COMPONENT_TESTING.md`

Complete documentation covering:
- Test overview and purpose
- What each test verifies
- How to run the tests
- Expected output
- Bug fix explanation
- Troubleshooting guide

## Test Coverage

### Test 1: Basic Coordinate Calculation
**Name:** "should calculate correct coordinates for reference and floating elements in a scroll container"

**What it tests:**
- Creates a scroll container (450×450px) with scrollable content (1500×1660px)
- Places a reference element at (670, 750) with size 160×160px
- Places a floating element with size 80×80px
- Centers the scroll position (as in Flip component)
- Verifies `computePosition` calculates correct coordinates

**Expected result:**
- X = 710px (reference.left + reference.width/2 - floating.width/2)
- Y = 910px (reference.top + reference.height)

**Status:** ✅ PASS

### Test 2: Scroll Position Changes
**Name:** "should handle scroll position changes correctly"

**What it tests:**
- Verifies coordinates remain consistent regardless of scroll position
- Tests with no scroll (0, 0)
- Tests with centered scroll (as in Flip component)
- Confirms coordinates are relative to offset parent

**Status:** ✅ PASS

### Test 3: Different Reference Positions
**Name:** "should position floating element correctly with different reference positions"

**What it tests:**
- Tests coordinate calculation with various reference positions and sizes
- Verifies the formula works consistently across different scenarios
- Tests 3 different configurations

**Status:** ✅ PASS

## How to Run

### Run Only These Tests

```bash
./mill floatingUI.test.testOnly io.github.nguyenyou.floatingUI.integration.FlipComponentPositioningTest
```

### Run All FloatingUI Tests

```bash
./mill floatingUI.test
```

### Prerequisites

If you haven't set up Playwright yet:

```bash
# Install Playwright
npm install -D playwright

# Install system dependencies
npx playwright install-deps

# Install Chromium browser
npx playwright install chromium
```

## What the Tests Verify

These tests verify that the bug fix to `getRectRelativeToOffsetParent` correctly handles:

1. **Scroll position adjustments** - The scroll container's scroll position is properly accounted for
2. **Offset parent calculations** - Coordinates are correctly calculated relative to the offset parent
3. **Element positioning** - Reference and floating elements are positioned correctly
4. **Formula accuracy** - The positioning formula works across different element sizes and positions

## Before vs After the Bug Fix

### Before (Broken)
```
Calculated: X: 186, Y: 306 ❌
Expected:   X: 710, Y: 910
```

The scroll position wasn't being accounted for in `getRectRelativeToOffsetParent`.

### After (Fixed)
```
Calculated: X: 710, Y: 910 ✅
Expected:   X: 710, Y: 910
```

The scroll adjustments are now correctly applied:
```scala
val x = rect.left + scroll._1 - offsets.x - htmlOffset.x
val y = rect.top + scroll._2 - offsets.y - htmlOffset.y
```

## Test Output Example

When running the tests, you'll see output like:

```
[info] FlipComponentPositioningTest:
[info] computePosition with Flip component structure
[info] - should calculate correct coordinates for reference and floating elements in a scroll container
✓ Calculated coordinates: X: 710.0, Y: 910.0
✓ Expected coordinates: X: 710.0, Y: 910.0
✓ Reference position: offsetLeft=670.0, offsetTop=750.0
✓ Scroll position: scrollLeft=525, scrollTop=605
[info] - should handle scroll position changes correctly
✓ No scroll - X: 710.0, Y: 910.0
✓ Centered scroll - X: 710.0, Y: 910.0
  Scroll position: scrollLeft=525, scrollTop=605
[info] - should position floating element correctly with different reference positions
✓ Reference at (100.0, 100.0) 100.0x50.0, Floating 150.0x80.0
  Calculated: X=75.0, Y=150.0, Expected: X=75.0, Y=150.0
✓ Reference at (300.0, 200.0) 120.0x60.0, Floating 100.0x70.0
  Calculated: X=310.0, Y=260.0, Expected: X=310.0, Y=260.0
✓ Reference at (500.0, 400.0) 80.0x80.0, Floating 60.0x60.0
  Calculated: X=510.0, Y=480.0, Expected: X=510.0, Y=480.0
```

## Relationship to Flip Component

The test structure mirrors the Flip component (`www/src/www/floating/Flip.scala`):

**Flip Component:**
```scala
// Create scroll container
div(
  className := "scroll",
  position.relative,
  // ... scroll setup
  
  // Reference element
  div(
    className := "reference",
    "Reference"
  ),
  
  // Floating element
  div(
    className := "floating",
    position.absolute,
    top.px(910),
    left.px(711),
    "Floating"
  )
)

// Compute position
val pos = FloatingUI.computePosition(
  reference = reference,
  floating = floating,
  middleware = Seq(FlipMiddleware.flip())
)
println(s"X: ${pos.x}, Y: ${pos.y}")
```

**Test:**
```scala
// Create scroll container
scrollContainer = dom.document.createElement("div")
scrollContainer.style.position = "relative"
scrollContainer.style.width = "450px"
scrollContainer.style.height = "450px"
scrollContainer.style.overflow = "auto"

// Create reference element
reference.style.left = "670px"
reference.style.top = "750px"
reference.style.width = "160px"
reference.style.height = "160px"

// Create floating element
floating.style.width = "80px"
floating.style.height = "80px"

// Compute position
val result = computePosition(
  reference = reference,
  floating = floating,
  placement = "bottom",
  middleware = Seq(FlipMiddleware.flip())
)

// Verify coordinates
result.x shouldBe 710.0 +- 1.0
result.y shouldBe 910.0 +- 1.0
```

## Files Created

1. **`floatingUI/test/src/integration/FlipComponentPositioningTest.scala`** - The test file
2. **`floatingUI/test/FLIP_COMPONENT_TESTING.md`** - Comprehensive documentation
3. **`floatingUI/test/FLIP_TEST_SUMMARY.md`** - This summary file

## Next Steps

These tests are now part of the FloatingUI test suite and will run automatically when you execute:

```bash
./mill floatingUI.test
```

They provide confidence that the `computePosition` method correctly handles scroll containers and offset parent calculations, which is critical for the Flip component and any other components that use FloatingUI with scrollable containers.

