# Understanding the Laminar Pattern: `div(onClick --> Observer { ... })`

This document explains how the implicit/given Owner flows through the Laminar pattern.

## The Pattern

```scala
val myDiv = div(
  onClick --> Observer { event =>
    println(s"Clicked: $event")
  }
)
```

## Step-by-Step Flow

### 1. **Element Creation: `div(...)`**

```scala
def div(modifiers: Binder*): Element = {
  val element = new Element("div")  // ← Element is created
  element.apply(modifiers*)         // ← Modifiers are applied
}
```

**What happens:**
- A new `Element` is created
- The element has a `given elementOwner: Owner` defined inside it
- This owner is the **source** of the implicit owner for all subscriptions

```scala
class Element(val tagName: String) {
  // THIS IS THE SOURCE OF THE IMPLICIT OWNER!
  given elementOwner: Owner = new Owner {
    private var subscriptions: List[Subscription] = List.empty
    
    def own(subscription: Subscription): Unit = {
      subscriptions = subscriptions :+ subscription
    }
    
    def killSubscriptions(): Unit = {
      subscriptions.foreach(_.kill())
    }
  }
  
  // ... rest of element
}
```

---

### 2. **The `-->` Operator Creates a Binder**

```scala
extension [A](observable: Observable[A]) {
  def -->(onNext: A => Unit): Binder = {
    // Returns a Binder that will be applied to an element later
    Binder { element =>
      element.bindObservable(observable, onNext)
    }
  }
}
```

**What happens:**
- `onClick --> Observer { ... }` creates a `Binder`
- The Binder is just a **function** that takes an `Element` and returns `Unit`
- It **captures** the observable and the observer function in a closure
- **Nothing is executed yet** - it's just a recipe!

---

### 3. **Binder is Applied to Element**

```scala
class Element {
  def apply(modifiers: Binder*): Element = {
    modifiers.foreach(_(this))  // ← Calls binder.apply(this)
    this
  }
}
```

**What happens:**
- `div(binder)` calls `element.apply(binder)`
- This calls `binder.apply(element)`
- The binder function executes: `element.bindObservable(observable, onNext)`

---

### 4. **Element Binds the Observable**

```scala
class Element {
  given elementOwner: Owner = ...  // ← This is in scope!
  
  def bindObservable[A](observable: Observable[A], onNext: A => Unit): Unit = {
    // The `using elementOwner` is passed implicitly!
    observable.addObserver(onNext)
    //         ↑ This method needs a `using owner: Owner` parameter
  }
}
```

**What happens:**
- `bindObservable` is called inside the element's scope
- The `given elementOwner` is available in this scope
- When `observable.addObserver(onNext)` is called, the compiler automatically passes `elementOwner`

---

### 5. **Observable Creates Subscription with Owner**

```scala
class Observable[A] {
  def addObserver(onNext: A => Unit)(using owner: Owner): Subscription = {
    //                                 ↑ Owner parameter received!
    observers = observers :+ onNext
    new Subscription(owner, cleanup = () => {
      observers = observers.filterNot(_ == onNext)
    })
  }
}
```

**What happens:**
- `addObserver` receives the `owner` parameter (the element's `elementOwner`)
- A new `Subscription` is created with this owner
- The subscription registers itself with the owner

---

### 6. **Subscription Registers with Owner**

```scala
class Subscription(owner: Owner, cleanup: () => Unit) {
  owner.own(this)  // ← Registers immediately in constructor!
  
  def kill(): Unit = cleanup()
}
```

**What happens:**
- The subscription calls `owner.own(this)` in its constructor
- The owner adds the subscription to its list
- Now the owner can kill this subscription later

---

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│  COMPLETE FLOW: div(onClick --> Observer { ... })               │
└─────────────────────────────────────────────────────────────────┘

TIME 1: Element Creation
┌──────────────────────────┐
│ div(...)                 │
│   ↓                      │
│ new Element("div")       │
│   ↓                      │
│ Element {                │
│   given elementOwner     │◄─── SOURCE OF IMPLICIT OWNER
│ }                        │
└──────────────────────────┘


TIME 2: Binder Creation (onClick --> Observer {...})
┌──────────────────────────────────────────┐
│ onClick --> Observer { event => ... }    │
│   ↓                                      │
│ Binder { element =>                      │
│   element.bindObservable(observable,     │
│                          onNext)         │
│ }                                        │
└──────────────────────────────────────────┘
         │
         │ Binder is just a function, not executed yet!
         ▼


TIME 3: Binder Applied to Element
┌──────────────────────────────────────────┐
│ element.apply(binder)                    │
│   ↓                                      │
│ binder.apply(element)                    │
│   ↓                                      │
│ element.bindObservable(observable,       │
│                        onNext)           │
└──────────────────────────────────────────┘
         │
         │ Now we're inside the element's scope!
         ▼


TIME 4: Inside Element's Scope
┌──────────────────────────────────────────┐
│ class Element {                          │
│   given elementOwner: Owner = ...        │◄─── GIVEN IS IN SCOPE
│                                          │
│   def bindObservable(...): Unit = {      │
│     observable.addObserver(onNext)       │
│     //         ↑                         │
│     //         Compiler inserts:         │
│     //         (using elementOwner)      │
│   }                                      │
│ }                                        │
└──────────────────────────────────────────┘
         │
         │ Compiler finds `given elementOwner`
         ▼


TIME 5: Observable Receives Owner
┌──────────────────────────────────────────┐
│ def addObserver(onNext: A => Unit)       │
│                (using owner: Owner)      │◄─── OWNER RECEIVED!
│   : Subscription = {                     │
│   new Subscription(owner, cleanup)       │
│ }                                        │
└──────────────────────────────────────────┘
         │
         │ Owner is the element's elementOwner
         ▼


TIME 6: Subscription Registers
┌──────────────────────────────────────────┐
│ class Subscription(owner: Owner, ...) {  │
│   owner.own(this)  // Register!          │
│ }                                        │
│   ↓                                      │
│ Owner.own(subscription) {                │
│   subscriptions += subscription          │
│ }                                        │
└──────────────────────────────────────────┘


FINAL STATE:
┌────────────────────────────────────────────┐
│ Element                                    │
│   elementOwner: Owner                      │
│     subscriptions: [Subscription]          │
│                           │                │
│                           ▼                │
│                     Subscription           │
│                       owner: elementOwner  │
│                       cleanup: () => ...   │
└────────────────────────────────────────────┘
```

---

## Key Insights

### 1. **The `given` is defined in the Element**

```scala
class Element {
  given elementOwner: Owner = new Owner { ... }
  //    ↑ This is the source!
}
```

### 2. **The Binder accesses the Element's scope**

```scala
Binder { element =>
  element.bindObservable(...)  // ← Called inside element's scope
}
```

### 3. **The `given` is in scope when calling `addObserver`**

```scala
def bindObservable(...): Unit = {
  observable.addObserver(onNext)  // ← elementOwner is in scope here!
}
```

### 4. **The compiler automatically passes the `given`**

```scala
// What you write:
observable.addObserver(onNext)

// What the compiler does:
observable.addObserver(onNext)(using elementOwner)
```

---

## Why This Pattern is Brilliant

1. **Automatic cleanup**: When the element is unmounted, all subscriptions are killed
2. **No manual wiring**: You never have to pass the owner explicitly
3. **Type-safe**: The compiler ensures an owner is always available
4. **Scoped**: Each element has its own owner, preventing cross-contamination
5. **Lazy**: Subscriptions are only created when the element is mounted

---

## Running the Example

```bash
scala-cli examples/UsingClauseExample.scala -M example5
```

You'll see output like:

```
[Element] Created: <div>
[-->] Creating Binder for observable
[Element] Applying 1 modifier(s) to <div>
[Binder] Applied to element: 123456
[Element] bindObservable called
[Observable] addObserver called with owner: 789012
[Subscription] Created with owner: 789012
[Owner] Element <div> owns subscription

--- Simulating click events ---
[Observable] Emitting: Click 1
[Observer] Click event received: Click 1

--- Unmounting element (cleanup) ---
[Owner] Killing all subscriptions for <div>
[Subscription] Killed, running cleanup
[Observable] Observer removed
```

---

## Comparison with Manual Approach

### **Without implicit owner (manual):**

```scala
val owner = new Owner {}
val element = new Element("div")
val subscription = observable.addObserver(onNext)(using owner)
owner.own(subscription)

// Later: manual cleanup
subscription.kill()
```

**Problems:**
- ❌ Must manually create owner
- ❌ Must manually pass owner
- ❌ Must manually track subscriptions
- ❌ Easy to forget cleanup

### **With implicit owner (Laminar pattern):**

```scala
val element = div(
  onClick --> Observer { event => println(event) }
)

// Later: automatic cleanup
element.unmount()
```

**Benefits:**
- ✅ Owner created automatically
- ✅ Owner passed automatically
- ✅ Subscriptions tracked automatically
- ✅ Cleanup happens automatically

---

## Summary

The Laminar pattern uses Scala 3's `given`/`using` feature to:

1. **Define** a `given owner` in each element
2. **Capture** observables and observers in Binders
3. **Apply** Binders to elements, entering the element's scope
4. **Pass** the owner implicitly to `addObserver`
5. **Register** subscriptions with the owner automatically
6. **Cleanup** all subscriptions when the element is unmounted

This creates a clean, type-safe, and automatic subscription management system! 🎯

