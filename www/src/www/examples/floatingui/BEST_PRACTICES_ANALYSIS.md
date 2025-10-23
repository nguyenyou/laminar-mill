# Scala.js Best Practices Analysis for FloatingUI Examples

## Executive Summary

Based on analysis of the Scala.js, Airstream, and Laminar codebases in this project:

1. **ExecutionContext**: Use `scala.scalajs.concurrent.JSExecutionContext.Implicits.queue` ✅
2. **Promise handling**: Use `.toFuture` with Airstream's `EventStream.fromFuture()` or `Signal.fromFuture()` ✅
3. **Direct Future consumption**: Avoid `.foreach` and `.onComplete` - use Airstream instead ✅

---

## Question 1: `.toFuture.foreach` vs `.toFuture.onComplete`

### TL;DR: **Neither - Use Airstream Instead**

In Laminar/Airstream applications, you should **avoid directly consuming Futures** with `.foreach` or `.onComplete`. Instead, convert them to Airstream observables.

### The Airstream Way (Recommended)

```scala
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

// ✅ BEST: Convert to EventStream
val resultStream = EventStream.fromFuture(
  computePosition(buttonEl, tooltipEl).toFuture
)

resultStream.foreach { result =>
  tooltipEl.style.left = s"${result.x}px"
  tooltipEl.style.top = s"${result.y}px"
}
```

**Why this is better:**
- Integrates with Airstream's ownership system (automatic cleanup)
- Works with Laminar's reactive bindings
- Consistent with the rest of the codebase
- Proper error handling through Airstream's error propagation

### Evidence from Codebase

**Airstream's EventStream.fromFuture:**
```scala
// airstream/src/io/github/nguyenyou/airstream/core/EventStream.scala:399
def fromFuture[A](future: Future[A], emitOnce: Boolean = false)
  (implicit ec: ExecutionContext): EventStream[A] = {
  fromJsPromise(future.toJSPromise(using ec), emitOnce)
}
```

**Test examples:**
```scala
// airstream/test/src/com/raquo/airstream/timing/EventStreamFromFutureSpec.scala:29
def makeStream(promise: Promise[Int]): EventStream[Int] = 
  EventStream.fromFuture(promise.future)
    .map(Calculation.log("stream", calculations))
```

### If You Must Use Future Directly

If you're outside Airstream context (rare in Laminar apps):

**`.foreach` (simpler for success-only handling):**
```scala
promise.toFuture.foreach { result =>
  // Handle success only
  // Errors are silently ignored (logged to console)
}
```

**`.onComplete` (when you need error handling):**
```scala
promise.toFuture.onComplete {
  case Success(result) => // Handle success
  case Failure(error) => // Handle error
}
```

**Key differences:**
- `.foreach` only handles success cases
- `.onComplete` handles both success and failure
- Both require an ExecutionContext in scope

---

## Question 2: ExecutionContext - Which One to Use?

### TL;DR: Use `scala.scalajs.concurrent.JSExecutionContext.Implicits.queue`

### Recommendation

```scala
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
```

**NOT:**
```scala
import scala.concurrent.ExecutionContext.Implicits.global  // ❌ Avoid
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global  // ❌ Doesn't exist
```

### Evidence from This Codebase

**1. Airstream test suite uses JSExecutionContext:**
```scala
// airstream/test/src/com/raquo/airstream/AsyncUnitSpec.scala:11
override implicit def executionContext: ExecutionContext = 
  scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
```

**2. Laminar test suite uses JSExecutionContext:**
```scala
// laminar/test/src/com/raquo/laminar/tests/SyntaxSpec.scala:15
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
```

**3. Airstream syntax tests use JSExecutionContext:**
```scala
// airstream/test/src/com/raquo/airstream/syntax/SyntaxSpec.scala:6
import scalajs.concurrent.JSExecutionContext.Implicits.queue
```

### What About `ExecutionContext.global`?

In Scala.js, `ExecutionContext.global` **is actually an alias** for `JSExecutionContext.queue`:

```scala
// scala-js/scalalib/overrides/scala/concurrent/ExecutionContext.scala:154
implicit lazy val global: ExecutionContextExecutor =
  scala.scalajs.concurrent.JSExecutionContext.queue
```

So technically they're the same, but:
- ✅ **Use `JSExecutionContext.queue`** - explicit and clear
- ⚠️ **Avoid `ExecutionContext.global`** - triggers compiler warnings in Scala.js

### Compiler Warning

The Scala.js compiler warns about using `ExecutionContext.global`:

```scala
// scala-js/compiler/src/main/scala/org/scalajs/nscplugin/PrepJSInterop.scala:409
|If you do not care about macrotask fairness, you can silence this warning by:
|- Adding @nowarn("cat=other") (Scala >= 2.13.x only)
|- Setting the -P:scalajs:nowarnGlobalExecutionContext compiler option
|- Using scala.scalajs.concurrent.JSExecutionContext.queue
|  (the implementation of ExecutionContext.global in Scala.js) directly.
```

### What About MacrotaskExecutor?

`org.scalajs.macrotaskexecutor.MacrotaskExecutor` **does not exist** in this codebase or in standard Scala.js. 

A search of the entire codebase found **zero occurrences** of `MacrotaskExecutor`.

### How JSExecutionContext Works

```scala
// scala-js/library/src/main/scala/scala/scalajs/concurrent/QueueExecutionContext.scala:27
def apply(): ExecutionContextExecutor =
  if (js.typeOf(js.Dynamic.global.Promise) == "undefined") timeouts()
  else promises()
```

**JSExecutionContext.queue automatically chooses:**
- **Promises-based** (modern browsers) - uses JavaScript Promise microtask queue
- **setTimeout-based** (fallback) - uses setTimeout for older environments

This is optimal for browser environments.

---

## Recommended Pattern for FloatingUI Examples

### Current Pattern (Needs Improvement)

```scala
import scala.concurrent.ExecutionContext.Implicits.global  // ❌

computePosition(buttonEl, tooltipEl).toFuture.foreach { result =>
  tooltipEl.style.left = s"${result.x}px"
  tooltipEl.style.top = s"${result.y}px"
}
```

### Recommended Pattern (Airstream Integration)

```scala
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue  // ✅

val positionStream = EventStream.fromFuture(
  computePosition(buttonEl, tooltipEl).toFuture
)

positionStream.foreach { result =>
  tooltipEl.style.left = s"${result.x}px"
  tooltipEl.style.top = s"${result.y}px"
}
```

**Benefits:**
- Automatic cleanup when Owner is killed
- Consistent with Airstream patterns
- Better error handling
- Explicit ExecutionContext

### Alternative: If You Need Immediate Execution

If you truly need to execute immediately without Airstream (rare):

```scala
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

computePosition(buttonEl, tooltipEl).toFuture.foreach { result =>
  tooltipEl.style.left = s"${result.x}px"
  tooltipEl.style.top = s"${result.y}px"
}
```

This is acceptable but less idiomatic in Laminar applications.

---

## Summary of Required Changes

### Files to Update

All three FloatingUI example files need the ExecutionContext import changed:

1. `www/src/www/examples/floatingui/BasicTooltipExample.scala`
2. `www/src/www/examples/floatingui/MiddlewareExample.scala`
3. `www/src/www/examples/floatingui/AutoUpdateExample.scala`

### Change Required

```diff
- import scala.concurrent.ExecutionContext.Implicits.global
+ import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
```

### Optional Enhancement

Consider refactoring to use `EventStream.fromFuture()` for better Airstream integration, but this is not strictly necessary if the current pattern works for your use case.

---

## References

- **Scala.js ExecutionContext**: `scala-js/scalalib/overrides/scala/concurrent/ExecutionContext.scala`
- **JSExecutionContext**: `scala-js/library/src/main/scala/scala/scalajs/concurrent/JSExecutionContext.scala`
- **Airstream EventStream.fromFuture**: `airstream/src/io/github/nguyenyou/airstream/core/EventStream.scala:399`
- **Airstream test examples**: `airstream/test/src/com/raquo/airstream/AsyncUnitSpec.scala`
- **Laminar test examples**: `laminar/test/src/com/raquo/laminar/tests/SyntaxSpec.scala`

