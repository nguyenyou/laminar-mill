# Flip Component Positioning Tests

This document describes the Playwright-based integration tests for verifying FloatingUI position calculations with a DOM structure similar to the Flip component (`www/src/www/floating/Flip.scala`).

## Overview

The `FlipComponentPositioningTest.scala` test file creates a realistic DOM structure that mimics the Flip component:
- A scrollable container (`.scroll` div)
- A reference element (`.reference` div) positioned inside the scroll container
- A floating element (`.floating` div) that should be positioned relative to the reference

These tests verify that the recent bug fix to `getRectRelativeToOffsetParent` correctly handles scroll positions when calculating coordinates.

## Test File Location

```
floatingUI/test/src/integration/FlipComponentPositioningTest.scala
```

## What the Tests Verify

### Test 1: Basic Coordinate Calculation
**Test Name:** "should calculate correct coordinates for reference and floating elements in a scroll container"

**Setup:**
- Scroll container: 450px × 450px with scrollable content (1500px × 1660px)
- Reference element: 160px × 160px at position (670, 750)
- Floating element: 80px × 80px
- Scroll position: Centered (as in Flip component)

**Expected Behavior:**
For "bottom" placement, the floating element should be positioned at:
- **X = 710px**: `reference.offsetLeft (670) + reference.width/2 (80) - floating.width/2 (40)`
- **Y = 910px**: `reference.offsetTop (750) + reference.height (160)`

**What It Tests:**
- ✅ Correct coordinate calculation with scroll container
- ✅ Proper handling of scroll position (centered)
- ✅ Accurate offset parent calculations
- ✅ Placement strategy ("bottom")

**Before the Bug Fix:**
- Calculated: X: 186, Y: 306 ❌
- The scroll position wasn't being accounted for

**After the Bug Fix:**
- Calculated: X: 710, Y: 910 ✅
- Scroll adjustments are now correctly applied

### Test 2: Scroll Position Changes
**Test Name:** "should handle scroll position changes correctly"

**What It Tests:**
- Coordinates remain consistent regardless of scroll position
- Coordinates are relative to the offset parent (scroll container)
- Scroll changes don't affect the calculated position

**Test Scenarios:**
1. **No scroll** (scrollTop=0, scrollLeft=0)
   - Expected: X=710, Y=910
2. **Centered scroll** (as in Flip component)
   - Expected: X=710, Y=910 (same as no scroll)

**Why This Matters:**
The coordinates returned by `computePosition` are relative to the offset parent. The scroll position affects the viewport-relative position but not the offset-parent-relative position that FloatingUI calculates.

### Test 3: Different Reference Positions
**Test Name:** "should position floating element correctly with different reference positions"

**What It Tests:**
- Coordinate calculation works for various reference element positions
- Formula is consistent across different element sizes

**Test Cases:**
1. Reference at (100, 100), size 100×50, floating 150×80
2. Reference at (300, 200), size 120×60, floating 100×70
3. Reference at (500, 400), size 80×80, floating 60×60

**Formula Verification:**
For each case, verifies:
```
X = refLeft + (refWidth / 2) - (floatWidth / 2)
Y = refTop + refHeight
```

## Running the Tests

### Prerequisites (One-Time Setup)

If you haven't already set up Playwright:

```bash
# Install Playwright CLI
npm install -D playwright

# Install system dependencies for Chromium
npx playwright install-deps

# Install Chromium browser
npx playwright install chromium
```

### Run All FloatingUI Tests

```bash
./mill floatingUI.test
```

This runs all tests including:
- Unit tests (ClippingRectTest, ComputePositionTest, etc.)
- Integration tests (PositioningIntegrationTest, FlipComponentPositioningTest)

### Run Only the Flip Component Tests

```bash
./mill floatingUI.test.testOnly io.github.nguyenyou.floatingUI.integration.FlipComponentPositioningTest
```

### Run a Specific Test

Use ScalaTest's `-z` filter (note: no `--` separator needed):

```bash
./mill floatingUI.test -z "should calculate correct coordinates"
```

### Enable Debug Mode

To see browser logs and debug information, edit `build.mill`:

```scala
override def jsEnvConfig = Task {
  JsEnvConfig.Playwright.chrome(
    headless = false,  // Show browser window
    showLogs = true,   // Show console logs
    debug = true       // Show Playwright debug info
  )
}
```

Then run the tests again.

## Expected Output

When all tests pass, you should see:

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
[info] Run completed in X seconds.
[info] Total number of tests run: 3
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 3, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
```

## Understanding the Bug That Was Fixed

### The Problem

The Scala implementation of `getRectRelativeToOffsetParent` in `floatingUI/src/io/github/nguyenyou/floatingUI/DOMUtils.scala` was missing scroll position adjustments.

**Before (Incorrect):**
```scala
Rect(
  x = (rect.left - offsetParentClientRect.x) / scale.x,
  y = (rect.top - offsetParentClientRect.y) / scale.y,
  // ... missing scroll adjustments
)
```

**After (Correct):**
```scala
val x = rect.left + scroll._1 - offsets.x - htmlOffset.x
val y = rect.top + scroll._2 - offsets.y - htmlOffset.y

Rect(x = x, y = y, width = rect.width, height = rect.height)
```

### Why It Matters

When elements are inside a scrollable container:
1. `getBoundingClientRect()` returns viewport-relative coordinates
2. To get offset-parent-relative coordinates, we need to account for:
   - The offset parent's position
   - The scroll position (`scroll.scrollLeft`, `scroll.scrollTop`)
   - Any offsets from borders/padding
   - HTML element offsets

Without the scroll adjustments, the calculated coordinates were wrong when the scroll container was scrolled.

## Relationship to the Flip Component

The Flip component (`www/src/www/floating/Flip.scala`) uses this exact pattern:

```scala
// Lines 84-87: Center the scroll container
val y = scroll.scrollHeight / 2.0 - scroll.offsetHeight / 2.0
val x = scroll.scrollWidth / 2.0 - scroll.offsetWidth / 2.0
scroll.scrollTop = y
scroll.scrollLeft = if (rtl) -x else x

// Lines 49-54: Compute position
val pos = FloatingUI.computePosition(
  reference = reference,
  floating = floating,
  middleware = Seq(FlipMiddleware.flip())
)
println(s"X: ${pos.x}, Y: ${pos.y}")
```

The tests verify that this pattern works correctly and produces the expected coordinates.

## CI/CD Integration

These tests can be integrated into CI/CD pipelines. Example GitHub Actions workflow:

```yaml
name: Test FloatingUI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install Playwright
        run: |
          npm install
          npx playwright install-deps
          npx playwright install chromium
      
      - name: Run Tests
        run: ./mill floatingUI.test
```

## Troubleshooting

### Tests Fail with "Executable doesn't exist"

**Problem:** Playwright browsers not installed.

**Solution:**
```bash
npx playwright install chromium
npx playwright install-deps
```

### Coordinates Don't Match Expected Values

**Problem:** The bug fix may not have been applied correctly.

**Solution:**
1. Check that `floatingUI/src/io/github/nguyenyou/floatingUI/DOMUtils.scala` has the correct `getRectRelativeToOffsetParent` implementation
2. Recompile: `./mill floatingUI.compile`
3. Run tests again: `./mill floatingUI.test`

### Tests Are Slow

**Problem:** Playwright tests are slower than jsdom tests.

**Solution:** This is expected. Playwright starts a real browser with a layout engine. Consider:
- Running these tests less frequently (e.g., only on CI/CD or before releases)
- Using jsdom for fast unit tests
- Running Playwright tests only for positioning-critical functionality

## Further Reading

- [Playwright Testing Setup](./PLAYWRIGHT_TESTING.md) - Complete guide to Playwright testing in this project
- [FloatingUI Documentation](https://floating-ui.com/) - Official FloatingUI documentation
- [Test Porting Summary](./TEST_PORTING_SUMMARY.md) - Summary of ported TypeScript tests

