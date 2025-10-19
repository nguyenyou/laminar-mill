# Event Handling in Laminar - Executive Summary

This document provides a high-level overview of how event handling works in Laminar for:

```scala
div(onClick --> Observer { _ => println("clicked") })
```

For the complete, detailed flow with all source file references, see [COMPLETE_EVENT_HANDLING_FLOW.md](COMPLETE_EVENT_HANDLING_FLOW.md).

---

## The Big Picture

Laminar's event handling system has **6 phases**:

1. **Definition** - Creating the event property and operator
2. **Element Creation** - Building the DOM element with its owner
3. **Binder Application** - Attaching event handlers (but not activating them)
4. **Mounting** - Activating the owner and registering DOM listeners
5. **Event Handling** - Processing user interactions
6. **Cleanup** - Removing listeners and freeing resources

---

## Phase 1: Definition

```scala
onClick --> Observer { _ => println("clicked") }
```

**What happens:**

1. `onClick` is an `EventProp[dom.MouseEvent]` with name "click"
2. Implicitly converted to `EventProcessor[dom.MouseEvent, dom.MouseEvent]`
3. The `-->` operator creates an `EventListener` (which is a `Binder`)
4. The EventListener stores:
   - The event processor (for filtering/transforming events)
   - The callback function (`_ => println("clicked")`)
   - A `domCallback` (JavaScript function for the browser)

**Key files:**
- `laminar/defs/eventProps/GlobalEventProps.scala` - onClick definition
- `laminar/keys/EventProp.scala` - EventProp class
- `laminar/keys/EventProcessor.scala` - EventProcessor and --> operator
- `laminar/modifiers/EventListener.scala` - EventListener class

---

## Phase 2: Element Creation

```scala
div(onClick --> Observer { _ => println("clicked") })
```

**What happens:**

1. `div(...)` creates a `ReactiveHtmlElement`
2. The element has a `DynamicOwner` (not activated yet)
3. The element has a `pilotSubscription` that manages its lifecycle
4. The actual DOM element is created with `document.createElement("div")`

**Key insight:** Every element has its own `DynamicOwner` that will manage all subscriptions on that element.

**Key files:**
- `laminar/tags/HtmlTag.scala` - div() constructor
- `laminar/nodes/ReactiveElement.scala` - ReactiveElement with pilotSubscription
- `laminar/nodes/ParentNode.scala` - DynamicOwner creation

---

## Phase 3: Binder Application

**What happens:**

1. The EventListener (Binder) is applied to the element
2. A `subscribe` function is created (but NOT called yet):
   ```scala
   (ctx: MountContext) => {
     DomApi.addEventListener(element.ref, this)
     new Subscription(ctx.owner, cleanup = ...)
   }
   ```
3. A `DynamicSubscription` is created and registered with the element's `DynamicOwner`
4. The EventListener is added to the element's internal list

**Key insight:** The DOM listener is NOT registered yet! It's wrapped in a function that will be called on mount.

**Key files:**
- `laminar/modifiers/EventListener.scala` - bind() method
- `laminar/nodes/ReactiveElement.scala` - bindSubscriptionUnsafe()
- `airstream/ownership/DynamicSubscription.scala` - DynamicSubscription class

---

## Phase 4: Mounting

```scala
render(dom.document.getElementById("app"), app)
```

**What happens:**

1. A `RootNode` is created and `mount()` is called
2. `dynamicOwner.activate()` is called
3. A fresh `OneTimeOwner` is created
4. All `DynamicSubscription`s are activated:
   - `onActivate(oneTimeOwner)` is called
   - The `subscribe` function is finally called
   - A `MountContext` is created with the `OneTimeOwner` as an **implicit parameter**
5. The subscribe function:
   - Calls `DomApi.addEventListener` to register the DOM listener
   - Creates a `Subscription` with the owner from the context
6. The Subscription constructor calls `owner.own(this)` to register itself

**Key insight:** This is where the implicit owner flows:
```
OneTimeOwner → MountContext (implicit) → Subscription constructor
```

**Key files:**
- `laminar/nodes/RootNode.scala` - mount() method
- `airstream/ownership/DynamicOwner.scala` - activate() method
- `airstream/ownership/DynamicSubscription.scala` - onActivate() method
- `laminar/lifecycle/MountContext.scala` - MountContext with implicit owner
- `laminar/DomApi.scala` - addEventListener() method
- `airstream/ownership/Subscription.scala` - Subscription constructor

---

## Phase 5: Event Handling

**What happens when the user clicks:**

1. Browser fires a native `click` event
2. Browser calls the registered `domCallback` function
3. The domCallback:
   ```scala
   ev => {
     val processor = EventProcessor.processor(eventProcessor)
     processor(ev).foreach(callback)
   }
   ```
4. The processor transforms/filters the event (in our case, just wraps it in `Some`)
5. If the processor returns `Some(event)`, the callback is called
6. Our callback executes: `println("clicked")`

**Key insight:** The processor allows chaining like `onClick.preventDefault.filter(...) --> observer`

**Key files:**
- `laminar/modifiers/EventListener.scala` - domCallback definition
- `laminar/keys/EventProcessor.scala` - processor function

---

## Phase 6: Cleanup (Unmount)

**What happens when the element is removed:**

1. The element's `pilotSubscription` detects unmounting
2. `dynamicOwner.deactivate()` is called
3. All `DynamicSubscription`s are deactivated:
   - `onDeactivate()` is called
   - The `Subscription` is killed
4. The Subscription's cleanup function:
   - Removes the EventListener from the element's internal list
   - Calls `DomApi.removeEventListener` to unregister the DOM listener
5. The `OneTimeOwner` is killed and all its subscriptions are freed

**Key insight:** Automatic cleanup prevents memory leaks!

**Key files:**
- `laminar/nodes/ReactiveElement.scala` - willSetParent() method
- `airstream/ownership/DynamicOwner.scala` - deactivate() method
- `airstream/ownership/DynamicSubscription.scala` - onDeactivate() method
- `airstream/ownership/Subscription.scala` - kill() method
- `laminar/DomApi.scala` - removeEventListener() method

---

## The Ownership Chain

```
ReactiveElement
  └─ DynamicOwner (created on element creation)
      └─ DynamicSubscription (created on binder application)
          └─ OneTimeOwner (created on mount)
              └─ Subscription (created on activation)
```

**Lifecycle:**
- **Element created** → DynamicOwner created (inactive)
- **Binder applied** → DynamicSubscription created (inactive)
- **Element mounted** → DynamicOwner activated → OneTimeOwner created → Subscription created
- **Element unmounted** → DynamicOwner deactivated → Subscription killed → OneTimeOwner killed

---

## The Implicit Owner Flow

The implicit owner flows through the system like this:

```scala
// 1. DynamicOwner.activate() creates OneTimeOwner
val newOwner = new OneTimeOwner(...)

// 2. DynamicSubscription.onActivate(newOwner) is called
sub.onActivate(newOwner)

// 3. Inside onActivate, the activate function is called
maybeCurrentSubscription = activate(newOwner)

// 4. The activate function creates MountContext with implicit owner
owner => subscribe(new MountContext[El](element, owner))
//                                              ↑
//                                    implicit val owner: Owner

// 5. The subscribe function receives MountContext
(ctx: MountContext) => {
  // 6. Subscription is created with ctx.owner
  new Subscription(ctx.owner, cleanup = ...)
  //               ↑
  //         Extracts the implicit owner from context
}

// 7. Subscription constructor calls owner.own(this)
owner.own(this)
```

**Key insight:** The `MountContext` is the bridge that carries the implicit owner from the `DynamicOwner` to the `Subscription`.

---

## Key Design Decisions

### 1. Lazy Registration
DOM listeners are NOT registered until the element is mounted. This allows:
- Creating elements without side effects
- Reusing element definitions
- Mounting/unmounting elements multiple times

### 2. Ownership System
Every element has a `DynamicOwner` that can be activated/deactivated multiple times:
- **Activated** → Creates a fresh `OneTimeOwner` → Subscriptions are created
- **Deactivated** → Kills the `OneTimeOwner` → Subscriptions are cleaned up

### 3. Strong References with Explicit Lifecycle
Laminar uses strong references everywhere, but manages lifecycle explicitly:
- No weak references
- No garbage collection magic
- Explicit mount/unmount triggers cleanup
- Prevents memory leaks through disciplined resource management

### 4. Event Processing Pipeline
Events flow through a processor before reaching the callback:
- Allows filtering: `onClick.filter(_.ctrlKey) --> observer`
- Allows transformation: `onClick.map(_.clientX) --> observer`
- Allows side effects: `onClick.preventDefault --> observer`
- Composable and type-safe

### 5. Implicit Owner Pattern
The owner is passed implicitly through the system:
- No manual owner management in user code
- Type-safe (can't forget to pass owner)
- Scoped to the element (each element has its own owner)
- Automatic cleanup when element is unmounted

---

## Common Patterns

### Basic Event Handling
```scala
div(onClick --> Observer { ev => println(s"Clicked at ${ev.clientX}, ${ev.clientY}") })
```

### Event Filtering
```scala
div(onClick.filter(_.ctrlKey) --> Observer { _ => println("Ctrl+Click") })
```

### Event Transformation
```scala
div(onClick.map(_.clientX) --> Observer { x => println(s"X: $x") })
```

### preventDefault
```scala
a(href := "#", onClick.preventDefault --> Observer { _ => println("Link clicked but not followed") })
```

### Multiple Handlers
```scala
div(
  onClick --> Observer { _ => println("Handler 1") },
  onClick --> Observer { _ => println("Handler 2") }
)
```

### Event Streams
```scala
val clickStream: EventStream[dom.MouseEvent] = div.events(onClick)
clickStream.foreach(ev => println("Clicked"))(owner)
```

---

## Comparison with Other Frameworks

### React
```javascript
// React
<div onClick={(e) => console.log("clicked")}>
```
- Event listener registered immediately
- Manual cleanup in useEffect
- No ownership system

### Laminar
```scala
// Laminar
div(onClick --> Observer { _ => println("clicked") })
```
- Event listener registered on mount
- Automatic cleanup on unmount
- Ownership system manages lifecycle

### Vue
```javascript
// Vue
<div @click="handleClick">
```
- Event listener registered on mount
- Automatic cleanup on unmount
- Framework manages lifecycle

**Laminar's advantage:** Explicit ownership system allows fine-grained control over subscription lifecycle.

---

## Debugging Tips

### Check if element is mounted
```scala
element.dynamicOwner.isActive  // true if mounted
```

### Check event listeners
```scala
element.eventListeners  // List of all EventListeners on this element
```

### Check subscriptions
```scala
element.dynamicOwner.numSubscriptions  // Number of active subscriptions
```

### Common Issues

**Issue:** Event handler not firing
- **Check:** Is the element mounted?
- **Check:** Is the event name correct? (e.g., "click" not "onClick")
- **Check:** Is the event bubbling? (use capture mode if needed)

**Issue:** Memory leak
- **Check:** Is the element being unmounted properly?
- **Check:** Are you creating subscriptions outside the element's owner?
- **Check:** Are you manually killing subscriptions? (usually not needed)

**Issue:** Event handler called multiple times
- **Check:** Are you applying the same EventListener multiple times?
- **Check:** Are you creating multiple elements with the same listener?

---

## Further Reading

- [COMPLETE_EVENT_HANDLING_FLOW.md](COMPLETE_EVENT_HANDLING_FLOW.md) - Complete step-by-step flow with source references
- [LAMINAR_PATTERN_EXPLAINED.md](../examples/LAMINAR_PATTERN_EXPLAINED.md) - Simplified example explaining the pattern
- [COMPARISON_WITH_REAL_LAMINAR.md](../examples/COMPARISON_WITH_REAL_LAMINAR.md) - Comparison of example vs real code
- [counter-app-program-flow.md](counter-app-program-flow.md) - Complete flow for the counter app
- [rebuilding-laminar-from-scratch.md](rebuilding-laminar-from-scratch.md) - Guide to rebuilding Laminar

---

## Conclusion

Laminar's event handling system is built on three core principles:

1. **Lazy Activation** - Nothing happens until the element is mounted
2. **Ownership System** - Every element owns its subscriptions
3. **Automatic Cleanup** - Unmounting triggers cleanup automatically

This design provides:
- ✅ Type safety
- ✅ Memory safety
- ✅ Composability
- ✅ Predictable lifecycle
- ✅ No manual cleanup needed

The implicit owner pattern ensures that subscriptions are always properly managed without requiring manual owner passing in user code. 🎯

