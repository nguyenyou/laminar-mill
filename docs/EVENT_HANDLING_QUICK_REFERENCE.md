# Event Handling Quick Reference

Quick reference for understanding `div(onClick --> Observer { _ => println("clicked") })` in Laminar.

---

## The 6 Phases

| Phase | What Happens | DOM Listener? | Owner Active? |
|-------|--------------|---------------|---------------|
| 1. Definition | Create EventProp → EventProcessor → EventListener | ❌ No | ❌ No |
| 2. Element Creation | Create ReactiveElement with DynamicOwner | ❌ No | ❌ No |
| 3. Binder Application | Create DynamicSubscription, register with DynamicOwner | ❌ No | ❌ No |
| 4. Mounting | Activate DynamicOwner, create OneTimeOwner, register DOM listener | ✅ **YES** | ✅ **YES** |
| 5. Event Handling | Browser calls domCallback → processor → callback | ✅ Yes | ✅ Yes |
| 6. Cleanup | Deactivate DynamicOwner, remove DOM listener, kill OneTimeOwner | ❌ No | ❌ No |

---

## Key Classes and Their Roles

| Class | Role | Created When | Lifecycle |
|-------|------|--------------|-----------|
| `EventProp` | Represents an event type (e.g., "click") | Definition | Singleton (lazy val) |
| `EventProcessor` | Transforms/filters events | Definition (implicit conversion) | Per usage |
| `EventListener` | Binder that creates subscriptions | Definition (`-->` operator) | Per usage |
| `ReactiveElement` | DOM element wrapper | Element creation | Per element |
| `DynamicOwner` | Manages subscriptions for an element | Element creation | Per element |
| `DynamicSubscription` | Subscription that can be activated/deactivated | Binder application | Per binder |
| `OneTimeOwner` | Owner for a single mount cycle | Mounting (activate) | Per mount |
| `Subscription` | Represents a leaky resource | Mounting (activate) | Per mount |
| `MountContext` | Carries implicit owner | Mounting (activate) | Per activation |

---

## The Ownership Hierarchy

```
ReactiveElement
  └─ DynamicOwner (1 per element, reusable)
      └─ DynamicSubscription[] (N per element, reusable)
          └─ OneTimeOwner (1 per mount, disposable)
              └─ Subscription[] (N per mount, disposable)
```

**Key insight:** 
- `DynamicOwner` and `DynamicSubscription` are **reusable** (can be activated/deactivated multiple times)
- `OneTimeOwner` and `Subscription` are **disposable** (created on mount, killed on unmount)

---

## The Implicit Owner Flow

```scala
// Step 1: DynamicOwner.activate() creates OneTimeOwner
val newOwner = new OneTimeOwner(...)

// Step 2: Pass to DynamicSubscription
sub.onActivate(newOwner)

// Step 3: Create MountContext with implicit owner
new MountContext[El](element, newOwner)
//                            ↑
//                    implicit val owner: Owner

// Step 4: Subscribe function receives context
(ctx: MountContext) => {
  new Subscription(ctx.owner, cleanup)
  //               ↑
  //         Implicit owner extracted
}

// Step 5: Subscription registers with owner
owner.own(this)
```

---

## Source File Quick Reference

### Event Definition
- `laminar/defs/eventProps/GlobalEventProps.scala:34` - `onClick` definition
- `laminar/keys/EventProp.scala:14` - `EventProp` class
- `laminar/api/Implicits.scala:41-43` - Implicit conversion to `EventProcessor`

### The `-->` Operator
- `laminar/keys/EventProcessor.scala:36-38` - `-->` method
- `laminar/modifiers/EventListener.scala:12-15` - `EventListener` class
- `laminar/modifiers/EventListener.scala:24-27` - `domCallback` definition

### Element Creation
- `laminar/tags/HtmlTag.scala:13-17` - `div(...)` constructor
- `laminar/nodes/ReactiveElement.scala:24-27` - `pilotSubscription`
- `laminar/nodes/ParentNode.scala:12-15` - `DynamicOwner` creation

### Binder Application
- `laminar/modifiers/EventListener.scala:38-74` - `bind()` method
- `laminar/nodes/ReactiveElement.scala:272-281` - `bindSubscriptionUnsafe()`
- `airstream/ownership/DynamicSubscription.scala:26-35` - Constructor

### Mounting
- `laminar/nodes/RootNode.scala:42-45` - `mount()` method
- `airstream/ownership/DynamicOwner.scala:51-83` - `activate()` method
- `airstream/ownership/DynamicSubscription.scala:44-49` - `onActivate()` method
- `laminar/lifecycle/MountContext.scala:6-9` - `MountContext` with implicit owner
- `laminar/DomApi.scala:126-136` - `addEventListener()` method
- `airstream/ownership/Subscription.scala:13-23` - Constructor with `owner.own(this)`

### Event Handling
- `laminar/modifiers/EventListener.scala:24-27` - `domCallback` called by browser
- `laminar/keys/EventProcessor.scala:474-476` - `processor` function

### Cleanup
- `laminar/nodes/ReactiveElement.scala:183-189` - `willSetParent()` detects unmount
- `airstream/ownership/DynamicOwner.scala:85-107` - `deactivate()` method
- `airstream/ownership/DynamicSubscription.scala:51-56` - `onDeactivate()` method
- `airstream/ownership/Subscription.scala` - `kill()` method
- `laminar/DomApi.scala:138-146` - `removeEventListener()` method

---

## Method Call Chain

### On Element Creation
```
div(onClick --> Observer { _ => println("clicked") })
  └─ HtmlTag.apply(modifiers)
      ├─ build() → new ReactiveHtmlElement
      │   └─ new DynamicOwner
      └─ modifiers.foreach(modifier => modifier(element))
          └─ EventListener.apply(element)
              └─ EventListener.bind(element)
                  ├─ Create subscribe function
                  ├─ ReactiveElement.bindSubscriptionUnsafe(element)(subscribe)
                  │   └─ DynamicSubscription.unsafe(element.dynamicOwner, activate)
                  │       └─ new DynamicSubscription(dynamicOwner, activate, prepend)
                  │           └─ dynamicOwner.addSubscription(this, prepend)
                  └─ element.addEventListener(this, unsafePrepend)
```

### On Mounting
```
render(container, div)
  └─ new RootNode(container, div)
      └─ mount()
          ├─ dynamicOwner.activate()
          │   ├─ new OneTimeOwner(...)
          │   └─ subscriptions.foreach(_.onActivate(newOwner))
          │       └─ DynamicSubscription.onActivate(newOwner)
          │           └─ activate(newOwner)
          │               └─ subscribe(new MountContext(element, newOwner))
          │                   ├─ DomApi.addEventListener(element.ref, this)
          │                   │   └─ element.ref.addEventListener("click", domCallback, options)
          │                   └─ new Subscription(ctx.owner, cleanup)
          │                       └─ owner.own(this)
          └─ ParentNode.appendChild(this, div)
```

### On Click
```
User clicks div
  └─ Browser fires click event
      └─ domCallback(event)
          └─ processor(event).foreach(callback)
              └─ Some(event).foreach(callback)
                  └─ callback(event)
                      └─ println("clicked")
```

### On Unmount
```
Element removed from DOM
  └─ ReactiveElement.willSetParent(None)
      └─ pilotSubscription triggers deactivate
          └─ dynamicOwner.deactivate()
              ├─ subscriptions.foreach(_.onDeactivate())
              │   └─ DynamicSubscription.onDeactivate()
              │       └─ currentSubscription.kill()
              │           └─ cleanup()
              │               ├─ element.removeEventListener(index)
              │               └─ DomApi.removeEventListener(element.ref, this)
              │                   └─ element.ref.removeEventListener("click", domCallback, options)
              └─ currentOwner.killSubscriptions()
```

---

## Common Patterns

### Basic Event
```scala
div(onClick --> Observer { ev => println(s"Clicked at ${ev.clientX}") })
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
a(href := "#", onClick.preventDefault --> Observer { _ => println("Prevented") })
```

### Multiple Handlers
```scala
div(
  onClick --> Observer { _ => println("First") },
  onClick --> Observer { _ => println("Second") }
)
```

### Event Stream
```scala
val clicks: EventStream[dom.MouseEvent] = div.events(onClick)
```

---

## Key Insights

1. **Lazy Registration**: DOM listeners are NOT registered until mount
2. **Ownership Chain**: Element → DynamicOwner → OneTimeOwner → Subscription
3. **Implicit Flow**: OneTimeOwner → MountContext (implicit) → Subscription
4. **Automatic Cleanup**: Unmount triggers deactivation and cleanup
5. **Reusable Subscriptions**: DynamicSubscription can be activated/deactivated multiple times
6. **Event Processing**: Events flow through processor before reaching callback

---

## Debugging Checklist

- [ ] Is the element mounted? (`element.dynamicOwner.isActive`)
- [ ] Is the event name correct? ("click" not "onClick")
- [ ] Is the EventListener registered? (`element.eventListeners`)
- [ ] Is the DynamicSubscription active? (`element.dynamicOwner.numSubscriptions`)
- [ ] Is the processor filtering events? (check processor logic)
- [ ] Is the callback throwing an exception? (check console)
- [ ] Are there multiple listeners? (check `element.eventListeners.length`)

---

## Performance Tips

1. **Reuse elements**: Don't recreate elements unnecessarily
2. **Use event delegation**: For lists, use a single listener on the parent
3. **Avoid expensive processors**: Keep processor functions fast
4. **Use passive listeners**: For scroll/touch events, use `passive = true`
5. **Batch updates**: Use `Transaction.onStart.shared { ... }` for multiple updates

---

## Memory Management

**Laminar uses strong references everywhere, but manages lifecycle explicitly:**

- ✅ **Automatic cleanup** on unmount
- ✅ **No manual cleanup** needed in user code
- ✅ **No memory leaks** if elements are properly unmounted
- ❌ **No weak references** (explicit lifecycle instead)
- ❌ **No garbage collection magic** (disciplined resource management)

**Best practices:**
- Always mount elements through `render()` or proper parent-child relationships
- Don't create subscriptions outside the element's owner
- Don't manually kill subscriptions (let the owner handle it)
- Use `DetachedRoot` for elements not in the DOM tree

---

## Further Reading

- [EVENT_HANDLING_SUMMARY.md](EVENT_HANDLING_SUMMARY.md) - Executive summary
- [COMPLETE_EVENT_HANDLING_FLOW.md](COMPLETE_EVENT_HANDLING_FLOW.md) - Complete detailed flow
- [LAMINAR_PATTERN_EXPLAINED.md](../examples/LAMINAR_PATTERN_EXPLAINED.md) - Simplified example
- [COMPARISON_WITH_REAL_LAMINAR.md](../examples/COMPARISON_WITH_REAL_LAMINAR.md) - Example vs real code

---

## Glossary

- **EventProp**: Represents an event type (e.g., "click")
- **EventProcessor**: Transforms/filters events before they reach the callback
- **EventListener**: Binder that creates event subscriptions
- **Binder**: Modifier that creates subscriptions when applied to elements
- **ReactiveElement**: Laminar's wrapper around DOM elements
- **DynamicOwner**: Manages subscriptions for an element, can be activated/deactivated
- **DynamicSubscription**: Subscription that can be activated/deactivated multiple times
- **OneTimeOwner**: Owner for a single mount cycle, created on activate, killed on deactivate
- **Subscription**: Represents a leaky resource that needs cleanup
- **MountContext**: Carries the implicit owner during activation
- **pilotSubscription**: Manages the element's lifecycle (mount/unmount)
- **domCallback**: JavaScript function registered with the browser
- **processor**: Function that transforms/filters events
- **callback**: User's event handler function

---

**Last Updated:** 2025-10-19

