# Comparison: Example vs Real Laminar/Airstream

This document shows how the example in `UsingClauseExample.scala` (Example 5) maps to the actual Laminar/Airstream code.

## Side-by-Side Comparison

### 1. Owner Definition

**Example (Simplified):**
```scala
trait Owner {
  def own(subscription: Subscription): Unit
  def killSubscriptions(): Unit
}
```

**Real Airstream:**
```scala
// airstream/src/io/github/nguyenyou/airstream/ownership/Owner.scala
trait Owner {
  protected val subscriptions: JsArray[Subscription] = JsArray()
  
  protected def killSubscriptions(): Unit = {
    subscriptions.forEach(_.onKilledByOwner())
    subscriptions.length = 0
  }
  
  private[ownership] def own(subscription: Subscription): Unit = {
    subscriptions.push(subscription)
    onOwned(subscription)
  }
}
```

**Similarity:** ✅ Almost identical! The real version just has more details.

---

### 2. Subscription Definition

**Example (Simplified):**
```scala
class Subscription(owner: Owner, cleanup: () => Unit) {
  owner.own(this)  // Register with owner immediately!
  
  def kill(): Unit = cleanup()
}
```

**Real Airstream:**
```scala
// airstream/src/io/github/nguyenyou/airstream/ownership/Subscription.scala
class Subscription(
  private[ownership] val owner: Owner,
  cleanup: () => Unit
) {
  final private var _isKilled = false
  
  owner.own(this)  // ← Same pattern!
  
  def kill(): Unit = {
    if (!_isKilled) {
      _isKilled = true
      cleanup()
      owner.onKilledExternally(this)
    }
  }
}
```

**Similarity:** ✅ The core pattern is identical! Real version adds safety checks.

---

### 3. Observable.addObserver

**Example (Simplified):**
```scala
class Observable[A] {
  def addObserver(onNext: A => Unit)(using owner: Owner): Subscription = {
    observers = observers :+ onNext
    new Subscription(owner, cleanup = () => {
      observers = observers.filterNot(_ == onNext)
    })
  }
}
```

**Real Airstream:**
```scala
// airstream/src/io/github/nguyenyou/airstream/core/WritableObservable.scala
override def addObserver(observer: Observer[A])(implicit owner: Owner): Subscription = {
  //                                             ↑ Note: uses `implicit` (Scala 2 style)
  Transaction.onStart.shared({
    maybeWillStart()
    val subscription = addExternalObserver(observer, owner)
    onAddedExternalObserver(observer)
    maybeStart()
    subscription
  }, when = !isStarted)
}

override protected def addExternalObserver(observer: Observer[A], owner: Owner): Subscription = {
  val subscription = new Subscription(owner, () => removeExternalObserver(observer))
  //                                  ↑ Same pattern!
  externalObservers.push(observer)
  subscription
}
```

**Similarity:** ✅ The core pattern is the same! Real version has transaction management and start/stop logic.

---

### 4. Element with given Owner

**Example (Simplified):**
```scala
class Element(val tagName: String) {
  // Each element has its own Owner
  given elementOwner: Owner = new Owner {
    private var subscriptions: List[Subscription] = List.empty
    
    def own(subscription: Subscription): Unit = {
      subscriptions = subscriptions :+ subscription
    }
    
    def killSubscriptions(): Unit = {
      subscriptions.foreach(_.kill())
      subscriptions = List.empty
    }
  }
  
  def bindObservable[A](observable: Observable[A], onNext: A => Unit): Unit = {
    observable.addObserver(onNext)  // ← Uses given elementOwner implicitly
  }
}
```

**Real Laminar:**
```scala
// laminar/src/io/github/nguyenyou/laminar/nodes/ParentNode.scala
trait ParentNode[+Ref <: dom.Element] extends ReactiveNode[Ref] {
  private[nodes] val dynamicOwner: DynamicOwner = new DynamicOwner(() => {
    val path = DomApi.debugPath(ref).mkString(" > ")
    throw new Exception(s"Attempting to use owner of unmounted element: $path")
  })
}

// laminar/src/io/github/nguyenyou/laminar/nodes/ReactiveElement.scala
@inline def bindSink[V](
  element: ReactiveElement.Base,
  observable: Observable[V]
)(
  sink: Sink[V]
): DynamicSubscription = {
  DynamicSubscription.subscribeSink(element.dynamicOwner, observable, sink)
  //                                 ↑ Extracts the element's owner
}
```

**Similarity:** ✅ Same concept! Real version uses `DynamicOwner` (can activate/deactivate) instead of simple `Owner`.

---

### 5. The `-->` Operator

**Example (Simplified):**
```scala
extension [A](observable: Observable[A]) {
  def -->(onNext: A => Unit): Binder = {
    Binder { element =>
      element.bindObservable(observable, onNext)
    }
  }
}
```

**Real Laminar:**
```scala
// laminar/src/io/github/nguyenyou/laminar/api/Implicits.scala
class RichSource[A](val source: Source[A]) extends AnyVal {
  def -->(sink: Sink[A]): Binder.Base = {
    Binder(ReactiveElement.bindSink(_, source.toObservable)(sink))
    //     ↑ Same pattern: returns a Binder that takes an element
  }
}
```

**Similarity:** ✅ Identical pattern! Real version uses `Source` (more general) and `Sink` (more flexible).

---

### 6. Binder Definition

**Example (Simplified):**
```scala
trait Binder {
  def apply(element: Element): Unit
}

object Binder {
  def apply(bindFn: Element => Unit): Binder = new Binder {
    def apply(element: Element): Unit = bindFn(element)
  }
}
```

**Real Laminar:**
```scala
// laminar/src/io/github/nguyenyou/laminar/modifiers/Binder.scala
trait Binder[-El <: ReactiveElement.Base] extends Modifier[El] {
  def bind(element: El): DynamicSubscription
  
  final override def apply(element: El): Unit = bind(element)
  //                                            ↑ Same pattern!
}
```

**Similarity:** ✅ Identical pattern! Real version returns `DynamicSubscription` for more control.

---

### 7. Element Constructor (div)

**Example (Simplified):**
```scala
def div(modifiers: Binder*): Element = {
  val element = new Element("div")
  element.apply(modifiers*)
}
```

**Real Laminar:**
```scala
// laminar/src/io/github/nguyenyou/laminar/tags/HtmlTag.scala
class HtmlTag[+Ref <: dom.html.Element](tagName: String, void: Boolean = false) {
  def apply(modifiers: Modifier[El]*): El = {
    val element = build()
    modifiers.foreach(_.apply(element))
    //         ↑ Same pattern!
    element
  }
}
```

**Similarity:** ✅ Identical pattern!

---

## The Complete Flow: Example vs Real

### **Example Flow:**

```
1. div(clickObservable --> Observer {...})
   ↓
2. new Element("div") with given elementOwner: Owner
   ↓
3. clickObservable --> Observer {...} creates Binder
   ↓
4. Binder applied to element
   ↓
5. element.bindObservable(observable, onNext)
   ↓
6. observable.addObserver(onNext)(using elementOwner)
   ↓
7. new Subscription(elementOwner, cleanup)
   ↓
8. elementOwner.own(subscription)
```

### **Real Laminar Flow:**

```
1. div(onClick --> Observer {...})
   ↓
2. new HtmlElement with dynamicOwner: DynamicOwner
   ↓
3. onClick --> Observer {...} creates Binder
   ↓
4. Binder applied to element
   ↓
5. ReactiveElement.bindSink(element, observable)(sink)
   ↓
6. DynamicSubscription.subscribeSink(element.dynamicOwner, observable, sink)
   ↓
7. DynamicSubscription.unsafe(dynamicOwner, owner => observable.addObserver(...)(using owner))
   ↓
8. When element mounts: dynamicOwner.activate() creates OneTimeOwner
   ↓
9. observable.addObserver(observer)(using oneTimeOwner)
   ↓
10. new Subscription(oneTimeOwner, cleanup)
    ↓
11. oneTimeOwner.own(subscription)
```

**Key Difference:** Real Laminar uses `DynamicOwner` → `OneTimeOwner` indirection for mount/unmount support.

---

## What's Different in Real Laminar?

### 1. **DynamicOwner vs Owner**

**Example:** Uses simple `Owner` directly

**Real Laminar:** Uses `DynamicOwner` which:
- Can be activated/deactivated repeatedly (mount/unmount)
- Creates a fresh `OneTimeOwner` on each activation
- Allows elements to be reused

### 2. **DynamicSubscription vs Subscription**

**Example:** Uses simple `Subscription`

**Real Laminar:** Uses `DynamicSubscription` which:
- Wraps the activation logic
- Can be activated/deactivated repeatedly
- Stores the current `Subscription` (if active)

### 3. **Transaction Management**

**Example:** No transactions

**Real Laminar:** Uses `Transaction.onStart.shared` to:
- Batch multiple updates
- Prevent glitches (temporary inconsistencies)
- Optimize performance

### 4. **Start/Stop Logic**

**Example:** No start/stop

**Real Laminar:** Observables start when they get their first observer, stop when they lose their last observer (lazy evaluation)

---

## What's the Same?

✅ **The core pattern of implicit owner passing**
✅ **Binder as a function from Element to Unit**
✅ **The `-->` operator creating a Binder**
✅ **Subscription registering with Owner in constructor**
✅ **Owner managing a list of subscriptions**
✅ **Automatic cleanup when owner is killed**

---

## Summary

The example in `UsingClauseExample.scala` captures **the essential pattern** of how Laminar works:

1. ✅ Element provides a `given owner`
2. ✅ Binder captures observable and observer
3. ✅ Binder accesses element's scope to get the owner
4. ✅ Owner is passed implicitly to `addObserver`
5. ✅ Subscription registers with owner automatically
6. ✅ Cleanup happens when owner is killed

The real Laminar adds:
- 🔄 Mount/unmount support (DynamicOwner)
- 🔄 Lazy observables (start/stop)
- 🔄 Transaction batching
- 🔄 More type safety and error handling

But the **fundamental pattern is identical**! 🎯

---

## Further Reading

- **Real Laminar Code:**
  - `laminar/src/io/github/nguyenyou/laminar/nodes/ParentNode.scala` - Element with DynamicOwner
  - `laminar/src/io/github/nguyenyou/laminar/api/Implicits.scala` - The `-->` operator
  - `laminar/src/io/github/nguyenyou/laminar/modifiers/Binder.scala` - Binder trait

- **Real Airstream Code:**
  - `airstream/src/io/github/nguyenyou/airstream/ownership/Owner.scala` - Owner trait
  - `airstream/src/io/github/nguyenyou/airstream/ownership/Subscription.scala` - Subscription class
  - `airstream/src/io/github/nguyenyou/airstream/ownership/DynamicOwner.scala` - DynamicOwner for mount/unmount
  - `airstream/src/io/github/nguyenyou/airstream/core/WritableObservable.scala` - addObserver method

- **Documentation:**
  - `examples/LAMINAR_PATTERN_EXPLAINED.md` - Detailed flow explanation
  - `examples/UsingClauseExample.scala` - Runnable examples

