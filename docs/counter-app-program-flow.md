# Deep Dive: Counter App Program Flow in Laminar

## Overview

This document traces the complete execution flow when rendering and clicking the increase/decrease buttons in [index.scala:6](../www/src/www/index.scala#L6).

## Initial Code

```scala
@main def main(): Unit = {
  val counterVar = Var(0)
  render(
    dom.document.getElementById("app"),
    div(
      button("-", onClick --> Observer { _ => counterVar.update(_ - 1) }),
      div(text <-- counterVar.signal),
      button("+", onClick --> Observer { _ => counterVar.update(_ + 1) })
    )
  )
}
```

---

## Phase 1: Initialization

### 1.1 Creating the Var

**Location:** [airstream/src/io/github/nguyenyou/airstream/state/Var.scala:272](../airstream/src/io/github/nguyenyou/airstream/state/Var.scala#L272)

```scala
val counterVar = Var(0)
```

**What happens:**
1. `Var.apply(0)` creates a `SourceVar[Int]` with initial value `Success(0)`
2. Inside `SourceVar`, a `VarSignal` is created to hold the reactive state
3. `VarSignal` is a `StrictSignal` - its current value is always up-to-date without needing subscriptions
4. The signal stores the initial value internally via `setCurrentValue(Success(0))`

**Key files:**
- [Var.scala:272](../airstream/src/io/github/nguyenyou/airstream/state/Var.scala#L272) - Factory method
- [VarSignal.scala:14](../airstream/src/io/github/nguyenyou/airstream/state/VarSignal.scala#L14) - Signal implementation

### 1.2 Building the UI Tree

**Location:** [laminar/src/io/github/nguyenyou/laminar/defs/tags/HtmlTags.scala](../laminar/src/io/github/nguyenyou/laminar/defs/tags/)

```scala
div(
  button("-", onClick --> Observer { _ => counterVar.update(_ - 1) }),
  div(text <-- counterVar.signal),
  button("+", onClick --> Observer { _ => counterVar.update(_ + 1) })
)
```

**What happens:**
1. `div` creates a `ReactiveHtmlElement[dom.html.Div]`
2. Each child element (`button`, inner `div`) is also created as `ReactiveHtmlElement`
3. **Modifiers** are applied:
   - `"-"` and `"+"` text becomes `TextNode` children
   - `onClick --> Observer { ... }` creates **event listeners** (not yet attached)
   - `text <-- counterVar.signal` creates a **dynamic inserter** (not yet subscribed)

**Key concept:** At this stage, we're just building a **declarative tree** of elements and modifiers. Nothing is "live" yet - no DOM elements, no subscriptions, no event listeners.

---

## Phase 2: Rendering - Mounting to the DOM

### 2.1 The render() Call

**Location:** [laminar/src/io/github/nguyenyou/laminar/api/Laminar.scala:90](../laminar/src/io/github/nguyenyou/laminar/api/Laminar.scala#L90)

```scala
render(dom.document.getElementById("app"), div(...))
```

**What happens:**
1. Creates a `RootNode` with the container and child element
2. `RootNode` constructor calls `mount()` immediately

**Location:** [laminar/src/io/github/nguyenyou/laminar/nodes/RootNode.scala:42](../laminar/src/io/github/nguyenyou/laminar/nodes/RootNode.scala#L42)

```scala
def mount(): Boolean = {
  dynamicOwner.activate()
  ParentNode.appendChild(parent = this, child, hooks = js.undefined)
}
```

### 2.2 Activation - The Ownership System

**What happens when `dynamicOwner.activate()` is called:**

1. **DynamicOwner** is activated on the root node
2. This recursively activates all child elements' `DynamicOwner`s
3. Each `ReactiveElement` has a `pilotSubscription` that:
   - Activates when the element is mounted
   - Deactivates when the element is unmounted
4. When activated, all **DynamicSubscriptions** owned by that element start

**Key concept:** This is Laminar's **automatic subscription management**. When an element is mounted to the DOM, all its reactive subscriptions become active. When unmounted, they're automatically cleaned up.

### 2.3 Attaching Event Listeners

**For:** `onClick --> Observer { _ => counterVar.update(_ - 1) }`

**Location:** [laminar/src/io/github/nguyenyou/laminar/api/Implicits.scala:140](../laminar/src/io/github/nguyenyou/laminar/api/Implicits.scala#L140)

**What happens:**

1. The `-->` operator is defined in `RichSource`:
   ```scala
   def -->(sink: Sink[A]): Binder.Base = {
     Binder(ReactiveElement.bindSink(_, source.toObservable)(sink))
   }
   ```

2. When the button element is mounted:
   - `EventListener` is created with:
     - `eventProp` = `onClick` (from [GlobalEventProps.scala:34](../laminar/src/io/github/nguyenyou/laminar/defs/eventProps/GlobalEventProps.scala#L34))
     - `callback` = the `Observer`'s `onNext` method
   - Native DOM event listener is attached: `button.addEventListener("click", callback)`
   - The listener is registered in the element's `maybeEventListeners` array

**Result:** Clicking the button will now call `Observer.onNext(mouseEvent)`

### 2.4 Text Binding Subscription

**For:** `text <-- counterVar.signal`

**Location:** [laminar/src/io/github/nguyenyou/laminar/receivers/ChildTextReceiver.scala:24](../laminar/src/io/github/nguyenyou/laminar/receivers/ChildTextReceiver.scala#L24)

**What happens:**

1. `text <-- counterVar.signal` creates a `ChildTextInserter` (dynamic inserter)
2. When the parent `div` is mounted, the inserter's `insertFn` is called
3. A subscription is created: `textSource.foreach { newValue => ... }`
4. **Initial render:**
   - Creates a `TextNode` with value `"0"`
   - Replaces the sentinel comment node with the new `TextNode`
   - Stores reference to the `TextNode` for future updates

**Location:** [laminar/src/io/github/nguyenyou/laminar/inserters/ChildTextInserter.scala:19](../laminar/src/io/github/nguyenyou/laminar/inserters/ChildTextInserter.scala#L19)

```scala
textSource.foreach { newValue =>
  maybeTextNode.fold {
    val newTextNode = new TextNode(renderable.asString(newValue))
    switchToText(newTextNode, ctx)
    maybeTextNode = newTextNode
  } { textNode =>
    textNode.ref.textContent = renderable.asString(newValue)
  }
}
```

**Result:** The counter displays "0" in the DOM

---

## Phase 3: Button Click - Update Flow

### 3.1 User Clicks the "+" Button

**What happens:**

1. Browser fires native `click` event
2. DOM event listener calls the Observer's callback
3. Observer executes: `counterVar.update(_ + 1)`

### 3.2 Var Update in a Transaction

**Location:** [airstream/src/io/github/nguyenyou/airstream/state/Var.scala:231](../airstream/src/io/github/nguyenyou/airstream/state/Var.scala#L231)

```scala
def update(mod: A => A): Unit = {
  Transaction { trx =>
    tryNow() match {
      case Success(currentValue) =>
        val nextValue = Try(mod(currentValue))
        setCurrentValue(nextValue, trx)
      case Failure(err) => ...
    }
  }
}
```

**What happens:**

1. **Transaction is created** ([Transaction.scala:102](../airstream/src/io/github/nguyenyou/airstream/core/Transaction.scala#L102))
   - Airstream uses transactions to prevent **FRP glitches**
   - All changes within a transaction are atomic
   - Observables can only emit once per transaction

2. **Current value is read:** `tryNow()` returns `Success(0)`

3. **Modifier is applied:** `mod(0)` returns `1`

4. **New value is set:** `setCurrentValue(Success(1), trx)`

### 3.3 Propagating the Change

**Location:** [airstream/src/io/github/nguyenyou/airstream/state/Var.scala:25](../airstream/src/io/github/nguyenyou/airstream/state/Var.scala#L25)

```scala
private[state] def setCurrentValue(value: Try[A], transaction: Transaction): Unit
```

**Flow:**

1. `SourceVar.setCurrentValue()` is called
2. It updates the internal `_currentValue`
3. It calls `signal.onTry(value, transaction)`

**Location:** [VarSignal.scala:28](../airstream/src/io/github/nguyenyou/airstream/state/VarSignal.scala#L28)

```scala
private[state] def onTry(nextValue: Try[A], transaction: Transaction): Unit = {
  fireTry(nextValue, transaction)
}
```

4. `fireTry()` propagates the new value to all **observers**

### 3.4 Signal Propagation

**What happens when `fireTry()` is called:**

1. The signal's current value is updated to `1`
2. **All active observers** of the signal are notified
3. In our case, there's one observer: the `ChildTextInserter`'s subscription

### 3.5 DOM Update

**Location:** [ChildTextInserter.scala:29](../laminar/src/io/github/nguyenyou/laminar/inserters/ChildTextInserter.scala#L29)

```scala
textNode.ref.textContent = renderable.asString(newValue)
```

**What happens:**

1. The inserter's `foreach` callback is invoked with `newValue = 1`
2. Since `maybeTextNode` already exists (from initial render), it takes the second branch
3. Updates the existing `TextNode`'s `textContent` to `"1"`
4. Browser automatically re-renders the changed text

**Result:** The counter now displays "1" in the DOM

### 3.6 Transaction Completion

**Location:** [Transaction.scala:394](../airstream/src/io/github/nguyenyou/airstream/core/Transaction.scala#L394)

```scala
private def run(transaction: Transaction): Unit = {
  try {
    transaction.code(transaction)
    transaction.resolvePendingObservables()
  } catch { ... }
}
```

**What happens:**

1. All pending observables in the transaction are resolved
2. Transaction is marked as complete
3. Memory is cleaned up (`transaction.code` is set to throw error if reused)

---

## Key Architectural Concepts

### 1. Transactions - Glitch Prevention

**Purpose:** Ensure consistency in reactive systems

**Example scenario without transactions:**
```scala
val a = Var(1)
val b = a.signal.map(_ * 2)
val c = a.signal.combineWith(b.signal)(_ + _)
```

If `a` updates to `2`:
- Without transactions: `c` might temporarily see inconsistent state (old `a` + new `b`)
- With transactions: All updates happen atomically, `c` sees consistent state

**Implementation:**
- [Transaction.scala:12](../airstream/src/io/github/nguyenyou/airstream/core/Transaction.scala#L12)
- Uses topological ordering via `topoRank` to fire observables in correct order
- Prevents multiple emissions from same observable in one transaction

### 2. Ownership System

**Purpose:** Automatic subscription lifecycle management

**Key components:**
- `Owner` - manages subscriptions' lifetime
- `DynamicOwner` - can be activated/deactivated
- `DynamicSubscription` - subscription tied to an owner
- `pilotSubscription` - special subscription that activates element's owner when mounted

**Flow:**
1. Element created → has inactive `DynamicOwner`
2. Element mounted → `pilotSubscription` activates owner
3. Owner activated → all `DynamicSubscription`s start
4. Element unmounted → owner deactivated → subscriptions stop
5. No memory leaks! 🎉

### 3. Signals vs EventStreams

**Signal:**
- Has current value (can call `.now()`)
- Always emits to new subscribers
- Example: `counterVar.signal`

**EventStream:**
- No current value
- Only emits new events
- Example: `onClick` events

### 4. Observers vs Binders

**Observer** ([Observer.scala:9](../airstream/src/io/github/nguyenyou/airstream/core/Observer.scala#L9)):
- Consumes values from observables
- Has `onNext(value)` method
- Example: `Observer { _ => counterVar.update(_ + 1) }`

**Binder** ([Implicits.scala:140](../laminar/src/io/github/nguyenyou/laminar/api/Implicits.scala#L140)):
- Connects an observable to an observer on an element
- Created by `-->` operator
- Manages subscription lifecycle via element's ownership

---

## Complete Flow Diagram

```
[User clicks "+"]
       ↓
[Browser fires DOM click event]
       ↓
[EventListener callback invoked]
       ↓
[Observer.onNext(mouseEvent) called]
       ↓
[counterVar.update(_ + 1) executes]
       ↓
[Transaction created] ──────────────┐
       ↓                            │
[Get current value: 0]              │
       ↓                            │ Atomic!
[Apply modifier: 0 + 1 = 1]        │
       ↓                            │
[setCurrentValue(Success(1), trx)] │
       ↓                            │
[VarSignal.onTry(Success(1), trx)] │
       ↓                            │
[Signal.fireTry(Success(1), trx)]  │
       ↓                            │
[Notify all observers] ─────────────┘
       ↓
[ChildTextInserter observer triggered]
       ↓
[textNode.ref.textContent = "1"]
       ↓
[Browser re-renders text node]
       ↓
[User sees "1" in UI]
```

---

## Performance Optimizations

1. **Lazy evaluation:** `VarSignal` maintains current value without subscriptions
2. **No redundant updates:** Inserter doesn't check if text changed (setting is faster than reading from DOM)
3. **Topological ordering:** Signals fire in dependency order to minimize recalculations
4. **Memory efficiency:** Completed transactions clean up immediately
5. **Single TextNode reuse:** Text updates modify existing node, no DOM thrashing

---

## Summary

The counter app demonstrates Laminar's core principles:

1. **Declarative UI:** Build element tree with modifiers
2. **Automatic reactivity:** Changes propagate automatically via signals
3. **Automatic lifecycle:** Subscriptions managed by ownership system
4. **Glitch-free:** Transactions ensure consistency
5. **Efficient updates:** Direct DOM manipulation, minimal overhead

When you click "+":
- Observer updates Var in transaction
- Signal propagates to inserter
- TextNode updates in place
- Total time: ~microseconds
- Memory allocations: minimal (transaction object, updated value)