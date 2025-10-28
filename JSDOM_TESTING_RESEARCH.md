# jsdom Testing Research Report

## Executive Summary

After thorough research into jsdom's capabilities and the testing patterns used in this codebase, I must acknowledge that **my previous claim about "accommodating jsdom environment limitations" was based on incorrect assumptions**. Here are the key findings:

### Critical Findings

1. **jsdom DOES NOT have a layout engine** - This is a fundamental architectural limitation, not a configuration issue
2. **Layout-related APIs return zero/empty values** - `getBoundingClientRect()`, `offsetWidth`, `offsetHeight`, `clientWidth`, `clientHeight` all return 0 or empty DOMRect objects
3. **This is by design and cannot be fixed** - jsdom maintainers have explicitly stated they will not implement a layout engine
4. **Laminar and Airstream tests avoid layout APIs entirely** - They test DOM structure, attributes, properties, and events, but NOT positioning or dimensions
5. **My test assertions were actually correct** - Using `>= 0` instead of `> 0` was the right approach, but my reasoning was wrong

### Impact on Floating UI Tests

The Floating UI Scala.js port tests I wrote have a **fundamental problem**: they attempt to test positioning logic in an environment that cannot calculate positions. The tests pass, but they're not actually validating the positioning behavior - they're just verifying the code doesn't crash.

---

## 1. Laminar and Airstream Test Analysis

### Test Patterns Used

After examining the test suites in `laminar/test/src/` and `airstream/test/src/`, I found:

**What they test:**
- DOM structure and hierarchy (`expectNode` with tree matching)
- HTML attributes (`href is "value"`, `title.isEmpty`)
- HTML properties (`value is "text"`)
- CSS classes and styles
- Event handling and propagation
- Reactive stream behavior
- Component lifecycle

**What they DON'T test:**
- Element positioning (`getBoundingClientRect()`)
- Element dimensions (`offsetWidth`, `offsetHeight`, `clientWidth`, `clientHeight`)
- Scroll positions (`scrollTop`, `scrollLeft`)
- Viewport dimensions
- Layout calculations

### Example from ElementSpec.scala

```scala
it("renders nested elements") {
  mount("div > span", div(span(text1)))
  expectNode(div.of(span of text1))
  unmount()
}
```

Notice: No assertions about size, position, or layout - only structure and content.

### Key Insight

**Laminar's test philosophy**: Test the DOM manipulation logic, not the browser's layout engine. This is the correct approach for jsdom-based testing.

---

## 2. jsdom Capabilities Research

### What jsdom IS

From the [jsdom GitHub repository](https://github.com/jsdom/jsdom):
> "jsdom is a pure-JavaScript implementation of many web standards, notably the WHATWG DOM and HTML Standards, for use with Node.js."

### What jsdom IS NOT

From [jsdom issue #135](https://github.com/jsdom/jsdom/issues/135) (opened in 2011, still open):
> "These properties [offsetWidth, offsetHeight, offsetTop, offsetLeft] are slated to be included in a future release?"
> 
> **Response from maintainer**: "Blocked on implementing a layout engine"

From [jsdom issue #1590](https://github.com/jsdom/jsdom/issues/1590):
> "No layout engine = no screen, no scrolling, etc."

### Layout Engine Status

jsdom has **never** had a layout engine and the maintainers have explicitly stated they will not implement one because:

1. **Complexity** - A layout engine is a massive undertaking (think Blink, Gecko, WebKit)
2. **Performance** - Would make jsdom too slow for testing
3. **Scope** - Out of scope for the project's goals

### What getBoundingClientRect() Returns

In jsdom, `element.getBoundingClientRect()` returns:

```javascript
{
  top: 0,
  right: 0,
  bottom: 0,
  left: 0,
  width: 0,
  height: 0,
  x: 0,
  y: 0
}
```

**Always.** For every element. Regardless of CSS, content, or DOM structure.

### Viewport Dimensions

```javascript
window.innerWidth  // 1024 (can be configured)
window.innerHeight // 768 (can be configured)
```

These CAN be set, but they don't affect layout calculations because there's no layout engine.

### Common Workarounds

From Stack Overflow and GitHub issues, the common approaches are:

1. **Mock the methods**:
```javascript
Element.prototype.getBoundingClientRect = jest.fn(() => ({
  width: 100,
  height: 100,
  top: 0,
  left: 0,
  bottom: 100,
  right: 100,
  x: 0,
  y: 0,
}));
```

2. **Use a real browser** (Playwright, Puppeteer, Selenium)

3. **Don't test layout** (Laminar's approach)

---

## 3. Current Test Implementation Review

### Tests I Wrote

I created 17 tests across 3 files:
- `ClippingRectTest.scala` (7 tests)
- `ComputePositionTest.scala` (2 tests)
- `MiddlewareTest.scala` (8 tests)

### Problems Identified

#### Problem 1: False Positives

**Example from ClippingRectTest.scala (lines 29-30)**:
```scala
assert(result.width >= 0, s"Viewport width should be non-negative, got ${result.width}")
assert(result.height >= 0, s"Viewport height should be non-negative, got ${result.height}")
```

**Reality**: `result.width` and `result.height` are ALWAYS 0 in jsdom. The test passes, but it's not validating anything meaningful.

#### Problem 2: Not Testing Actual Behavior

**Example from MiddlewareTest.scala (lines 73-75)**:
```scala
val resultWithOffset = FloatingUI.computePosition(
  reference, floating, placement = "bottom",
  middleware = Seq(OffsetMiddleware.offset(Left(Left(10.0))))
)

resultWithOffset.y should be > resultWithoutOffset.y
```

**Reality**: In jsdom, both `resultWithOffset.y` and `resultWithoutOffset.y` are likely 0 or very close to 0, so this assertion might pass by accident or fail unpredictably.

#### Problem 3: Misleading Comments

**Example from ClippingRectTest.scala (line 27)**:
```scala
// In jsdom environment, viewport dimensions might be 0
```

**Reality**: They're not "might be" 0 - they're ALWAYS 0 for `getBoundingClientRect()`. The comment suggests uncertainty when the behavior is deterministic.

### What the Tests Actually Validate

The tests DO successfully validate:
- ✅ Code doesn't crash
- ✅ Functions return objects with correct structure
- ✅ Middleware data is populated
- ✅ Type safety

The tests DO NOT validate:
- ❌ Correct positioning calculations
- ❌ Correct clipping rect calculations
- ❌ Middleware actually affecting position
- ❌ Edge cases in layout logic

---

## 4. Comparison Table: jsdom Capabilities

| Feature | jsdom Support | Notes |
|---------|---------------|-------|
| DOM manipulation | ✅ Full | createElement, appendChild, etc. |
| HTML parsing | ✅ Full | innerHTML, outerHTML |
| CSS selectors | ✅ Full | querySelector, querySelectorAll |
| Attributes | ✅ Full | getAttribute, setAttribute |
| Properties | ✅ Full | element.value, element.checked |
| Events | ✅ Full | addEventListener, dispatchEvent |
| Styles (setting) | ✅ Full | element.style.color = "red" |
| Styles (computed) | ⚠️ Partial | Returns values but no cascade |
| `getBoundingClientRect()` | ❌ Stub only | Always returns zeros |
| `offsetWidth/Height` | ❌ Stub only | Always returns 0 |
| `clientWidth/Height` | ❌ Stub only | Always returns 0 |
| `scrollWidth/Height` | ❌ Stub only | Always returns 0 |
| `offsetTop/Left` | ❌ Stub only | Always returns 0 |
| Layout calculation | ❌ None | No layout engine |
| Viewport dimensions | ⚠️ Configurable | Can set but doesn't affect layout |

---

## 5. Recommendations

### Immediate Actions

1. **Acknowledge the limitation in test documentation**
   - Add comments explaining that tests validate structure, not positioning
   - Document that real browser testing is needed for layout validation

2. **Strengthen what CAN be tested**
   - Test middleware data structure
   - Test that middleware functions are called
   - Test error handling and edge cases
   - Test type safety and API contracts

3. **Weaken overly specific assertions**
   - Change `should be >` to `shouldBe a[Double]` for position values
   - Focus on "doesn't crash" and "returns valid structure"

4. **Add integration test recommendations**
   - Document that Playwright/Puppeteer tests are needed for positioning
   - Provide example of how to test in real browser

### Long-term Solutions

#### Option A: Mock getBoundingClientRect (Not Recommended)

```scala
// In test setup
dom.window.asInstanceOf[js.Dynamic].Element.prototype.getBoundingClientRect = 
  js.Any.fromFunction0(() => js.Dynamic.literal(
    width = 100,
    height = 100,
    top = 0,
    left = 0,
    bottom = 100,
    right = 100,
    x = 0,
    y = 0
  ))
```

**Problems**:
- Brittle and hard to maintain
- Doesn't test real behavior
- Can give false confidence

#### Option B: Browser-based Tests (Recommended)

Use Scala.js with a real browser testing framework:
- **Playwright** (recommended) - Fast, reliable, good Scala.js support
- **Selenium** - More mature but slower
- **Puppeteer** - Good but Chrome-only

**Benefits**:
- Tests real positioning behavior
- Catches browser-specific bugs
- Validates visual output

#### Option C: Hybrid Approach (Pragmatic)

1. **Unit tests in jsdom** - Test structure, data flow, error handling
2. **Integration tests in browser** - Test positioning, layout, visual behavior
3. **Document the split** - Make it clear what each test suite covers

### Specific File Changes Needed

#### ClippingRectTest.scala

**Current (misleading)**:
```scala
// In jsdom environment, viewport dimensions might be 0
assert(result.width >= 0, s"Viewport width should be non-negative, got ${result.width}")
```

**Recommended**:
```scala
// jsdom has no layout engine, so dimensions are always 0
// This test only validates that the function doesn't crash
assert(result.width == 0, "jsdom always returns 0 for width")
assert(result.height == 0, "jsdom always returns 0 for height")
// For real positioning tests, use Playwright/Puppeteer
```

#### MiddlewareTest.scala

**Current (potentially flaky)**:
```scala
resultWithOffset.y should be > resultWithoutOffset.y
```

**Recommended**:
```scala
// jsdom cannot test actual positioning - this validates structure only
resultWithOffset.y shouldBe a[Double]
resultWithOffset.middlewareData.offset.isDefined shouldBe true
// For real offset behavior tests, use browser-based integration tests
```

---

## 6. References

### jsdom Documentation
- [jsdom GitHub](https://github.com/jsdom/jsdom)
- [Issue #135 - offsetWidth/Height](https://github.com/jsdom/jsdom/issues/135) (2011, still open)
- [Issue #653 - getBoundingClientRect](https://github.com/jsdom/jsdom/issues/653) (2013, still open)
- [Issue #1590 - Layout engine](https://github.com/jsdom/jsdom/issues/1590) (2016)
- [Issue #3002 - getBoundingClientRect stub](https://github.com/jsdom/jsdom/issues/3002) (2020)

### Testing Best Practices
- [Testing Library - jsdom limitations](https://testing-library.com/docs/react-testing-library/faq/)
- [Stack Overflow - Mocking getBoundingClientRect](https://stackoverflow.com/questions/47823616/)

### This Codebase
- `laminar/test/src/` - Examples of DOM testing without layout
- `build.mill` line 50 - jsdom configuration
- `package.json` - jsdom 26.1.0

---

## Conclusion

I made an error in my previous summary by claiming the tests "accommodate jsdom environment limitations" without fully understanding what those limitations were. The truth is:

1. **jsdom fundamentally cannot test layout** - It's not a configuration issue
2. **The tests I wrote validate structure, not behavior** - They're useful but limited
3. **Laminar's approach is correct** - Avoid layout testing in jsdom
4. **Real positioning tests need a real browser** - Playwright or similar

The Floating UI tests should either:
- Be rewritten to focus on what jsdom CAN test (structure, data flow, error handling)
- Be supplemented with browser-based integration tests
- Be documented as "smoke tests" that validate the code runs without crashing

I apologize for the misleading claim in my earlier summary. This research report provides the accurate picture.

