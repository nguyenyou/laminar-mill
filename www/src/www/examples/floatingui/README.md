# FloatingUI Examples for Scala.js/Laminar

This directory contains comprehensive examples demonstrating how to use the FloatingUI library in Scala.js with Laminar.

**✅ All examples compile successfully and follow Laminar best practices!**

## Overview

These examples are based on the official Floating UI documentation and tutorials, translated to idiomatic Scala.js/Laminar code. They demonstrate:

1. **Basic positioning** - Simple tooltips and popovers
2. **Middleware** - Advanced positioning features (flip, shift, arrow, offset)
3. **AutoUpdate** - Automatic repositioning on scroll/resize
4. **Real-world patterns** - Production-ready components

## Files

### 1. BasicTooltipExample.scala

Demonstrates fundamental FloatingUI usage:
- Simple tooltip with default placement
- Custom placements (top, bottom, left, right, and their variants)
- Basic offset middleware
- Promise handling in Scala.js

**Key concepts:**
```scala
import scala.scalajs.js.Thenable.Implicits.thenable2future
import www.facades.floatingui.FloatingUIDOM._

// Basic usage
computePosition(buttonEl, tooltipEl).foreach { result =>
  tooltipEl.style.left = s"${result.x}px"
  tooltipEl.style.top = s"${result.y}px"
}

// With configuration
computePosition(
  buttonEl,
  tooltipEl,
  ComputePositionConfig(
    placement = "top",
    middleware = js.Array(offset(10))
  )
).foreach { result =>
  // Apply positioning
}
```

### 2. MiddlewareExample.scala

Shows how to use middleware for advanced positioning:
- **flip()** - Automatically flips placement when there's no space
- **shift()** - Shifts the element to keep it in view
- **arrow()** - Positions an arrow pointing to the reference element
- **offset()** - Adds spacing between elements
- Combining multiple middleware

**Key concepts:**
```scala
// Using multiple middleware together
ComputePositionConfig(
  placement = "top",
  middleware = js.Array(
    offset(10),
    flip(FlipOptions(
      fallbackPlacements = js.Array("bottom", "left", "right")
    )),
    shift(ShiftOptions()),
    arrow(ArrowOptions(element = arrowEl, padding = 5))
  )
)

// Accessing middleware data
result.middlewareData.arrow.foreach { arrowData =>
  arrowData.x.foreach(x => arrowEl.style.left = s"${x}px")
  arrowData.y.foreach(y => arrowEl.style.top = s"${y}px")
}
```

### 3. AutoUpdateExample.scala

Demonstrates automatic position updates:
- Basic autoUpdate setup and cleanup
- Animation frame updates for smooth tracking
- Real-world dropdown menu example
- Proper lifecycle management with Laminar

**Key concepts:**
```scala
var cleanupFn: Option[js.Function0[Unit]] = None

def updatePosition(): Unit = {
  computePosition(buttonEl, tooltipEl, config).foreach { result =>
    // Apply positioning
  }
}

// Set up autoUpdate
cleanupFn = Some(
  autoUpdate(
    buttonEl,
    tooltipEl,
    () => updatePosition(),
    AutoUpdateOptions(
      ancestorScroll = true,
      ancestorResize = true,
      elementResize = true
    )
  )
)

// Clean up when done
cleanupFn.foreach(cleanup => cleanup())
```

## Important Patterns

### 1. Promise to Future Conversion

FloatingUI returns JavaScript Promises. Convert them to Scala Futures using `.toFuture`:

```scala
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

computePosition(ref, floating).toFuture.foreach { result =>
  // Use result
}
```

**Why `.toFuture` instead of `thenable2future` implicit?**
- More explicit and clear about the conversion
- No need to import implicit conversions
- Follows the principle of explicit over implicit

**Why `JSExecutionContext.queue` instead of `ExecutionContext.global`?**
- `JSExecutionContext.queue` is the Scala.js-specific ExecutionContext
- `ExecutionContext.global` triggers compiler warnings in Scala.js
- `JSExecutionContext.queue` is what the Airstream and Laminar test suites use
- It's optimized for JavaScript environments (uses Promise microtasks or setTimeout)

### 2. Creating Configuration Objects

Use the companion object `apply` methods we added:

```scala
// Recommended: Using companion object
val config = ComputePositionConfig(
  placement = "top",
  middleware = js.Array(offset(10))
)

// Alternative: Using new
val config = new ComputePositionConfig {
  override val placement = "top"
  override val middleware = js.Array(offset(10))
}

// Alternative: Using js.Dynamic.literal
val config = js.Dynamic.literal(
  placement = "top",
  middleware = js.Array(offset(10))
).asInstanceOf[ComputePositionConfig]
```

### 3. Middleware Options

All middleware accept optional configuration:

```scala
// Simple usage
offset(10)  // Just a number

// With options object
offset(OffsetOptions(
  mainAxis = 10,
  crossAxis = 5,
  alignmentAxis = 0
))

// Flip with fallback placements
flip(FlipOptions(
  fallbackPlacements = js.Array("bottom", "left", "right")
))

// Shift with padding
shift(ShiftOptions())  // Uses defaults

// Arrow with element and padding
arrow(ArrowOptions(
  element = arrowElement,
  padding = 5
))
```

### 4. Laminar Integration

Integrate with Laminar's reactive system:

```scala
val isVisible = Var(false)

div(
  // Show/hide based on state
  display <-- isVisible.signal.map(if (_) "block" else "none"),
  
  // Update position when visibility changes
  onMountCallback { ctx =>
    isVisible.signal.foreach { visible =>
      if (visible) {
        computePosition(ref, floating).foreach { result =>
          // Apply positioning
        }
      }
    }(ctx.owner)
  }
)
```

### 5. AutoUpdate Lifecycle Management

Always clean up autoUpdate listeners:

```scala
onMountCallback { ctx =>
  var cleanupFn: Option[js.Function0[Unit]] = None
  
  isVisible.signal.foreach { visible =>
    if (visible) {
      cleanupFn = Some(autoUpdate(ref, floating, updateFn))
    } else {
      cleanupFn.foreach(cleanup => cleanup())
      cleanupFn = None
    }
  }(ctx.owner)
  
  // Clean up on unmount
  ctx.onUnmountCallback { _ =>
    cleanupFn.foreach(cleanup => cleanup())
  }
}
```

## Running the Examples

To see these examples in action:

1. Add them to your App.scala:
```scala
import www.examples.floatingui._

div(
  BasicTooltipExample.demo(),
  MiddlewareExample.demo(),
  AutoUpdateExample.demo()
)
```

2. Compile and run:
```bash
./mill www.fullLinkJS
yarn dev
```

## Common Patterns

### Scala 3 `given Owner` Pattern

All examples use the idiomatic Scala 3 pattern for working with `Owner`:

```scala
onMountCallback { ctx =>
  given Owner = ctx.owner  // ✅ Scala 3 idiomatic way

  // Now owner is available as a given instance
  isVisible.signal.foreach { visible =>
    // ... owner is implicitly passed here
  }
}
```

**Why not `import ctx.owner`?**
- `import ctx.owner` is a Scala 2 idiom that still works but mixes old and new styles
- `given Owner = ctx.owner` is explicit and idiomatic in Scala 3
- It clearly shows that we're declaring a `given` instance for the scope

**Alternative (explicit passing):**
```scala
onMountCallback { ctx =>
  isVisible.signal.foreach { visible =>
    // ...
  }(using ctx.owner)  // Explicitly pass owner
}
```

### Tooltip Component

```scala
def tooltip(
  trigger: HtmlElement,
  content: String,
  placement: String = "top"
): HtmlElement = {
  val isVisible = Var(false)

  div(
    trigger.amend(
      onMouseEnter --> Observer(_ => isVisible.set(true)),
      onMouseLeave --> Observer(_ => isVisible.set(false))
    ),
    div(
      role := "tooltip",
      display <-- isVisible.signal.map(if (_) "block" else "none"),
      // ... styling ...
      onMountCallback { ctx =>
        given Owner = ctx.owner
        // ... positioning logic ...
      },
      content
    )
  )
}
```

### Dropdown Component

```scala
def dropdown(
  trigger: HtmlElement,
  items: List[String],
  onSelect: String => Unit
): HtmlElement = {
  val isOpen = Var(false)
  
  div(
    trigger.amend(onClick --> Observer(_ => isOpen.update(!_))),
    div(
      display <-- isOpen.signal.map(if (_) "block" else "none"),
      // ... menu styling ...
      onMountCallback { ctx =>
        // ... positioning with autoUpdate ...
      },
      items.map { item =>
        div(
          onClick --> Observer { _ =>
            onSelect(item)
            isOpen.set(false)
          },
          item
        )
      }
    )
  )
}
```

## References

- [Official FloatingUI Documentation](https://floating-ui.com/docs/getting-started)
- [FloatingUI Tutorial](https://floating-ui.com/docs/tutorial)
- [Middleware Guide](https://floating-ui.com/docs/middleware)
- [AutoUpdate Documentation](https://floating-ui.com/docs/autoUpdate)
- [Facade Implementation](../../facades/floatingUI.scala)
- [Facade Improvements](../../facades/FACADE_IMPROVEMENTS.md)

## Tips

1. **Always use thenable2future** - Import it at the top of your file
2. **Clean up autoUpdate** - Use Laminar's lifecycle callbacks
3. **Use companion objects** - They provide the cleanest API for creating configs
4. **Combine middleware** - Order matters! Usually: offset → flip → shift → arrow
5. **Check middleware data** - Use `result.middlewareData` to access additional info
6. **Handle edge cases** - Use flip and shift to handle viewport boundaries
7. **Performance** - Only set up autoUpdate when the floating element is visible

## Troubleshooting

**Problem:** "value foreach is not a member of js.Promise"
**Solution:** Import `scala.scalajs.js.Thenable.Implicits.thenable2future`

**Problem:** "Cannot create instance of @js.native trait"
**Solution:** Use companion object `apply` methods or `new` with non-native traits

**Problem:** Tooltip doesn't update position on scroll
**Solution:** Use `autoUpdate` and make sure to clean it up properly

**Problem:** Arrow not positioned correctly
**Solution:** Make sure to read `result.middlewareData.arrow` and apply both x/y and the static side

**Problem:** Union type errors when creating options
**Solution:** The companion objects handle this automatically - use them!

