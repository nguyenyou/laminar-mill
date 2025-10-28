# Floating UI Scala.js Port - Prioritized Improvements

This document provides a comprehensive, actionable list of improvements to make the Floating UI Scala.js port a "perfect" implementation. Each improvement includes priority level, estimated effort, and specific files affected.

---

## 📊 Summary Statistics

| Category | Critical | High | Medium | Low | Total |
|----------|----------|------|--------|-----|-------|
| Testing | 1 | 2 | 1 | 0 | 4 |
| Documentation | 0 | 2 | 2 | 1 | 5 |
| Code Quality | 0 | 3 | 3 | 2 | 8 |
| Performance | 0 | 1 | 2 | 1 | 4 |
| Type Safety | 0 | 2 | 1 | 0 | 3 |
| API Design | 0 | 1 | 2 | 1 | 4 |
| **Total** | **1** | **11** | **11** | **5** | **28** |

---

## 1. Testing Coverage

### 1.1 Create Comprehensive Test Suite
**Priority**: 🔴 **Critical**  
**Effort**: Large  
**Category**: Testing

**What**: Implement a complete test suite matching the TypeScript implementation's coverage.

**Why**: 
- Currently **zero test files** exist in `floatingUI/test/`
- TypeScript has 30+ test files covering unit, functional, and visual tests
- Without tests, regressions and bugs can go undetected
- Tests serve as living documentation of expected behavior

**Affected Files**:
- Create: `floatingUI/test/src/ComputePositionTest.scala`
- Create: `floatingUI/test/src/DetectOverflowTest.scala`
- Create: `floatingUI/test/src/middleware/` (8 test files)
- Create: `floatingUI/test/src/DOMUtilsTest.scala`
- Create: `floatingUI/test/src/AutoUpdateTest.scala`

**Specific Actions**:
1. **Unit Tests** (Priority: Critical)
   - Test `computePosition` with various placements and middleware combinations
   - Test `detectOverflow` with different boundaries and padding
   - Test each middleware function independently
   - Test utility functions (placement, padding, rect operations)

2. **Integration Tests** (Priority: High)
   - Test middleware composition and interaction
   - Test reset mechanism with multiple resets
   - Test virtual element support
   - Test RTL (right-to-left) text direction handling

3. **DOM Tests** (Priority: High)
   - Test with jsdom environment (already configured in build.mill)
   - Test `autoUpdate` with ResizeObserver and IntersectionObserver
   - Test clipping rect calculation with various DOM structures
   - Test iframe traversal and scale calculations

4. **Edge Case Tests** (Priority: Medium)
   - Test with zero-sized elements
   - Test with negative coordinates
   - Test with extreme viewport sizes
   - Test reset count limit (50 resets)

**Example Test Structure**:
```scala
class ComputePositionTest extends AnyFunSuite with DomTester {
  test("should position floating element at bottom by default") {
    withElement { container =>
      val reference = createDiv(container, width = 100, height = 100)
      val floating = createDiv(container, width = 50, height = 50)
      
      val result = computePosition(reference, floating)
      
      assert(result.placement == "bottom")
      assert(result.x == 25.0) // centered
      assert(result.y == 100.0) // below reference
    }
  }
}
```

---

### 1.2 Add Property-Based Testing
**Priority**: 🟡 **High**  
**Effort**: Medium  
**Category**: Testing

**What**: Implement property-based tests using ScalaCheck to verify invariants.

**Why**:
- Catches edge cases that manual tests might miss
- Verifies mathematical properties (e.g., overflow calculations)
- Ensures consistency across all placement combinations

**Affected Files**:
- Update: `build.mill` (add ScalaCheck dependency)
- Create: `floatingUI/test/src/properties/PlacementProperties.scala`
- Create: `floatingUI/test/src/properties/OverflowProperties.scala`

**Specific Properties to Test**:
1. **Placement Invariants**:
   - Opposite placements should produce opposite coordinates
   - Alignment should always be within reference bounds
   - RTL should mirror horizontal placements

2. **Overflow Detection**:
   - Sum of overflow sides should equal total overflow
   - Negative overflow means space available
   - Overflow should be scale-adjusted correctly

3. **Middleware Composition**:
   - Order of middleware should not cause infinite loops
   - Reset count should never exceed 50

---

### 1.3 Add Visual Regression Tests
**Priority**: 🟡 **High**  
**Effort**: Large  
**Category**: Testing

**What**: Create visual regression tests similar to TypeScript's Playwright tests.

**Why**:
- Ensures pixel-perfect positioning across browsers
- Catches subtle rendering issues
- Validates complex scenarios (scrolling, transforms, iframes)

**Affected Files**:
- Create: `floatingUI/test/visual/` directory structure
- Create: Visual test specs for each middleware
- Update: `build.mill` (add Playwright or similar tool)

**Test Scenarios** (from TypeScript):
- Arrow positioning with various sizes and padding
- AutoPlacement with different allowed placements
- Border and padding edge cases
- Containing block scenarios
- Decimal size handling
- Flip behavior with scrolling
- Hide strategies
- Inline element positioning
- Offset with various configurations
- Relative positioning
- Scrollbar handling
- Shadow DOM support
- Shift with limiters
- Size middleware resizing
- Table element positioning
- Transform handling

---

### 1.4 Add Benchmark Tests
**Priority**: 🟢 **Medium**  
**Effort**: Small  
**Category**: Testing

**What**: Create performance benchmarks to track positioning speed.

**Why**:
- Ensures performance doesn't regress
- Identifies optimization opportunities
- Compares with TypeScript implementation

**Affected Files**:
- Create: `floatingUI/test/src/benchmarks/PositioningBenchmark.scala`

**Benchmarks**:
- Single `computePosition` call
- `computePosition` with all middleware
- `autoUpdate` update frequency
- Clipping rect calculation (with and without cache)

---

## 2. Documentation

### 2.1 Expand Scaladoc Comments
**Priority**: 🟡 **High**  
**Effort**: Medium  
**Category**: Documentation

**What**: Add comprehensive Scaladoc comments matching TypeScript's JSDoc coverage.

**Why**:
- Current Scaladoc is minimal compared to TypeScript
- Improves IDE autocomplete and inline documentation
- Helps users understand API without reading source code

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/FloatingUI.scala`
- `floatingUI/src/io/github/nguyenyou/floatingUI/Types.scala`
- All middleware files (8 files)
- `floatingUI/src/io/github/nguyenyou/floatingUI/Utils.scala`
- `floatingUI/src/io/github/nguyenyou/floatingUI/DOMUtils.scala`

**Specific Improvements**:
1. **Add `@example` tags** with code snippets
2. **Add `@see` links** to floating-ui.com documentation
3. **Document all parameters** with `@param`
4. **Document return values** with `@return`
5. **Add `@note` for important caveats**
6. **Cross-reference related functions**

**Example**:
```scala
/** Shifts the floating element to keep it in view.
  *
  * Optimizes the floating element's position by shifting it along the main axis
  * and/or cross axis to prevent overflow of the clipping boundary.
  *
  * @example
  * {{{
  * computePosition(
  *   reference,
  *   floating,
  *   middleware = Seq(shift())
  * )
  * }}}
  *
  * @param options Shift configuration (can be static or derivable from state)
  * @return Middleware object that performs shifting
  * @see [[https://floating-ui.com/docs/shift Shift Documentation]]
  * @note Use `limitShift()` to prevent the floating element from detaching from the reference
  */
def shift(options: Derivable[ShiftOptions] = Left(ShiftOptions())): Middleware
```

---

### 2.2 Create Usage Examples
**Priority**: 🟡 **High**  
**Effort**: Medium  
**Category**: Documentation

**What**: Create comprehensive usage examples in a dedicated examples directory.

**Why**:
- No examples currently exist for the Scala.js port
- Users need to see real-world usage patterns
- Examples serve as integration tests

**Affected Files**:
- Create: `floatingUI/examples/BasicPositioning.scala`
- Create: `floatingUI/examples/TooltipExample.scala`
- Create: `floatingUI/examples/DropdownExample.scala`
- Create: `floatingUI/examples/PopoverExample.scala`
- Create: `floatingUI/examples/VirtualElementExample.scala`
- Create: `floatingUI/examples/README.md`

**Example Topics**:
1. Basic positioning with different placements
2. Tooltip with arrow
3. Dropdown menu with flip and shift
4. Popover with auto-placement
5. Virtual element (mouse position)
6. Auto-update with scroll/resize
7. Custom middleware
8. RTL support

---

### 2.3 Add README for floatingUI Module
**Priority**: 🟢 **Medium**  
**Effort**: Small  
**Category**: Documentation

**What**: Create a README.md specifically for the floatingUI module.

**Why**:
- No module-specific documentation exists
- Users need quick start guide
- Should explain differences from TypeScript version

**Affected Files**:
- Create: `floatingUI/README.md`

**Content**:
- Overview and purpose
- Installation instructions
- Quick start example
- API differences from TypeScript
- Link to full documentation
- Migration guide from TypeScript
- Known limitations

---

### 2.4 Generate API Documentation
**Priority**: 🟢 **Medium**  
**Effort**: Small  
**Category**: Documentation

**What**: Set up automated Scaladoc generation and hosting.

**Why**:
- Makes API documentation easily accessible
- Provides searchable reference
- Standard practice for Scala libraries

**Affected Files**:
- Update: `build.mill` (add Scaladoc generation task)
- Create: `floatingUI/docs/` output directory

**Actions**:
1. Add Mill task for Scaladoc generation
2. Configure Scaladoc options (external links, etc.)
3. Set up GitHub Pages or similar for hosting
4. Add documentation badge to README

---

### 2.5 Create Migration Guide
**Priority**: 🔵 **Low**  
**Effort**: Small  
**Category**: Documentation

**What**: Document how to migrate from TypeScript Floating UI to Scala.js version.

**Why**:
- Helps users familiar with TypeScript version
- Explains API differences and idioms
- Reduces learning curve

**Affected Files**:
- Create: `floatingUI/MIGRATION.md`

**Content**:
- Async to sync conversion
- Type system differences
- Derivable values (Either vs union types)
- Middleware data access (Option vs optional chaining)
- Import differences

---

## 3. Code Quality

### 3.1 Add Input Validation
**Priority**: 🟡 **High**  
**Effort**: Medium  
**Category**: Code Quality

**What**: Add validation for function inputs with helpful error messages.

**Why**:
- Currently no validation exists
- Invalid inputs can cause cryptic errors
- TypeScript has runtime checks in development mode

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/ComputePosition.scala`
- `floatingUI/src/io/github/nguyenyou/floatingUI/middleware/ArrowMiddleware.scala`
- `floatingUI/src/io/github/nguyenyou/floatingUI/AutoUpdate.scala`

**Specific Validations**:
1. **Arrow Middleware**:
   ```scala
   require(element != null, "Arrow element cannot be null")
   ```

2. **Placement Validation**:
   ```scala
   require(isValidPlacement(placement), s"Invalid placement: $placement")
   ```

3. **Reset Count**:
   ```scala
   if (resetCount > 50) {
     console.warn(s"Infinite loop detected: reset count exceeded 50")
   }
   ```

4. **Padding Values**:
   ```scala
   require(padding >= 0, "Padding must be non-negative")
   ```

---

### 3.2 Improve Error Handling
**Priority**: 🟡 **High**  
**Effort**: Medium  
**Category**: Code Quality

**What**: Add try-catch blocks and graceful degradation for DOM operations.

**Why**:
- DOM operations can fail in edge cases
- TypeScript has error handling in critical paths
- Improves robustness

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/DOMUtils.scala`
- `floatingUI/src/io/github/nguyenyou/floatingUI/AutoUpdate.scala`

**Specific Improvements**:
1. **IntersectionObserver Fallback** (already partially implemented):
   - Improve error messages
   - Add logging for debugging

2. **getBoundingClientRect**:
   ```scala
   try {
     element.getBoundingClientRect()
   } catch {
     case e: Exception =>
       console.warn(s"Failed to get bounding rect: ${e.getMessage}")
       // Return zero rect as fallback
       dom.DOMRect(0, 0, 0, 0)
   }
   ```

3. **getComputedStyle**:
   - Handle cases where computed style is unavailable
   - Provide sensible defaults

---

### 3.3 Add Logging/Debugging Support
**Priority**: 🟡 **High**  
**Effort**: Small  
**Category**: Code Quality

**What**: Add optional debug logging similar to TypeScript's `__DEV__` mode.

**Why**:
- Helps users debug positioning issues
- TypeScript has extensive dev-mode logging
- Can be disabled in production

**Affected Files**:
- Create: `floatingUI/src/io/github/nguyenyou/floatingUI/Debug.scala`
- Update: All middleware files

**Implementation**:
```scala
object Debug {
  var enabled: Boolean = false
  
  def log(message: String): Unit = {
    if (enabled) console.log(s"[FloatingUI] $message")
  }
  
  def warn(message: String): Unit = {
    if (enabled) console.warn(s"[FloatingUI] $message")
  }
}
```

**Usage**:
```scala
Debug.log(s"Computing position for placement: $placement")
Debug.log(s"Middleware data: $middlewareData")
Debug.warn(s"Reset count: $resetCount (limit: 50)")
```

---

### 3.4 Improve Cache Implementation
**Priority**: 🟢 **Medium**  
**Effort**: Medium  
**Category**: Code Quality

**What**: Replace mutable Map cache with WeakMap-like implementation.

**Why**:
- Current cache uses `scala.collection.mutable.Map`
- TypeScript uses `WeakMap` for automatic garbage collection
- Prevents memory leaks in long-running applications

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/DOMUtils.scala`
- `floatingUI/src/io/github/nguyenyou/floatingUI/FloatingUI.scala`

**Implementation Options**:
1. Use Scala.js `js.WeakMap` facade
2. Implement custom weak reference cache
3. Add cache size limits and LRU eviction

**Example**:
```scala
import scala.scalajs.js

object ClippingCache {
  private val cache = new js.WeakMap[dom.Element, Seq[dom.Element]]()
  
  def get(element: dom.Element): Option[Seq[dom.Element]] = {
    Option(cache.get(element).asInstanceOf[Seq[dom.Element]])
  }
  
  def set(element: dom.Element, value: Seq[dom.Element]): Unit = {
    cache.set(element, value.asInstanceOf[js.Any])
  }
}
```

---

### 3.5 Add Null Safety Checks
**Priority**: 🟢 **Medium**  
**Effort**: Small  
**Category**: Code Quality

**What**: Add explicit null checks for DOM operations.

**Why**:
- DOM APIs can return null
- Scala.js doesn't enforce null safety for JS interop
- Prevents runtime errors

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/DOMUtils.scala`
- `floatingUI/src/io/github/nguyenyou/floatingUI/Utils.scala`

**Example**:
```scala
def getDocumentElement(element: dom.Element): dom.HTMLElement = {
  val doc = element.ownerDocument
  require(doc != null, "Element has no owner document")
  
  val docElement = doc.documentElement
  require(docElement != null, "Document has no documentElement")
  
  docElement.asInstanceOf[dom.HTMLElement]
}
```

---

### 3.6 Refactor Large Functions
**Priority**: 🟢 **Medium**  
**Effort**: Medium  
**Category**: Code Quality

**What**: Break down large functions into smaller, testable units.

**Why**:
- Some functions exceed 100 lines (e.g., `getBoundingClientRect`, `getClippingRect`)
- Improves readability and testability
- Easier to maintain

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/DOMUtils.scala` (731 lines)
- `floatingUI/src/io/github/nguyenyou/floatingUI/AutoUpdate.scala` (307 lines)

**Specific Refactorings**:
1. **getBoundingClientRect** (140 lines):
   - Extract iframe traversal logic
   - Extract scale calculation
   - Extract visual offset handling

2. **getClippingRect** (50+ lines):
   - Extract boundary resolution
   - Extract rect merging logic

3. **autoUpdate** (200+ lines):
   - Extract observer setup
   - Extract cleanup logic
   - Extract refresh logic

---

### 3.7 Add Code Comments for Complex Logic
**Priority**: 🟢 **Medium**  
**Effort**: Small  
**Category**: Code Quality

**What**: Add inline comments explaining complex algorithms.

**Why**:
- Some algorithms are non-obvious (e.g., arrow positioning, overflow detection)
- TypeScript has extensive inline comments
- Helps future maintainers

**Affected Files**:
- All middleware files
- `floatingUI/src/io/github/nguyenyou/floatingUI/DOMUtils.scala`

**Example**:
```scala
// If the padding is large enough that it causes the arrow to no longer be
// centered, modify the padding so that it is centered.
val largestPossiblePadding = clientSize / 2 - arrowDimensions(length) / 2 - 1
val minPadding = math.min(paddingObject(minProp), largestPossiblePadding)
```

---

### 3.8 Standardize Naming Conventions
**Priority**: 🔵 **Low**  
**Effort**: Small  
**Category**: Code Quality

**What**: Ensure consistent naming across all files.

**Why**:
- Some inconsistencies exist (e.g., `_c` for cache)
- Improves code readability
- Follows Scala conventions

**Affected Files**:
- All source files

**Specific Changes**:
1. Use camelCase for all variables and functions
2. Use PascalCase for all types and classes
3. Avoid single-letter names except in math formulas
4. Use descriptive names for boolean flags

---

### 3.9 Add Type Aliases for Complex Types
**Priority**: 🔵 **Low**  
**Effort**: Small  
**Category**: Code Quality

**What**: Create type aliases for frequently used complex types.

**Why**:
- Improves readability
- Reduces repetition
- Makes refactoring easier

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/Types.scala`

**Example**:
```scala
type ClippingCache = scala.collection.mutable.Map[ReferenceElement, Seq[dom.Element]]
type EventHandler = js.Function1[dom.Event, Unit]
type CleanupFunction = () => Unit
```

---

## 4. Performance

### 4.1 Optimize Clipping Rect Calculation
**Priority**: 🟡 **High**  
**Effort**: Medium  
**Category**: Performance

**What**: Improve caching strategy and reduce redundant calculations.

**Why**:
- `getClippingRect` is called frequently
- Current cache is cleared after each `computePosition` call
- TypeScript uses persistent WeakMap cache

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/DOMUtils.scala`
- `floatingUI/src/io/github/nguyenyou/floatingUI/FloatingUI.scala`

**Optimizations**:
1. Use persistent cache across multiple `computePosition` calls
2. Implement cache invalidation on DOM mutations
3. Memoize intermediate calculations
4. Reduce DOM queries

---

### 4.2 Reduce Object Allocations
**Priority**: 🟢 **Medium**  
**Effort**: Medium  
**Category**: Performance

**What**: Reuse objects where possible to reduce GC pressure.

**Why**:
- Many case classes are created per positioning calculation
- Can impact performance in high-frequency scenarios (e.g., mouse tracking)

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/ComputePosition.scala`
- All middleware files

**Optimizations**:
1. Reuse `MiddlewareState` objects
2. Pool frequently allocated objects
3. Use mutable builders for intermediate results

---

### 4.3 Optimize Overflow Ancestor Traversal
**Priority**: 🟢 **Medium**  
**Effort**: Small  
**Category**: Performance

**What**: Cache overflow ancestors and invalidate on DOM changes.

**Why**:
- Traversing DOM tree is expensive
- Ancestors rarely change during positioning

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/Utils.scala`

**Implementation**:
- Cache overflow ancestors per element
- Invalidate cache on MutationObserver events

---

### 4.4 Add Performance Monitoring
**Priority**: 🔵 **Low**  
**Effort**: Small  
**Category**: Performance

**What**: Add optional performance timing for positioning operations.

**Why**:
- Helps identify bottlenecks
- Useful for optimization efforts
- Can be disabled in production

**Affected Files**:
- Create: `floatingUI/src/io/github/nguyenyou/floatingUI/Performance.scala`

**Implementation**:
```scala
object Performance {
  var enabled: Boolean = false
  
  def measure[T](label: String)(block: => T): T = {
    if (enabled) {
      val start = js.Date.now()
      val result = block
      val end = js.Date.now()
      console.log(s"[$label] ${end - start}ms")
      result
    } else {
      block
    }
  }
}
```

---

## 5. Type Safety

### 5.1 Strengthen Placement Type
**Priority**: 🟡 **High**  
**Effort**: Medium  
**Category**: Type Safety

**What**: Replace `String` type for `Placement` with sealed trait or enum.

**Why**:
- Current `type Placement = String` allows invalid values
- TypeScript uses string literal union types
- Compile-time validation prevents errors

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/Types.scala`
- All files using `Placement`

**Implementation**:
```scala
sealed trait Placement
object Placement {
  case object Top extends Placement
  case object TopStart extends Placement
  case object TopEnd extends Placement
  case object Right extends Placement
  case object RightStart extends Placement
  case object RightEnd extends Placement
  case object Bottom extends Placement
  case object BottomStart extends Placement
  case object BottomEnd extends Placement
  case object Left extends Placement
  case object LeftStart extends Placement
  case object LeftEnd extends Placement
  
  def fromString(s: String): Option[Placement] = s match {
    case "top" => Some(Top)
    case "top-start" => Some(TopStart)
    // ... etc
    case _ => None
  }
  
  def toString(p: Placement): String = p match {
    case Top => "top"
    case TopStart => "top-start"
    // ... etc
  }
}
```

---

### 5.2 Add Opaque Types for Coordinates
**Priority**: 🟡 **High**  
**Effort**: Small  
**Category**: Type Safety

**What**: Use opaque types to distinguish between different coordinate systems.

**Why**:
- Prevents mixing viewport and offset-parent coordinates
- Catches bugs at compile time
- Documents coordinate system in type signature

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/Types.scala`

**Implementation**:
```scala
opaque type ViewportCoords = Coords
opaque type OffsetParentCoords = Coords

object ViewportCoords {
  def apply(x: Double, y: Double): ViewportCoords = Coords(x, y)
}

object OffsetParentCoords {
  def apply(x: Double, y: Double): OffsetParentCoords = Coords(x, y)
}
```

---

### 5.3 Improve Derivable Type Ergonomics
**Priority**: 🟢 **Medium**  
**Effort**: Small  
**Category**: Type Safety

**What**: Add implicit conversions for `Derivable[T]` to reduce boilerplate.

**Why**:
- Current API requires `Left(value)` for static values
- TypeScript allows direct values or functions
- Improves developer experience

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/Types.scala`
- `floatingUI/src/io/github/nguyenyou/floatingUI/FloatingUI.scala`

**Implementation**:
```scala
object Derivable {
  given [T]: Conversion[T, Derivable[T]] = Left(_)
  given [T]: Conversion[MiddlewareState => T, Derivable[T]] = Right(_)
}

// Usage:
shift(ShiftOptions(mainAxis = true)) // instead of Left(ShiftOptions(...))
```

---

## 6. API Design

### 6.1 Add Builder Pattern for Options
**Priority**: 🟡 **High**  
**Effort**: Medium  
**Category**: API Design

**What**: Provide fluent builder API for complex option objects.

**Why**:
- Current API requires verbose case class construction
- Improves discoverability
- More idiomatic Scala

**Affected Files**:
- Create: `floatingUI/src/io/github/nguyenyou/floatingUI/builders/`

**Example**:
```scala
shift()
  .mainAxis(true)
  .crossAxis(false)
  .limiter(limitShift().offset(10))
  .padding(5)
```

---

### 6.2 Add Convenience Methods
**Priority**: 🟢 **Medium**  
**Effort**: Small  
**Category**: API Design

**What**: Add common middleware combinations as convenience methods.

**Why**:
- Reduces boilerplate for common use cases
- Improves developer experience
- Matches patterns from React/Vue wrappers

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/FloatingUI.scala`

**Examples**:
```scala
// Tooltip preset
def tooltipMiddleware(arrowElement: dom.HTMLElement): Seq[Middleware] = Seq(
  offset(8),
  flip(),
  shift(ShiftOptions(padding = 5)),
  arrow(ArrowOptions(element = arrowElement, padding = 5))
)

// Dropdown preset
def dropdownMiddleware(): Seq[Middleware] = Seq(
  offset(4),
  flip(),
  shift(ShiftOptions(padding = 8))
)
```

---

### 6.3 Improve computePosition API
**Priority**: 🟢 **Medium**  
**Effort**: Small  
**Category**: API Design

**What**: Add overload that accepts `ComputePositionConfig` directly.

**Why**:
- Current API has many parameters
- Config object is more flexible
- Matches TypeScript API

**Affected Files**:
- `floatingUI/src/io/github/nguyenyou/floatingUI/FloatingUI.scala`

**Implementation**:
```scala
def computePosition(
  reference: ReferenceElement,
  floating: dom.HTMLElement,
  config: ComputePositionConfig
): ComputePositionReturn = {
  // ... existing implementation
}

// Keep existing convenience overload
def computePosition(
  reference: ReferenceElement,
  floating: dom.HTMLElement,
  placement: Placement = "bottom",
  strategy: Strategy = "absolute",
  middleware: Seq[Middleware] = Seq.empty
): ComputePositionReturn = {
  computePosition(reference, floating, ComputePositionConfig(
    placement, strategy, middleware, platform
  ))
}
```

---

### 6.4 Add Extension Methods
**Priority**: 🔵 **Low**  
**Effort**: Small  
**Category**: API Design

**What**: Provide extension methods on DOM elements for common operations.

**Why**:
- More idiomatic Scala
- Reduces import boilerplate
- Improves discoverability

**Affected Files**:
- Create: `floatingUI/src/io/github/nguyenyou/floatingUI/syntax/`

**Example**:
```scala
extension (reference: dom.Element) {
  def positionFloating(
    floating: dom.HTMLElement,
    placement: Placement = "bottom",
    middleware: Seq[Middleware] = Seq.empty
  ): ComputePositionReturn = {
    computePosition(reference, floating, placement, middleware = middleware)
  }
}

// Usage:
referenceElement.positionFloating(floatingElement, "top")
```

---

## Implementation Roadmap

### Phase 1: Critical Foundation (Weeks 1-4)
1. ✅ Create comprehensive test suite (1.1)
2. ✅ Add input validation (3.1)
3. ✅ Expand Scaladoc comments (2.1)

### Phase 2: Quality & Safety (Weeks 5-8)
4. ✅ Improve error handling (3.2)
5. ✅ Add property-based testing (1.2)
6. ✅ Strengthen placement type (5.1)
7. ✅ Add opaque types (5.2)
8. ✅ Optimize clipping rect (4.1)

### Phase 3: Developer Experience (Weeks 9-12)
9. ✅ Create usage examples (2.2)
10. ✅ Add logging/debugging (3.3)
11. ✅ Add builder pattern (6.1)
12. ✅ Improve Derivable ergonomics (5.3)
13. ✅ Add convenience methods (6.2)

### Phase 4: Polish & Performance (Weeks 13-16)
14. ✅ Add visual regression tests (1.3)
15. ✅ Improve cache implementation (3.4)
16. ✅ Reduce object allocations (4.2)
17. ✅ Add README and migration guide (2.3, 2.5)
18. ✅ Refactor large functions (3.6)

### Phase 5: Final Touches (Weeks 17-20)
19. ✅ Add benchmark tests (1.4)
20. ✅ Generate API documentation (2.4)
21. ✅ Add null safety checks (3.5)
22. ✅ Optimize overflow traversal (4.3)
23. ✅ Add code comments (3.7)
24. ✅ Remaining low-priority items

---

## Success Metrics

- **Test Coverage**: >90% line coverage
- **Documentation**: 100% public API documented
- **Performance**: Within 10% of TypeScript implementation
- **Type Safety**: Zero runtime type errors in tests
- **Developer Experience**: Positive feedback from early adopters

---

## Conclusion

This roadmap transforms the Floating UI Scala.js port from a functional implementation to a production-grade library with:

- ✅ Comprehensive test coverage
- ✅ Excellent documentation
- ✅ Strong type safety
- ✅ Optimized performance
- ✅ Great developer experience

The prioritization ensures critical items (testing, validation) are addressed first, followed by quality improvements and developer experience enhancements.

