# Rendering vs Event Handling in Laminar

This document compares the flow of simple rendering vs event handling to highlight the similarities and differences.

---

## Side-by-Side Comparison

### Simple Rendering
```scala
render(document.querySelector("#app"), div("hello world"))
```

### Event Handling
```scala
render(document.querySelector("#app"), div(onClick --> Observer { _ => println("clicked") }))
```

---

## Phase-by-Phase Comparison

| Phase | Simple Rendering | Event Handling |
|-------|------------------|----------------|
| **1. Content Creation** | String → TextNode → dom.Text | EventProp → EventProcessor → EventListener |
| **2. Element Creation** | div(...) → ReactiveHtmlElement | div(...) → ReactiveHtmlElement |
| **3. Modifier Application** | TextNode appended to div | EventListener.bind() creates DynamicSubscription |
| **4. Render** | render() creates RootNode | render() creates RootNode |
| **5. Root Activation** | Root.dynamicOwner.activate() | Root.dynamicOwner.activate() |
| **6. DOM Insertion** | Div appended to container | Div appended to container |
| **7. Child Activation** | Div.dynamicOwner.activate() | Div.dynamicOwner.activate() + EventListener activated |
| **8. Runtime** | Static content displayed | Event listener registered, waiting for events |

---

## Key Differences

### 1. Modifier Type

**Simple Rendering:**
```scala
TextNode extends ChildNode extends Modifier
```
- TextNode is a DOM node
- Applied by appending to parent
- No subscriptions created

**Event Handling:**
```scala
EventListener extends Binder extends Modifier
```
- EventListener is a Binder
- Applied by creating a DynamicSubscription
- Subscription created on activation

---

### 2. Modifier Application

**Simple Rendering:**
```scala
// TextNode.apply(element)
override def apply(parentNode: ReactiveElement.Base): Unit = {
  ParentNode.appendChild(parent = parentNode, child = this, hooks = js.undefined)
}
```
- Appends the text node to the DOM
- No subscriptions created
- Happens during element creation

**Event Handling:**
```scala
// EventListener.bind(element)
private[laminar] def bind(element: ReactiveElement.Base, unsafePrepend: Boolean): DynamicSubscription = {
  val subscribe = (ctx: MountContext[ReactiveElement.Base]) => {
    DomApi.addEventListener(element.ref, this)
    new Subscription(ctx.owner, cleanup = ...)
  }
  ReactiveElement.bindSubscriptionUnsafe(element)(subscribe)
}
```
- Creates a DynamicSubscription
- Registers with element's DynamicOwner
- DOM listener NOT registered yet (deferred until activation)

---

### 3. Activation Phase

**Simple Rendering:**
```
Div.dynamicOwner.activate()
  → Creates OneTimeOwner
  → No subscriptions to activate (in this simple example)
  → Done
```

**Event Handling:**
```
Div.dynamicOwner.activate()
  → Creates OneTimeOwner
  → Activates all DynamicSubscriptions
  → EventListener's DynamicSubscription.onActivate(oneTimeOwner)
  → subscribe function is called
  → DomApi.addEventListener(element.ref, domCallback)
  → Browser registers event listener
  → Subscription created and owned by OneTimeOwner
```

---

### 4. Runtime Behavior

**Simple Rendering:**
- Static content
- No ongoing work
- No subscriptions active
- No memory overhead beyond the DOM nodes

**Event Handling:**
- Dynamic behavior
- Event listener waiting for events
- Subscription active
- Memory overhead: DynamicSubscription + Subscription + domCallback

---

### 5. Cleanup

**Simple Rendering:**
```
root.unmount()
  → root.dynamicOwner.deactivate()
  → div.pilotSubscription cleanup
  → div.dynamicOwner.deactivate()
  → Div removed from DOM
  → No subscriptions to clean up
```

**Event Handling:**
```
root.unmount()
  → root.dynamicOwner.deactivate()
  → div.pilotSubscription cleanup
  → div.dynamicOwner.deactivate()
  → EventListener's DynamicSubscription.onDeactivate()
  → Subscription.kill()
  → cleanup() removes DOM event listener
  → DomApi.removeEventListener(element.ref, domCallback)
  → Browser unregisters event listener
  → Div removed from DOM
```

---

## Similarities

### 1. Element Creation
Both create the same ReactiveHtmlElement with:
- DOM element ref
- DynamicOwner (inactive)
- pilotSubscription

### 2. Mounting Process
Both follow the same mounting process:
1. RootNode created
2. Root.dynamicOwner.activate()
3. Div appended to container
4. pilotSubscription.setOwner(root.dynamicOwner)
5. Div.dynamicOwner.activate()

### 3. Ownership Chain
Both establish the same ownership chain:
```
Root.dynamicOwner
  └─ OneTimeOwner
      └─ Subscription (from div's pilotSubscription)
```

### 4. Lifecycle Management
Both use the same lifecycle management:
- pilotSubscription manages element lifecycle
- DynamicOwner manages subscriptions
- Automatic activation on mount
- Automatic deactivation on unmount

---

## Subscription Count Comparison

### Simple Rendering

**Root:**
- 1 Subscription (from div's pilotSubscription)

**Div:**
- 0 Subscriptions (no event listeners or reactive content)

**Total:** 1 Subscription

---

### Event Handling

**Root:**
- 1 Subscription (from div's pilotSubscription)

**Div:**
- 1 DynamicSubscription (from EventListener)
  - 1 Subscription (created on activation)

**Total:** 2 Subscriptions (1 from pilotSubscription, 1 from EventListener)

---

## Memory Footprint Comparison

### Simple Rendering

```
RootNode
  └─ dynamicOwner
      └─ OneTimeOwner
          └─ Subscription (pilotSubscription)

Div
  └─ dynamicOwner
      └─ OneTimeOwner
  └─ pilotSubscription
      └─ DynamicSubscription

TextNode
  └─ ref: dom.Text
```

**Memory:** ~5 objects (RootNode, Div, TextNode, 2 DynamicOwners, 2 OneTimeOwners, 1 pilotSubscription, 1 DynamicSubscription, 1 Subscription)

---

### Event Handling

```
RootNode
  └─ dynamicOwner
      └─ OneTimeOwner
          └─ Subscription (pilotSubscription)

Div
  └─ dynamicOwner
      └─ OneTimeOwner
          └─ Subscription (from EventListener)
  └─ pilotSubscription
      └─ DynamicSubscription
  └─ maybeEventListeners
      └─ [EventListener]

EventListener
  └─ eventProcessor
  └─ callback
  └─ domCallback (JS function)
```

**Memory:** ~8 objects (RootNode, Div, 2 DynamicOwners, 2 OneTimeOwners, 1 pilotSubscription, 2 DynamicSubscriptions, 2 Subscriptions, 1 EventListener, 1 EventProcessor, 1 domCallback)

**Additional:** Browser's internal event listener registration

---

## Performance Comparison

### Simple Rendering

**Creation:** O(1)
- Create TextNode
- Create Div
- Append TextNode to Div

**Mounting:** O(1)
- Activate Root
- Append Div to container
- Activate Div

**Runtime:** O(0)
- No ongoing work

**Unmounting:** O(1)
- Deactivate Div
- Deactivate Root
- Remove from DOM

---

### Event Handling

**Creation:** O(1)
- Create EventListener
- Create Div
- Create DynamicSubscription

**Mounting:** O(1)
- Activate Root
- Append Div to container
- Activate Div
- Activate EventListener DynamicSubscription
- Register DOM event listener

**Runtime:** O(1) per event
- Browser calls domCallback
- domCallback calls processor
- processor calls callback

**Unmounting:** O(1)
- Deactivate EventListener DynamicSubscription
- Remove DOM event listener
- Deactivate Div
- Deactivate Root
- Remove from DOM

---

## When to Use Each Pattern

### Use Simple Rendering When:
- Displaying static content
- No user interaction needed
- No reactive updates needed
- Minimal memory footprint desired

### Use Event Handling When:
- User interaction required
- Need to respond to events
- Building interactive UIs
- Memory overhead is acceptable

---

## Combining Both Patterns

Most real applications combine both:

```scala
render(container, div(
  h1("Counter"),  // Simple rendering
  button(
    onClick --> Observer { _ => count.update(_ + 1) },  // Event handling
    "Increment"  // Simple rendering
  ),
  div(
    "Count: ",  // Simple rendering
    child.text <-- count  // Reactive rendering (another pattern!)
  )
))
```

**Subscriptions:**
- 1 from button's pilotSubscription
- 1 from onClick EventListener
- 1 from div's pilotSubscription
- 1 from child.text <-- count

**Total:** 4 Subscriptions

---

## Key Takeaways

1. **Same Foundation**: Both use the same element creation and mounting process
2. **Different Modifiers**: TextNode vs EventListener
3. **Deferred Registration**: Event listeners are registered on activation, not creation
4. **Automatic Cleanup**: Both benefit from automatic cleanup on unmount
5. **Composable**: Can combine static content, events, and reactive content seamlessly
6. **Predictable**: Both follow the same lifecycle management pattern

---

## Further Reading

- [COMPLETE_RENDERING_FLOW.md](COMPLETE_RENDERING_FLOW.md) - Complete rendering flow
- [COMPLETE_EVENT_HANDLING_FLOW.md](COMPLETE_EVENT_HANDLING_FLOW.md) - Complete event handling flow
- [RENDERING_SUMMARY.md](RENDERING_SUMMARY.md) - Rendering summary
- [EVENT_HANDLING_SUMMARY.md](EVENT_HANDLING_SUMMARY.md) - Event handling summary

---

**Last Updated:** 2025-10-19

