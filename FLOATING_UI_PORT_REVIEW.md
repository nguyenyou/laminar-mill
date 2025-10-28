# Floating UI DOM → Scala.js Port: Comprehensive Code Review

**Review Date:** 2025-10-28  
**TypeScript Source:** `floating-ui/packages/dom/src/`  
**Scala.js Port:** `floatingUI/src/io/github/nguyenyou/floatingUI/`

---

## Executive Summary

### Overall Assessment: ⚠️ **INCOMPLETE PORT - CRITICAL ISSUES FOUND**

The Scala.js port is **NOT a facade** - it's a **native reimplementation** of the Floating UI DOM library. While this is architecturally impressive, the port has **significant discrepancies** from the TypeScript original that affect correctness and completeness.

**Port Status:**
- ✅ **Core architecture**: Correctly implemented
- ⚠️ **Type system**: Mostly correct with some deviations
- ❌ **computePosition**: **CRITICAL - Missing cache implementation**
- ⚠️ **Platform methods**: Incomplete - missing Promise support
- ⚠️ **Middleware**: Need detailed verification
- ⚠️ **Utility functions**: Need verification against TypeScript originals

---

## 1. File-by-File Comparison

### 1.1 Main Entry Point

| TypeScript | Scala.js | Status |
|------------|----------|--------|
| `index.ts` | `FloatingUI.scala` | ⚠️ **CRITICAL ISSUE** |

**CRITICAL DISCREPANCY:**

**TypeScript `index.ts` (lines 14-29):**
```typescript
export const computePosition = (
  reference: ReferenceElement,
  floating: FloatingElement,
  options?: Partial<ComputePositionConfig>,
) => {
  // This caches the expensive `getClippingElementAncestors` function so that
  // multiple lifecycle resets re-use the same result. It only lives for a
  // single call. If other functions become expensive, we can add them as well.
  const cache = new Map<ReferenceElement, Array<Element>>();
  const mergedOptions = {platform, ...options};
  const platformWithCache = {...mergedOptions.platform, _c: cache};
  return computePositionCore(reference, floating, {
    ...mergedOptions,
    platform: platformWithCache,
  });
};
```

**Scala.js `FloatingUI.scala` (lines 41-55):**
```scala
def computePosition(
  reference: ReferenceElement,
  floating: dom.HTMLElement,
  placement: Placement = "bottom",
  strategy: Strategy = "absolute",
  middleware: Seq[Middleware] = Seq.empty
): ComputePositionReturn = {
  val config = ComputePositionConfig(
    placement = placement,
    strategy = strategy,
    middleware = middleware,
    platform = platform
  )
  ComputePosition.computePosition(reference, floating, config)
}
```

**❌ MISSING:**
1. **Cache implementation** - The TypeScript version creates a `Map<ReferenceElement, Array<Element>>` cache and injects it into the platform as `_c`. This is **critical for performance** as it prevents expensive `getClippingElementAncestors` calls from being repeated during middleware lifecycle resets.
2. **Options merging** - TypeScript accepts `Partial<ComputePositionConfig>` and merges with defaults. Scala version uses individual parameters instead.

**Impact:** Performance degradation - expensive clipping ancestor calculations will be repeated unnecessarily.

---

### 1.2 Type Definitions

| TypeScript | Scala.js | Status |
|------------|----------|--------|
| `types.ts` | `Types.scala` | ⚠️ **PARTIAL** |

**Key Discrepancies:**

#### 1.2.1 Platform Interface - Promise Support

**TypeScript `types.ts` (lines 61-92):**
```typescript
export interface Platform {
  // Required
  getElementRects: (args: {
    reference: ReferenceElement;
    floating: FloatingElement;
    strategy: Strategy;
  }) => Promisable<ElementRects>;  // ← Can return Promise!
  getClippingRect: (args: {
    element: Element;
    boundary: Boundary;
    rootBoundary: RootBoundary;
    strategy: Strategy;
  }) => Promisable<Rect>;  // ← Can return Promise!
  getDimensions: (element: Element) => Promisable<Dimensions>;  // ← Can return Promise!
  // ... all methods can return Promisable<T>
}
```

**Scala.js `Types.scala` (lines 252-309):**
```scala
trait Platform {
  // Required methods
  def getElementRects(reference: ReferenceElement, floating: dom.HTMLElement, strategy: Strategy): ElementRects
  def getDimensions(element: dom.Element): Dimensions
  def getClippingRect(element: Any, boundary: String, rootBoundary: String, strategy: Strategy): Rect
  // ... all methods return synchronous values
}
```

**❌ MISSING:** 
- `Promisable<T>` support - TypeScript allows all platform methods to return `Promise<T>` for async operations
- This is **critical** for custom platform implementations that might need async operations

**Note:** The current DOM platform implementation is synchronous, so this doesn't affect the default case, but it **breaks the contract** for custom platforms.

#### 1.2.2 Middleware Interface

**TypeScript `types.ts` (lines 152-156):**
```typescript
export type Middleware = Prettify<
  Omit<CoreMiddleware, 'fn'> & {
    fn(state: MiddlewareState): Promisable<MiddlewareReturn>;  // ← Can return Promise!
  }
>;
```

**Scala.js `Types.scala` (lines 197-201):**
```scala
trait Middleware {
  def name: String
  def fn(state: MiddlewareState): MiddlewareReturn  // ← Synchronous only
}
```

**❌ MISSING:** Promise support in middleware `fn` - TypeScript allows middleware to be async.

---

### 1.3 Platform Implementation

| TypeScript | Scala.js | Status |
|------------|----------|--------|
| `platform.ts` | `DOMPlatform.scala` | ✅ **CORRECT** |

**Status:** The platform implementation correctly delegates to utility functions. Structure matches TypeScript.

---

### 1.4 AutoUpdate

| TypeScript | Scala.js | Status |
|------------|----------|--------|
| `autoUpdate.ts` | `AutoUpdate.scala` | ✅ **EXCELLENT PORT** |

**Status:** This is an **exemplary port**. The Scala implementation:
- ✅ Correctly implements all options with conditional defaults
- ✅ Properly handles virtual elements via `unwrapElement`
- ✅ Implements `observeMove` with IntersectionObserver
- ✅ Handles ResizeObserver with reobserve frame logic
- ✅ Implements animation frame loop
- ✅ Proper cleanup function
- ✅ Matches TypeScript logic line-by-line

**Minor differences:**
- Uses `Option[Boolean]` for `elementResize` and `layoutShift` instead of direct boolean (acceptable pattern)
- Timeout ID uses `Int` instead of `NodeJS.Timeout` (platform difference, acceptable)

---

## 2. Missing Implementations

### 2.1 Core Missing Features

1. **❌ Cache mechanism in `computePosition`**
   - **File:** `FloatingUI.scala`
   - **Impact:** Performance degradation
   - **Fix Required:** Implement cache Map and inject into platform as `_c`

2. **❌ Promise/async support in Platform interface**
   - **File:** `Types.scala`
   - **Impact:** Breaks contract for custom platforms
   - **Fix Required:** Change return types to support Future/Promise

3. **❌ Promise/async support in Middleware interface**
   - **File:** `Types.scala`
   - **Impact:** Middleware cannot perform async operations
   - **Fix Required:** Change `fn` return type to support Future/Promise

### 2.2 Utility Functions - Need Verification

The following utility functions need line-by-line verification:

| TypeScript File | Scala.js Location | Verification Status |
|----------------|-------------------|---------------------|
| `utils/getBoundingClientRect.ts` | `Utils.scala` or `DOMUtils.scala` | ⏳ Pending |
| `utils/getCssDimensions.ts` | `DOMUtils.scala` | ⏳ Pending |
| `utils/getDocumentRect.ts` | `DOMUtils.scala` | ⏳ Pending |
| `utils/getHTMLOffset.ts` | `DOMUtils.scala` | ⏳ Pending |
| `utils/getRectRelativeToOffsetParent.ts` | `DOMUtils.scala` | ⏳ Pending |
| `utils/getViewportRect.ts` | `DOMUtils.scala` | ⏳ Pending |
| `utils/getVisualOffsets.ts` | `DOMUtils.scala` | ⏳ Pending |
| `utils/getWindowScrollBarX.ts` | `DOMUtils.scala` | ⏳ Pending |
| `utils/isStaticPositioned.ts` | `DOMUtils.scala` | ⏳ Pending |
| `utils/rectsAreEqual.ts` | `Utils.scala` | ⏳ Pending |
| `utils/unwrapElement.ts` | `Utils.scala` | ⏳ Pending |

### 2.3 Platform Functions - Need Verification

| TypeScript File | Scala.js Location | Verification Status |
|----------------|-------------------|---------------------|
| `platform/convertOffsetParentRelativeRectToViewportRelativeRect.ts` | `DOMUtils.scala` | ⏳ Pending |
| `platform/getClientRects.ts` | `DOMPlatform.scala` | ⏳ Pending |
| `platform/getClippingRect.ts` | `DOMUtils.scala` | ⏳ Pending |
| `platform/getDimensions.ts` | `DOMPlatform.scala` | ⏳ Pending |
| `platform/getDocumentElement.ts` | `Utils.scala` | ⏳ Pending |
| `platform/getElementRects.ts` | `DOMPlatform.scala` | ⏳ Pending |
| `platform/getOffsetParent.ts` | `DOMUtils.scala` | ⏳ Pending |
| `platform/getScale.ts` | `DOMUtils.scala` | ⏳ Pending |
| `platform/isElement.ts` | `DOMPlatform.scala` | ⏳ Pending |
| `platform/isRTL.ts` | `Types.scala` (default impl) | ⏳ Pending |

### 2.4 Middleware - Need Verification

| TypeScript File | Scala.js File | Verification Status |
|----------------|---------------|---------------------|
| `middleware.ts` (exports) | `FloatingUI.scala` | ⏳ Pending |
| Individual middleware in `@floating-ui/core` | `middleware/*.scala` | ⏳ Pending |

---

## 3. Type Mapping Verification

### 3.1 Correct Mappings ✅

| TypeScript | Scala.js | Status |
|------------|----------|--------|
| `Side` | `type Side = "top" \| "right" \| "bottom" \| "left"` | ✅ |
| `Alignment` | `type Alignment = "start" \| "end"` | ✅ |
| `Strategy` | `type Strategy = "absolute" \| "fixed"` | ✅ |
| `Coords` | `case class Coords(x: Double, y: Double)` | ✅ |
| `Dimensions` | `case class Dimensions(width: Double, height: Double)` | ✅ |
| `Rect` | `case class Rect(x: Double, y: Double, width: Double, height: Double)` | ✅ |

### 3.2 Questionable Mappings ⚠️

| TypeScript | Scala.js | Issue |
|------------|----------|-------|
| `Promisable<T> = T \| Promise<T>` | Not implemented | ❌ Missing async support |
| `Derivable<T> = (state: MiddlewareState) => T` | `Either[T, MiddlewareState => T]` | ⚠️ Different pattern (acceptable) |
| `Boundary = 'clippingAncestors' \| Element \| Array<Element> \| Rect` | `String` | ❌ Too permissive, loses type safety |

---

## 4. Algorithm Correctness - Detailed Verification Needed

The following files contain complex algorithms that need line-by-line verification:

1. **ComputePosition.scala** - Core positioning algorithm
2. **DetectOverflow.scala** - Overflow detection
3. **ComputeCoordsFromPlacement.scala** - Coordinate calculation
4. **All middleware files** - Each middleware algorithm

**Status:** ⏳ **PENDING DETAILED REVIEW**

---

## 5. Recommendations

### 5.1 Critical Fixes Required

1. **Implement cache in `computePosition`**
   ```scala
   def computePosition(
     reference: ReferenceElement,
     floating: dom.HTMLElement,
     options: ComputePositionConfig
   ): ComputePositionReturn = {
     // Create cache for this call
     val cache = scala.collection.mutable.Map[ReferenceElement, Seq[dom.Element]]()
     
     // Inject cache into platform (need to add _c field to Platform trait)
     val platformWithCache = // ... inject cache
     
     val configWithCache = options.copy(platform = platformWithCache)
     ComputePosition.computePosition(reference, floating, configWithCache)
   }
   ```

2. **Add Promise support to Platform and Middleware**
   - Consider using `scala.concurrent.Future` or keeping synchronous for simplicity
   - Document the decision and limitations

3. **Fix Boundary type**
   - Change from `String` to proper union type or sealed trait

### 5.2 Verification Tasks

1. **Line-by-line comparison of all utility functions**
2. **Line-by-line comparison of all platform functions**
3. **Line-by-line comparison of all middleware implementations**
4. **Algorithm verification for core positioning logic**

### 5.3 Testing Requirements

1. **Port TypeScript test suite** to verify correctness
2. **Add integration tests** comparing with actual @floating-ui/dom library
3. **Performance benchmarks** to verify cache implementation

---

## 6. Detailed Utility Function Verification

### 6.1 getBoundingClientRect - ❌ **CRITICAL SIMPLIFICATION**

**TypeScript** (`utils/getBoundingClientRect.ts`, 82 lines):
- ✅ Handles scale calculation with `includeScale` parameter
- ✅ Handles visual offsets for fixed strategy
- ✅ **Handles iframe traversal** (lines 46-78) - Critical for cross-iframe positioning
- ✅ Applies scale transformations correctly
- ✅ Handles offsetParent parameter

**Scala.js** (`Utils.scala`, lines 279-293):
```scala
def getBoundingClientRect(element: dom.Element): ClientRectObject = {
  val rect = element.getBoundingClientRect()
  ClientRectObject(
    x = rect.x,
    y = rect.y,
    width = rect.width,
    height = rect.height,
    top = rect.top,
    right = rect.right,
    bottom = rect.bottom,
    left = rect.left
  )
}
```

**❌ MISSING:**
1. **No `includeScale` parameter** - Cannot handle scaled elements
2. **No `isFixedStrategy` parameter** - Cannot handle fixed positioning correctly
3. **No `offsetParent` parameter** - Cannot handle relative positioning
4. **No iframe traversal** - **CRITICAL** - Will fail for cross-iframe positioning
5. **No visual offsets** - Will fail for fixed strategy
6. **No scale application** - Will fail for transformed elements

**Impact:** **CRITICAL** - This function is used throughout the codebase. The simplified version will produce **incorrect coordinates** for:
- Scaled elements (CSS transforms)
- Fixed positioned elements
- Cross-iframe scenarios
- Elements with visual offsets

**Comment in code says:** "simplified version for autoUpdate" - but this is used elsewhere too!

### 6.2 getClippingElementAncestors - ❌ **MISSING CACHE**

**TypeScript** (`platform/getClippingRect.ts`, lines 104-155):
```typescript
function getClippingElementAncestors(
  element: Element,
  cache: PlatformWithCache['_c'],  // ← Cache parameter!
): Array<Element> {
  const cachedResult = cache.get(element);  // ← Check cache first
  if (cachedResult) {
    return cachedResult;
  }

  // ... expensive computation ...

  cache.set(element, result);  // ← Store in cache
  return result;
}
```

**Scala.js** (`DOMUtils.scala`, lines 463-506):
```scala
def getClippingElementAncestors(element: dom.Element): Seq[dom.Element] = {
  // ❌ NO CACHE PARAMETER
  // ❌ NO CACHE CHECK

  var result = Utils.getOverflowAncestors(element)
    .filter(el => el.isInstanceOf[dom.Element] && getNodeName(el.asInstanceOf[dom.Node]) != "body")
    .map(_.asInstanceOf[dom.Element])

  // ... computation ...

  result  // ❌ NO CACHE STORAGE
}
```

**❌ MISSING:**
1. **No cache parameter** - Cannot receive cache from platform
2. **No cache check** - Always recomputes expensive operation
3. **No cache storage** - Cannot store result for reuse

**Impact:** **CRITICAL PERFORMANCE** - This is the **exact function** that the cache was designed to optimize! The TypeScript comment says:
> "This caches the expensive `getClippingElementAncestors` function so that multiple lifecycle resets re-use the same result."

Without caching, this expensive DOM traversal will be repeated **multiple times per positioning calculation** when middleware causes lifecycle resets.

**Algorithm correctness:** ✅ The actual algorithm logic appears correct (lines 463-506 match TypeScript lines 113-154).

---

### 6.3 Arrow Middleware - ✅ **EXCELLENT PORT**

**TypeScript** (`core/src/middleware/arrow.ts`, 118 lines)
**Scala.js** (`middleware/ArrowMiddleware.scala`, 132 lines)

**Status:** ✅ **CORRECT** - Algorithm matches line-by-line

**Verified:**
- ✅ Padding object calculation
- ✅ Axis and length calculations
- ✅ Arrow dimensions retrieval
- ✅ Offset parent handling
- ✅ Client size calculation with fallback
- ✅ Center-to-reference calculation
- ✅ Min/max padding clamping
- ✅ Offset calculation
- ✅ shouldAddOffset logic (lines 93-100 TS ↔ lines 100-104 Scala)
- ✅ Alignment offset calculation
- ✅ Return value structure

**Minor differences (acceptable):**
- Scala uses `Option` for nullable values instead of `null` checks
- Scala uses pattern matching instead of ternary operators
- Scala uses `Map` for data instead of object literal

**Note:** TypeScript uses `async fn` but doesn't actually await anything except platform methods. Scala version is synchronous, which is fine since the current platform is synchronous.

---

## 7. Summary of Middleware Verification

All 8 middleware files are present:

| Middleware | TypeScript | Scala.js | Status |
|------------|-----------|----------|--------|
| arrow | `core/src/middleware/arrow.ts` | `ArrowMiddleware.scala` | ✅ Verified correct |
| autoPlacement | `core/src/middleware/autoPlacement.ts` | `AutoPlacementMiddleware.scala` | ⏳ Needs verification |
| flip | `core/src/middleware/flip.ts` | `FlipMiddleware.scala` | ⏳ Needs verification |
| hide | `core/src/middleware/hide.ts` | `HideMiddleware.scala` | ⏳ Needs verification |
| inline | `core/src/middleware/inline.ts` | `InlineMiddleware.scala` | ⏳ Needs verification |
| offset | `core/src/middleware/offset.ts` | `OffsetMiddleware.scala` | ⏳ Needs verification |
| shift | `core/src/middleware/shift.ts` | `ShiftMiddleware.scala` | ⏳ Needs verification |
| size | `core/src/middleware/size.ts` | `SizeMiddleware.scala` | ⏳ Needs verification |

**Recommendation:** Based on the quality of the `arrow` middleware port, the other middleware are likely well-implemented, but they still need line-by-line verification.

---

## 8. Complete File Mapping

### 8.1 Core Files

| TypeScript | Scala.js | Lines (TS) | Lines (Scala) | Status |
|------------|----------|------------|---------------|--------|
| `index.ts` | `FloatingUI.scala` | 25 | ~100 | ❌ Missing cache |
| `types.ts` | `Types.scala` | ~200 | 495 | ⚠️ Missing Promise support |
| `platform.ts` | `DOMPlatform.scala` | 25 | 142 | ✅ Correct structure |
| `autoUpdate.ts` | `AutoUpdate.scala` | 239 | 307 | ✅ Excellent port |

### 8.2 Utility Functions

| TypeScript | Scala.js Location | Status |
|------------|-------------------|--------|
| `utils/getBoundingClientRect.ts` (82 lines) | `Utils.scala` (15 lines) | ❌ **CRITICAL - Severely simplified** |
| `utils/getCssDimensions.ts` | `DOMUtils.scala` | ⏳ Needs verification |
| `utils/getDocumentRect.ts` | `DOMUtils.scala` | ⏳ Needs verification |
| `utils/getVisualOffsets.ts` | `DOMUtils.scala` | ⏳ Needs verification |
| `utils/rectsAreEqual.ts` | `Utils.scala` | ⏳ Needs verification |
| `utils/unwrapElement.ts` | `Utils.scala` | ⏳ Needs verification |

### 8.3 Platform Functions

| TypeScript | Scala.js Location | Status |
|------------|-------------------|--------|
| `platform/getClippingRect.ts` (207 lines) | `DOMUtils.scala` (~90 lines) | ⚠️ Algorithm OK, missing cache |
| `platform/getClippingElementAncestors` | `DOMUtils.scala` | ❌ **Missing cache parameter** |
| `platform/getScale.ts` | `DOMUtils.scala` | ⏳ Needs verification |
| `platform/getOffsetParent.ts` | `DOMUtils.scala` | ⏳ Needs verification |
| `platform/getElementRects.ts` | `DOMPlatform.scala` | ⏳ Needs verification |

### 8.4 Middleware (All Present)

| Middleware | Status |
|------------|--------|
| arrow | ✅ Verified correct |
| autoPlacement | ⏳ Needs verification |
| flip | ⏳ Needs verification |
| hide | ⏳ Needs verification |
| inline | ⏳ Needs verification |
| offset | ⏳ Needs verification |
| shift | ⏳ Needs verification |
| size | ⏳ Needs verification |

---

## 9. Conclusion

This is an **ambitious and well-structured port** that demonstrates **deep understanding** of the Floating UI architecture. However, it is **NOT production-ready** due to:

### Critical Issues (Must Fix)

1. ❌ **Missing cache mechanism in `computePosition`** - Critical performance issue
2. ❌ **Missing cache in `getClippingElementAncestors`** - Critical performance issue
3. ❌ **Severely simplified `getBoundingClientRect`** - **CRITICAL CORRECTNESS ISSUE**
   - Missing: scale, visual offsets, iframe traversal, offsetParent handling
   - Will produce incorrect coordinates for scaled elements, fixed positioning, cross-iframe scenarios

### Architectural Issues (Should Fix)

4. ❌ **Missing Promise/async support** - Breaks extensibility contract for custom platforms
5. ⚠️ **Boundary type too permissive** - Uses `String` instead of proper union type

### Verification Needed

6. ⏳ **~20 utility functions** - Need line-by-line verification
7. ⏳ **7 middleware** - Need line-by-line verification (arrow is verified ✅)
8. ⏳ **Core positioning algorithm** - Need verification

### Quality Assessment

**What's Good:**
- ✅ Excellent architecture and code organization
- ✅ AutoUpdate is a perfect port
- ✅ Arrow middleware is correctly implemented
- ✅ Type system is mostly correct
- ✅ Handles virtual elements properly

**What's Missing:**
- ❌ Performance optimizations (cache)
- ❌ Full `getBoundingClientRect` implementation
- ❌ Async/Promise support
- ⏳ Comprehensive testing

**Recommendation:** **DO NOT USE IN PRODUCTION** until critical issues are fixed and verification is complete.

**Estimated work remaining:** 40-80 hours of detailed verification and fixes.

---

## 10. Immediate Action Items

### Priority 1 - Critical Correctness Issues

1. **Implement full `getBoundingClientRect`**
   - Add `includeScale`, `isFixedStrategy`, `offsetParent` parameters
   - Implement iframe traversal logic
   - Implement visual offsets
   - Implement scale application

2. **Implement cache in `computePosition`**
   - Add `_c` field to Platform trait
   - Create cache Map in computePosition
   - Inject cache into platform

### Priority 2 - Verification Tasks

3. **Verify all utility functions** against TypeScript originals
4. **Verify all platform functions** against TypeScript originals
5. **Verify all middleware** against TypeScript originals

### Priority 3 - Architecture

6. **Add Promise/Future support** to Platform and Middleware interfaces
7. **Fix Boundary type** to use proper union type
8. **Port test suite** for verification


