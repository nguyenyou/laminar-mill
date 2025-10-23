# FloatingUI Facade Improvements

## Summary of Changes

The FloatingUI facade has been significantly improved to follow Scala.js best practices and enable proper usage from Scala code.

## Critical Fixes

### 1. Configuration Objects Made Creatable from Scala

**Problem**: All configuration/options traits were marked with `@js.native`, which meant they could only be **read** from JavaScript, not **created** from Scala code.

**Solution**: Removed `@js.native` from all configuration and options traits, making them non-native traits that can be instantiated from Scala.

**Affected Traits** (now non-native):
- `ComputePositionConfig`
- `OffsetOptions`
- `ArrowOptions`
- `ShiftOptions`
- `DetectOverflowOptions`
- `FlipOptions`
- `AutoPlacementOptions`
- `HideOptions`
- `SizeOptions`
- `InlineOptions`
- `AutoUpdateOptions`

**Kept as `@js.native`** (read-only data from JavaScript):
- `ComputePositionReturn`
- `MiddlewareData`
- `ArrowData`, `FlipData`, `HideData`, etc.
- `Coords`, `Rect`, `SideObject`, etc.
- `Middleware` (returned by middleware functions)
- `Platform` (provided by the library)

### 2. Default Values Updated

Changed default values from `= js.native` to `= js.undefined` in all non-native traits.

**Before**:
```scala
@js.native
trait OffsetOptions extends js.Object {
  val mainAxis: js.UndefOr[Double] = js.native  // ❌ Can't create
}
```

**After**:
```scala
trait OffsetOptions extends js.Object {
  val mainAxis: js.UndefOr[Double] = js.undefined  // ✅ Can create
}
```

### 3. Companion Objects with Apply Methods

Added companion objects with `apply` methods for all configuration traits to provide a clean, idiomatic Scala API.

**Example**:
```scala
object OffsetOptions {
  def apply(
    mainAxis: js.UndefOr[Double] = js.undefined,
    crossAxis: js.UndefOr[Double] = js.undefined,
    alignmentAxis: js.UndefOr[Double | Null] = js.undefined
  ): OffsetOptions = {
    val obj = js.Dynamic.literal()
    if (mainAxis.isDefined) obj.mainAxis = mainAxis.get
    if (crossAxis.isDefined) obj.crossAxis = crossAxis.get
    if (alignmentAxis.isDefined) obj.alignmentAxis = alignmentAxis.get.asInstanceOf[js.Any]
    obj.asInstanceOf[OffsetOptions]
  }
}
```

### 4. Handling Union Types

Union types (e.g., `Double | SideObject`, `Alignment | Null`) require special handling when creating objects with `js.Dynamic.literal`. We use `.asInstanceOf[js.Any]` to work around type system limitations.

## Usage Examples

### Before (Didn't Work)

```scala
// ❌ This would not compile
val config = new ComputePositionConfig {
  val placement = "top"
  val middleware = js.Array(offset(10))
}
```

### After (Works!)

#### Option 1: Using Companion Object (Recommended)

```scala
import www.facades.floatingui.FloatingUIDOM._

val config = ComputePositionConfig(
  placement = "top",
  middleware = js.Array(
    offset(10),
    flip(),
    shift(ShiftOptions(padding = 5))
  )
)

computePosition(referenceEl, floatingEl, config).foreach { result =>
  floatingEl.style.left = s"${result.x}px"
  floatingEl.style.top = s"${result.y}px"
}
```

#### Option 2: Using Trait with `new`

```scala
val config = new ComputePositionConfig {
  override val placement = "top"
  override val middleware = js.Array(offset(10))
}
```

#### Option 3: Using js.Dynamic.literal

```scala
val config = js.Dynamic.literal(
  placement = "top",
  middleware = js.Array(offset(10))
).asInstanceOf[ComputePositionConfig]
```

### Complete Example with Middleware

```scala
import www.facades.floatingui.FloatingUIDOM._

def setupTooltip(
  button: dom.HTMLElement,
  tooltip: dom.HTMLElement,
  arrow: dom.HTMLElement
): Unit = {
  
  def updatePosition(): Unit = {
    computePosition(
      button,
      tooltip,
      ComputePositionConfig(
        placement = "top",
        middleware = js.Array(
          offset(OffsetOptions(mainAxis = 10)),
          flip(FlipOptions(
            fallbackPlacements = js.Array("bottom", "left", "right")
          )),
          shift(ShiftOptions(padding = 8)),
          arrow(ArrowOptions(element = arrow, padding = 5))
        )
      )
    ).foreach { result =>
      // Position tooltip
      tooltip.style.left = s"${result.x}px"
      tooltip.style.top = s"${result.y}px"
      
      // Position arrow
      result.middlewareData.arrow.foreach { arrowData =>
        arrowData.x.foreach(x => arrow.style.left = s"${x}px")
        arrowData.y.foreach(y => arrow.style.top = s"${y}px")
      }
    }
  }
  
  // Auto-update on scroll/resize
  val cleanup = autoUpdate(
    button,
    tooltip,
    () => updatePosition(),
    AutoUpdateOptions(
      ancestorScroll = true,
      ancestorResize = true
    )
  )
  
  // Call cleanup() when done
}
```

## Benefits

1. **Type Safety**: Full type checking at compile time
2. **IDE Support**: Auto-completion and inline documentation
3. **Idiomatic Scala**: Clean, readable API that feels natural in Scala
4. **Flexibility**: Multiple ways to create configuration objects
5. **Correctness**: Follows official Scala.js best practices

## Migration Guide

If you have existing code using the old facade (which wouldn't have compiled), update it as follows:

### Old (Broken)
```scala
// This never worked
val config = ??? // No way to create config
```

### New (Working)
```scala
// Use companion object
val config = ComputePositionConfig(
  placement = "top",
  middleware = js.Array(offset(10))
)
```

## Reference

See `FloatingUIUsageExample.scala` for comprehensive usage examples covering:
- Basic usage
- Middleware configuration
- Arrow positioning
- Auto-update
- Size constraints
- Complex configurations
- Overflow detection

## Technical Details

### Why Non-Native for Config Objects?

In Scala.js:
- **`@js.native` traits**: Can only be **read** from JavaScript (for data coming FROM JavaScript)
- **Non-native traits**: Can be **created** in Scala (for data going TO JavaScript)

Configuration objects are passed TO JavaScript functions, so they must be non-native.

### Why Keep Some Traits Native?

Return types and data structures that come FROM JavaScript (like `ComputePositionReturn`, `MiddlewareData`) should remain `@js.native` because:
1. They're created by JavaScript, not Scala
2. We only need to read their values
3. Keeping them native ensures we don't accidentally try to create them

### Handling Union Types

JavaScript union types (e.g., `number | SideObject`) don't map cleanly to Scala's type system when using `js.Dynamic.literal`. We handle this by:
1. Using conditional assignment (`if (x.isDefined)`)
2. Casting to `js.Any` when necessary
3. Letting the JavaScript runtime handle the actual type

This is a pragmatic trade-off between type safety and usability.

