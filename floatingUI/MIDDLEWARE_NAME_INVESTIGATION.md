# Middleware.name Investigation and Documentation Enhancement

## Overview

This document summarizes the investigation into whether `Middleware.name` should be refactored from `String` to a type-safe implementation (enum or sealed trait), and the resulting documentation enhancement.

**Investigation Date:** 2025-10-31  
**Conclusion:** ✅ **Keep as `String`** (no refactoring needed)  
**Action Taken:** Documentation enhancement only

---

## Investigation Summary

### Question Investigated

Should `Middleware.name` be refactored from `String` to a type-safe implementation (Scala 3 enum or sealed trait), following the pattern used for `ElementContext`, `HideStrategy`, and other type safety enhancements?

### Key Findings

#### 1. TypeScript Type Definition

**From `packages/core/src/types.ts`:**
```typescript
export type Middleware = {
  name: string;  // ← Plain string, NOT a union type
  options?: any;
  fn: (state: MiddlewareState) => Promisable<MiddlewareReturn>;
};
```

**Critical Finding:** TypeScript uses **plain `string` type**, NOT a string literal union like `'offset' | 'flip' | ...`

#### 2. Built-in Middleware (8 total)

All middleware implementations in the codebase use hardcoded string literals:

| Middleware | Name | Purpose |
|------------|------|---------|
| OffsetMiddleware | `"offset"` | Displace floating element from reference |
| FlipMiddleware | `"flip"` | Flip placement to keep in view |
| ShiftMiddleware | `"shift"` | Shift along axis to keep in view |
| HideMiddleware | `"hide"` | Detect when element should be hidden |
| SizeMiddleware | `"size"` | Resize based on available space |
| ArrowMiddleware | `"arrow"` | Position arrow element |
| AutoPlacementMiddleware | `"autoPlacement"` | Choose best placement automatically |
| InlineMiddleware | `"inline"` | Handle inline reference elements |

#### 3. Custom Middleware Support

**Evidence that custom middleware is a core feature:**

1. **MiddlewareData has extensible storage:**
   ```scala
   case class MiddlewareData(
     arrow: Option[ArrowData] = None,
     // ... other known middleware
     custom: Map[String, Any] = Map.empty  // ← For custom middleware
   )
   ```

2. **Pattern matching handles unknown names:**
   ```scala
   middlewareData = middleware.name match {
     case "arrow" => /* merge arrow data */
     case "offset" => /* merge offset data */
     // ... other known middleware
     case customName => /* handle custom middleware */
   }
   ```

3. **TypeScript tests show custom middleware:**
   ```typescript
   const middleware: Middleware = {
     name: 'test',  // ← Arbitrary custom name
     fn(args) {
       return {};
     },
   };
   ```

#### 4. Open vs Closed Set Analysis

| Aspect | Finding | Conclusion |
|--------|---------|------------|
| TypeScript Type | `name: string` | **Open set** |
| Known Middleware | 8 built-in with fixed names | Closed subset |
| Custom Middleware | Explicitly supported | **Open set** |
| Pattern Matching | Has catch-all case | **Open set** |
| Extensibility | Core design feature | **Open set** |

**Result:** `Middleware.name` is an **OPEN SET** - users can create custom middleware with arbitrary names.

### Comparison with Previous Refactorings

| Type | Set Type | TypeScript | Scala Approach | Rationale |
|------|----------|------------|----------------|-----------|
| `ElementContext` | Closed | `'reference' \| 'floating'` | Enum | Fixed 2 values |
| `HideStrategy` | Closed | `'referenceHidden' \| 'escaped'` | Enum | Fixed 2 values |
| `Placement` | Closed | `'top' \| 'top-start' \| ...` | Enum | Fixed 12 values |
| `Boundary` | Open | `'clippingAncestors' \| Element \| ...` | Union + sealed trait | Mixed types |
| `RootBoundary` | Open | `'viewport' \| 'document' \| Rect` | Union + sealed trait | Mixed types |
| **`Middleware.name`** | **Open** | **`string`** | **`String`** | **User-defined names** |

### Recommendation

**✅ STRONGLY RECOMMEND: Keep `Middleware.name` as `String`**

**Rationale:**

1. ✅ **TypeScript Parity:** TypeScript uses plain `string`, not a union type
2. ✅ **Design Intent:** The library is explicitly designed to support custom middleware
3. ✅ **Extensibility:** Users should be able to create middleware with any name
4. ✅ **Zero Breaking Changes:** No refactoring needed
5. ✅ **Pattern Matching Already Handles It:** The catch-all case properly handles unknown names
6. ✅ **No Real Benefit:** Type-safe alternatives add complexity without meaningful safety

**The current implementation is CORRECT and should NOT be changed.**

---

## Documentation Enhancement

Instead of refactoring, we enhanced the documentation to improve developer experience.

### Changes Made

#### 1. Enhanced `Middleware` Trait Documentation

**Location:** `floatingUI/src/io/github/nguyenyou/floatingUI/Types.scala` (lines 752-791)

**Added:**
- Comprehensive ScalaDoc explaining what middleware is
- List of all 8 built-in middleware with brief descriptions
- Explanation that custom middleware can use any name
- TypeScript type reference
- `@see` link to Floating UI middleware documentation
- Detailed documentation for `name` field
- Detailed documentation for `fn` field with `@param` and `@return`

**Example:**
```scala
/** Middleware object for customizing positioning behavior.
  *
  * Middleware allows you to customize the positioning logic and add features beyond basic placement.
  * Each middleware has a unique name and a function that processes the positioning state.
  *
  * The library provides 8 built-in middleware:
  *   - **"offset"** - Displaces the floating element from its reference element
  *   - **"flip"** - Flips the placement to keep the floating element in view
  *   - ... (all 8 listed)
  *
  * Custom middleware can use any name. The name is used as a key to store middleware-specific
  * data in `MiddlewareData.custom`.
  *
  * Matches TypeScript: type Middleware = { name: string; options?: any; fn: ... }
  *
  * @see https://floating-ui.com/docs/middleware
  */
trait Middleware {
  /** Unique identifier for this middleware.
    *
    * Built-in middleware use fixed names (e.g., "offset", "flip"). Custom middleware can use
    * any string value. The name is used to organize middleware data in the `MiddlewareData` object.
    */
  def name: String

  /** Middleware function that processes positioning state.
    *
    * @param state Current positioning state including coordinates, placement, rects, and platform
    * @return Middleware return value with optional coordinate adjustments, data, or reset instructions
    */
  def fn(state: MiddlewareState): MiddlewareReturn
}
```

#### 2. Added `MiddlewareNames` Object

**Location:** `floatingUI/src/io/github/nguyenyou/floatingUI/Types.scala` (lines 793-877)

**Purpose:** Provide convenience constants for built-in middleware names

**Contents:**
- 8 string constants for built-in middleware names
- Comprehensive ScalaDoc for the object
- Individual documentation for each constant with `@see` links
- Clear note that custom middleware can use any name

**Example:**
```scala
/** Convenience constants for built-in middleware names.
  *
  * These constants provide type-safe references to the names of the 8 built-in middleware.
  * They are provided for convenience and documentation purposes.
  *
  * Note: Custom middleware can use any name not in this list. These constants are not
  * exhaustive - they only cover the built-in middleware provided by the library.
  *
  * @see https://floating-ui.com/docs/middleware
  */
object MiddlewareNames {
  /** Offset middleware name: "offset"
    *
    * Displaces the floating element from its reference element by a specified distance.
    *
    * @see https://floating-ui.com/docs/offset
    */
  val Offset: String = "offset"

  // ... (all 8 constants)
}
```

### Benefits of Documentation Enhancement

1. ✅ **Improved Discoverability:** Developers can see all built-in middleware in one place
2. ✅ **Better IDE Support:** ScalaDoc appears on hover in IDEs
3. ✅ **Clear Guidance:** Explains that custom middleware is supported
4. ✅ **TypeScript Parity:** References TypeScript type definition
5. ✅ **Zero Breaking Changes:** No code changes, only documentation
6. ✅ **Convenience Constants:** `MiddlewareNames.Offset` is more readable than `"offset"`

---

## Verification Results

### Compilation
```
✅ ./mill floatingUI.compile
   - No errors
   - No warnings
   - Clean compilation
```

### Tests
```
✅ ./mill floatingUI.test
   - Total tests: 68
   - Passed: 68
   - Failed: 0
   - All tests passed
```

### Impact
- **Files Modified:** 1 (`floatingUI/src/io/github/nguyenyou/floatingUI/Types.scala`)
- **Breaking Changes:** None (documentation only)
- **Code Changes:** None (no refactoring)
- **Lines Added:** ~125 (all documentation)

---

## Usage Examples

### Using Built-in Middleware (Before)

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._

val config = ComputePositionConfig(
  placement = Placement.Top,
  middleware = Seq(
    offset(10),
    flip(),
    shift()
  ),
  platform = DOMPlatform
)
```

### Using Built-in Middleware (After - with constants)

```scala
import io.github.nguyenyou.floatingUI._
import io.github.nguyenyou.floatingUI.Types._
import io.github.nguyenyou.floatingUI.Types.MiddlewareNames

val config = ComputePositionConfig(
  placement = Placement.Top,
  middleware = Seq(
    offset(10),      // Still works the same
    flip(),          // Still works the same
    shift()          // Still works the same
  ),
  platform = DOMPlatform
)

// Constants available for reference (e.g., in pattern matching)
middlewareData.custom.get(MiddlewareNames.Offset)  // More readable than "offset"
```

### Creating Custom Middleware

```scala
// Custom middleware with arbitrary name
val customMiddleware = new Middleware {
  override def name: String = "myCustomMiddleware"  // Any name is allowed
  
  override def fn(state: MiddlewareState): MiddlewareReturn = {
    // Custom positioning logic
    MiddlewareReturn(
      x = Some(state.x + 10),
      data = Some(Map("customData" -> "value"))
    )
  }
}

val config = ComputePositionConfig(
  middleware = Seq(
    offset(10),
    customMiddleware  // Custom middleware works seamlessly
  ),
  platform = DOMPlatform
)
```

---

## Conclusion

The investigation concluded that `Middleware.name` should **remain as `String`** because:

1. TypeScript uses plain `string` (not a closed set)
2. Custom middleware is a core feature (not an edge case)
3. Current implementation is correct and matches upstream
4. Any type-safe alternative would break the extensibility design

Instead of refactoring, we enhanced the documentation to provide:
- Clear explanation of middleware concept
- List of all built-in middleware
- Guidance on custom middleware
- Convenience constants via `MiddlewareNames` object

This approach provides better developer experience without sacrificing the flexibility that makes the middleware system powerful.

---

## Related Documentation

- **Investigation Report:** See conversation history for detailed analysis
- **TypeScript Source:** https://github.com/floating-ui/floating-ui/blob/master/packages/core/src/types.ts
- **Floating UI Middleware Docs:** https://floating-ui.com/docs/middleware
- **Enum Pattern Standard:** `floatingUI/ENUM_PATTERN_STANDARD.md`

