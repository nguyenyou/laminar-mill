# Scala 3 `given Owner` Pattern in FloatingUI Examples

## Summary

All FloatingUI examples have been refactored to use the idiomatic Scala 3 pattern for working with `Owner` instances.

## The Change

### ❌ Before (Scala 2 style)

```scala
onMountCallback { ctx =>
  import ctx.owner  // Scala 2 implicit import
  
  isVisible.signal.foreach { visible =>
    // owner is available here as an implicit
  }
}
```

### ✅ After (Scala 3 style)

```scala
onMountCallback { ctx =>
  given Owner = ctx.owner  // Scala 3 given declaration
  
  isVisible.signal.foreach { visible =>
    // owner is available here as a given
  }
}
```

## Why This Matters

### 1. **Explicit and Clear**
The `given Owner = ctx.owner` syntax makes it immediately clear that:
- We're declaring a `given` instance
- The type is `Owner`
- The value comes from `ctx.owner`

### 2. **Idiomatic Scala 3**
Scala 3 introduced the `given`/`using` system to replace Scala 2's `implicit` system. While `import` still works for backward compatibility, it's mixing old and new idioms.

### 3. **Better IDE Support**
Modern IDEs can better understand and provide hints for `given` declarations compared to imported implicits.

### 4. **Follows Laminar Patterns**
Looking at the Laminar codebase itself, the pattern is to use `given` instances:

```scala
// From laminar/src/io/github/nguyenyou/laminar/nodes/ParentNode.scala
class Element(val tagName: String) {
  given elementOwner: Owner = new Owner {
    // ...
  }
}
```

## How Owner Works in Laminar

### The Flow

1. **Element provides Owner**: Each Laminar element has a `DynamicOwner` that manages subscriptions
2. **MountContext wraps it**: When an element is mounted, a `MountContext` is created with the current `Owner`
3. **Callbacks receive context**: `onMountCallback` receives the `MountContext` as a parameter
4. **We declare given**: We extract the owner and declare it as a `given` instance
5. **Subscriptions use it**: Methods like `foreach` on signals automatically use the `given Owner`

### Example Flow

```scala
// 1. Element has a DynamicOwner
val myDiv = div(
  // 2. onMountCallback receives MountContext
  onMountCallback { ctx =>
    // 3. We declare the owner as a given
    given Owner = ctx.owner
    
    // 4. Signal.foreach uses the given owner implicitly
    someSignal.foreach { value =>
      // This subscription is owned by ctx.owner
      // It will be cleaned up when the element unmounts
    }
  }
)
```

## Alternative Patterns

### Pattern 1: Explicit `using` (Most Explicit)

```scala
onMountCallback { ctx =>
  isVisible.signal.foreach { visible =>
    // ...
  }(using ctx.owner)  // Explicitly pass owner
}
```

**Pros:**
- Very explicit about where owner comes from
- No need to declare a given

**Cons:**
- Verbose
- Need to remember to add `(using ctx.owner)` to every call

### Pattern 2: `given` declaration (Recommended)

```scala
onMountCallback { ctx =>
  given Owner = ctx.owner
  
  isVisible.signal.foreach { visible =>
    // ...
  }
}
```

**Pros:**
- Idiomatic Scala 3
- Declare once, use everywhere in scope
- Clear and explicit

**Cons:**
- None! This is the recommended pattern.

### Pattern 3: `import` (Scala 2 compatibility)

```scala
onMountCallback { ctx =>
  import ctx.owner
  
  isVisible.signal.foreach { visible =>
    // ...
  }
}
```

**Pros:**
- Works (for backward compatibility)
- Shorter than explicit `using`

**Cons:**
- Mixes Scala 2 and Scala 3 idioms
- Less clear that we're working with a given instance
- Not idiomatic Scala 3

## Files Updated

All FloatingUI example files have been refactored:

1. ✅ `BasicTooltipExample.scala` - 3 occurrences updated
2. ✅ `MiddlewareExample.scala` - 4 occurrences updated
3. ✅ `AutoUpdateExample.scala` - 3 occurrences updated
4. ✅ `README.md` - Documentation updated with pattern explanation

## Verification

All examples compile successfully:

```bash
./mill www.compile
# [info] done compiling
```

## References

- **Scala 3 Documentation**: [Contextual Abstractions](https://docs.scala-lang.org/scala3/reference/contextual/givens.html)
- **Laminar Source**: `laminar/src/io/github/nguyenyou/laminar/lifecycle/MountContext.scala`
- **Airstream Source**: `airstream/src/io/github/nguyenyou/airstream/ownership/Owner.scala`
- **Example Code**: `examples/UsingClauseExample.scala` - Shows how the pattern works

## Key Takeaway

**Use `given Owner = ctx.owner` in Scala 3 Laminar code.** It's explicit, idiomatic, and makes the code easier to understand and maintain.

