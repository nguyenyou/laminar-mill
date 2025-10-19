# Rendering Quick Reference

Quick reference for understanding `render(document.querySelector("#app"), div("hello world"))` in Laminar.

---

## The 7 Phases

| Phase | What Happens | DOM Created? | Mounted? | Active? |
|-------|--------------|--------------|----------|---------|
| 1. Text Node Creation | String → TextNode → dom.Text | ✅ Yes | ❌ No | ❌ No |
| 2. Element Creation | div → ReactiveHtmlElement → dom.HTMLDivElement | ✅ Yes | ❌ No | ❌ No |
| 3. Modifier Application | TextNode appended to div in DOM | ✅ Yes | ❌ No | ❌ No |
| 4. Render Function | render() creates RootNode | ✅ Yes | ❌ No | ❌ No |
| 5. Root Activation | Root.dynamicOwner.activate() | ✅ Yes | ❌ No | ✅ Root active |
| 6. DOM Insertion | Div appended to container | ✅ Yes | ✅ Yes | ✅ Root active |
| 7. Child Activation | Div.dynamicOwner.activate() | ✅ Yes | ✅ Yes | ✅ Both active |

---

## Key Classes and Their Roles

| Class | Role | Created When | Lifecycle |
|-------|------|--------------|-----------|
| `TextNode` | Wraps DOM text node | String implicit conversion | Per text node |
| `ReactiveHtmlElement` | Wraps DOM element | Element creation | Per element |
| `DynamicOwner` | Manages subscriptions for an element | Element creation | Per element |
| `pilotSubscription` | Manages element's lifecycle | Element creation | Per element |
| `TransferableSubscription` | Can transfer between owners | Element creation | Per element |
| `RootNode` | Manages mounting to container | render() call | Per render() call |
| `OneTimeOwner` | Owner for a single mount cycle | DynamicOwner.activate() | Per activation |
| `DynamicSubscription` | Subscription that can be activated/deactivated | pilotSubscription.setOwner() | Per parent |

---

## The Ownership Hierarchy

```
RootNode
  └─ dynamicOwner (active)
      └─ OneTimeOwner
          └─ Subscription (from div's pilotSubscription)
              └─ cleanup: () => div.dynamicOwner.deactivate()

Div
  └─ dynamicOwner (active)
      └─ OneTimeOwner
          └─ (subscriptions from event listeners, etc.)
  └─ pilotSubscription
      └─ DynamicSubscription (owned by root's OneTimeOwner)
```

---

## Source File Quick Reference

### Text Node Creation
- `laminar/api/Implicits.scala:46-48` - `textToTextNode` implicit conversion
- `laminar/modifiers/RenderableText.scala:32` - `stringRenderable` instance
- `laminar/nodes/TextNode.scala:6-11` - `TextNode` constructor
- `laminar/DomApi.scala:469` - `createTextNode()` method

### Element Creation
- `laminar/defs/tags/HtmlTags.scala` - `div` tag definition
- `laminar/tags/HtmlTag.scala:13-17` - `apply()` method
- `laminar/tags/HtmlTag.scala:22` - `build()` method
- `laminar/DomApi.scala:153-155` - `createHtmlElement()` method
- `laminar/nodes/ReactiveElement.scala:24-27` - `pilotSubscription` creation
- `laminar/nodes/ParentNode.scala:12-15` - `dynamicOwner` creation

### Modifier Application
- `laminar/nodes/ChildNode.scala:38-40` - `apply()` method
- `laminar/nodes/ParentNode.scala:35-51` - `appendChild()` method
- `laminar/DomApi.scala:27-37` - `appendChild()` DOM method

### Render & RootNode
- `laminar/api/Laminar.scala:90-95` - `render()` function
- `laminar/nodes/RootNode.scala:21-39` - `RootNode` constructor
- `laminar/nodes/RootNode.scala:42-45` - `mount()` method

### Activation & Lifecycle
- `airstream/ownership/DynamicOwner.scala:51-83` - `activate()` method
- `laminar/nodes/ReactiveElement.scala:183-189` - `willSetParent()` method
- `laminar/nodes/ReactiveElement.scala:193-203` - `setParent()` method
- `laminar/nodes/ReactiveElement.scala:218-224` - `unsafeSetPilotSubscriptionOwner()` method
- `airstream/ownership/TransferableSubscription.scala:40-107` - `setOwner()` method
- `airstream/ownership/DynamicSubscription.scala:44-49` - `onActivate()` method

---

## Method Call Chain

### Complete Flow

```
render(document.querySelector("#app"), div("hello world"))
  │
  ├─ TEXT NODE CREATION
  │   └─ "hello world"
  │       └─ Implicits.textToTextNode[String]("hello world")
  │           └─ RenderableText.stringRenderable.asString("hello world")
  │           └─ new TextNode("hello world")
  │               └─ DomApi.createTextNode("hello world")
  │                   └─ document.createTextNode("hello world")
  │
  ├─ ELEMENT CREATION
  │   └─ div(...)
  │       └─ HtmlTag.apply(modifiers)
  │           ├─ build()
  │           │   └─ new ReactiveHtmlElement(tag, DomApi.createHtmlElement(tag))
  │           │       ├─ document.createElement("div")
  │           │       ├─ new DynamicOwner(...)
  │           │       └─ new TransferableSubscription(activate, deactivate)
  │           └─ modifiers.foreach(modifier => modifier(element))
  │
  ├─ MODIFIER APPLICATION
  │   └─ TextNode.apply(div)
  │       └─ ParentNode.appendChild(div, textNode)
  │           ├─ textNode.willSetParent(Some(div))
  │           ├─ DomApi.appendChild(div.ref, textNode.ref)
  │           │   └─ div.ref.appendChild(textNode.ref)
  │           └─ textNode.setParent(Some(div))
  │
  ├─ RENDER FUNCTION
  │   └─ render(container, div)
  │       └─ new RootNode(container, div)
  │
  ├─ ROOTNODE CREATION
  │   └─ RootNode constructor
  │       ├─ Validate container
  │       └─ mount()
  │
  ├─ ROOT ACTIVATION
  │   └─ mount()
  │       ├─ dynamicOwner.activate()
  │       │   ├─ new OneTimeOwner(...)
  │       │   └─ subscriptions.foreach(_.onActivate(newOwner))
  │       └─ ParentNode.appendChild(root, div)
  │
  ├─ DOM INSERTION
  │   └─ ParentNode.appendChild(root, div)
  │       ├─ div.willSetParent(Some(root))
  │       ├─ DomApi.appendChild(container, div.ref)
  │       │   └─ container.appendChild(div.ref)
  │       └─ div.setParent(Some(root))
  │
  └─ CHILD ACTIVATION
      └─ div.setParent(Some(root))
          └─ setPilotSubscriptionOwner(Some(root))
              └─ pilotSubscription.setOwner(root.dynamicOwner)
                  ├─ DynamicSubscription.unsafe(root.dynamicOwner, activate)
                  └─ DynamicSubscription.onActivate(root.oneTimeOwner)
                      └─ activate()
                          └─ div.dynamicOwner.activate()
                              └─ new OneTimeOwner(...)
```

---

## Key Insights

1. **Immediate DOM Creation**: DOM elements are created immediately, not lazily
2. **Deferred Activation**: DynamicOwners are created but not activated until mount
3. **pilotSubscription**: Bridges parent's DynamicOwner to child's DynamicOwner
4. **Automatic Activation**: When child is appended to active parent, it's automatically activated
5. **TransferableSubscription**: Allows moving elements without deactivating
6. **Ownership Chain**: Root owns div's pilotSubscription, which manages div's lifecycle
7. **No Manual Cleanup**: Unmounting root automatically deactivates all children

---

## State Transitions

### Element Creation
```
String "hello world"
  → TextNode (has dom.Text ref)
  → div(...) creates ReactiveHtmlElement
  → ReactiveHtmlElement has:
      - ref: dom.HTMLDivElement
      - dynamicOwner: DynamicOwner (inactive)
      - pilotSubscription: TransferableSubscription (no owner)
```

### After Modifier Application
```
ReactiveHtmlElement
  → TextNode appended to div in DOM
  → DOM structure: <div>hello world</div>
  → Still not mounted to document
  → Still inactive
```

### After render()
```
RootNode created
  → RootNode.mount() called
  → Root.dynamicOwner.activate()
  → Root.dynamicOwner: ACTIVE
  → Root.OneTimeOwner created
```

### After appendChild(root, div)
```
Div appended to container
  → div.setParent(Some(root))
  → pilotSubscription.setOwner(root.dynamicOwner)
  → DynamicSubscription created and owned by root.OneTimeOwner
  → DynamicSubscription.onActivate() called
  → div.dynamicOwner.activate()
  → Div.dynamicOwner: ACTIVE
  → Div.OneTimeOwner created
```

### Final State
```
DOM: <div id="app"><div>hello world</div></div>
Root.dynamicOwner: ACTIVE
Div.dynamicOwner: ACTIVE
Complete ownership chain established
```

---

## Common Patterns

### Simple Text
```scala
render(container, div("hello"))
```

### Multiple Children
```scala
render(container, div(
  h1("Title"),
  p("Paragraph"),
  span("Span")
))
```

### Nested Elements
```scala
render(container, div(
  div(
    div("deeply nested")
  )
))
```

### With Attributes
```scala
render(container, div(
  className := "container",
  "hello world"
))
```

### With Event Listeners
```scala
render(container, div(
  onClick --> Observer { _ => println("clicked") },
  "click me"
))
```

---

## Debugging Checklist

- [ ] Is the container element in the document? (`DomApi.isDescendantOf(container, document)`)
- [ ] Is the container not null?
- [ ] Is the root's dynamicOwner active? (`root.dynamicOwner.isActive`)
- [ ] Is the child's dynamicOwner active? (`child.dynamicOwner.isActive`)
- [ ] Does the child have a parent? (`child.maybeParent`)
- [ ] Is the pilotSubscription owned? (`pilotSubscription.hasOwner`)
- [ ] Is the DOM structure correct? (inspect in browser DevTools)

---

## Performance Tips

1. **Batch DOM Updates**: Use `Transaction.onStart.shared { ... }` for multiple updates
2. **Reuse Elements**: Don't recreate elements unnecessarily
3. **Lazy Evaluation**: Tags are lazy vals, created only when accessed
4. **Direct DOM Manipulation**: No virtual DOM diffing overhead
5. **Type Safety**: Compile-time type checking prevents runtime errors

---

## Memory Management

**Laminar uses strong references everywhere, but manages lifecycle explicitly:**

- ✅ **Automatic cleanup** on unmount
- ✅ **No manual cleanup** needed in user code
- ✅ **No memory leaks** if elements are properly unmounted
- ❌ **No weak references** (explicit lifecycle instead)
- ❌ **No garbage collection magic** (disciplined resource management)

**Cleanup flow:**
```
root.unmount()
  → root.dynamicOwner.deactivate()
  → root.OneTimeOwner.killSubscriptions()
  → div.pilotSubscription cleanup called
  → div.dynamicOwner.deactivate()
  → div.OneTimeOwner.killSubscriptions()
  → All subscriptions cleaned up
  → No memory leaks!
```

---

## Comparison with Other Frameworks

### React
```javascript
// React
ReactDOM.render(<div>hello world</div>, container)
```
- Virtual DOM diffing
- Reconciliation algorithm
- Fiber architecture
- Automatic re-rendering

### Laminar
```scala
// Laminar
render(container, div("hello world"))
```
- Direct DOM manipulation
- No virtual DOM
- Explicit lifecycle management
- Manual re-rendering (via signals)

**Laminar's advantage:** Predictable performance, no hidden re-renders, explicit control

---

## Further Reading

- [COMPLETE_RENDERING_FLOW.md](COMPLETE_RENDERING_FLOW.md) - Complete detailed flow
- [COMPLETE_EVENT_HANDLING_FLOW.md](COMPLETE_EVENT_HANDLING_FLOW.md) - Event handling flow
- [rendering-flow.md](rendering-flow.md) - Original rendering flow documentation
- [counter-app-program-flow.md](counter-app-program-flow.md) - Counter app flow

---

## Glossary

- **TextNode**: Laminar wrapper around DOM text node
- **ReactiveElement**: Laminar wrapper around DOM element
- **DynamicOwner**: Manages subscriptions for an element, can be activated/deactivated
- **pilotSubscription**: Manages element's lifecycle (mount/unmount)
- **TransferableSubscription**: Subscription that can transfer between owners
- **RootNode**: Special node that manages mounting to a container
- **OneTimeOwner**: Owner for a single mount cycle
- **DynamicSubscription**: Subscription that can be activated/deactivated multiple times
- **Modifier**: Something that can be applied to an element (text, attributes, events, etc.)
- **ChildNode**: Node that can be a child of another node

---

**Last Updated:** 2025-10-19

TEXT NODE CREATION:
  1. "hello world" → implicit conversion → textToTextNode
  2. RenderableText[String].asString("hello world")
  3. new TextNode("hello world")
  4. DomApi.createTextNode("hello world") → dom.Text

ELEMENT CREATION:
  5. div tag accessed → HtmlTag[HTMLDivElement]
  6. div(...) → HtmlTag.apply(modifiers)
  7. build() → DomApi.createHtmlElement("div")
  8. new ReactiveHtmlElement(tag, domElement)
  9. ReactiveElement creates pilotSubscription
  10. ParentNode creates dynamicOwner (inactive)

MODIFIER APPLICATION:
  11. TextNode.apply(div) → ParentNode.appendChild(div, textNode)
  12. DomApi.appendChild(div.ref, textNode.ref)
  13. DOM: <div>hello world</div>

RENDER FUNCTION:
  14. render(container, div) → new RootNode(container, div)

ROOTNODE CREATION:
  15. RootNode validates container
  16. RootNode.mount() is called

MOUNTING PROCESS:
  17. root.dynamicOwner.activate()
  18. Creates OneTimeOwner for root

DOM INSERTION:
  19. ParentNode.appendChild(root, div)
  20. div.willSetParent(Some(root))
  21. DomApi.appendChild(container, div.ref)
  22. div.setParent(Some(root))
  23. setPilotSubscriptionOwner(Some(root))

CHILD ACTIVATION:
  24. pilotSubscription.setOwner(root.dynamicOwner)
  25. Creates DynamicSubscription
  26. DynamicSubscription registered with root.dynamicOwner
  27. DynamicSubscription.onActivate(root.oneTimeOwner)
  28. activate() calls div.dynamicOwner.activate()
  29. Creates OneTimeOwner for div

FINAL STATE:
  30. DOM: <div id="app"><div>hello world</div></div>
  31. Root's DynamicOwner active
  32. Div's DynamicOwner active
  33. Complete ownership chain established