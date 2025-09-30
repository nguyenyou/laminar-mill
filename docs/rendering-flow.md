# Laminar Rendering Flow: From `div()` to DOM

This document traces the complete execution path when rendering a div element in Laminar, from the code in [index.scala](www/src/www/index.scala) to the actual DOM element appearing in the browser.

## Starting Point: index.scala

```scala
@main def main(): Unit = {
  render(dom.document.getElementById("app"), div("Hello, Laminar"))
}
```

## Step-by-Step Execution Flow

### 1. **Creating the `div` Tag** - [HtmlTags.scala:666](laminar/src/io/github/nguyenyou/laminar/defs/tags/HtmlTags.scala#L666)

When you write `div(...)`, you're accessing a lazy val from the `L` object (via `import io.github.nguyenyou.laminar.api.L.*`):

```scala
lazy val div: HtmlTag[dom.HTMLDivElement] = htmlTag("div")
```

**What happens:**
- `L` is an instance of `Laminar` trait defined in [api/package.scala:9](laminar/src/io/github/nguyenyou/laminar/api/package.scala#L9)
- `Laminar` extends `HtmlTags` trait which provides tag definitions
- The `div` lazy val creates a new `HtmlTag[dom.HTMLDivElement]` with name `"div"`

---

### 2. **Applying Modifiers to the Tag** - [HtmlTag.scala:13](laminar/src/io/github/nguyenyou/laminar/tags/HtmlTag.scala#L13)

When you call `div("Hello, Laminar")`, you're invoking the `apply` method on `HtmlTag`:

```scala
def apply(modifiers: Modifier[ReactiveHtmlElement[Ref]]*): ReactiveHtmlElement[Ref] = {
  val element = build()
  modifiers.foreach(modifier => modifier(element))
  element
}
```

**What happens:**
1. Calls `build()` to create the actual DOM element and wrap it
2. Applies each modifier (like text content) to the element
3. Returns the wrapped reactive element

---

### 3. **Building the DOM Element** - [HtmlTag.scala:22](laminar/src/io/github/nguyenyou/laminar/tags/HtmlTag.scala#L22)

The `build()` method creates both the native DOM element and the Laminar wrapper:

```scala
protected def build(): ReactiveHtmlElement[Ref] =
  new ReactiveHtmlElement(this, DomApi.createHtmlElement(this))
```

#### 3a. **Creating the Native DOM Element** - [DomApi.scala:153](laminar/src/io/github/nguyenyou/laminar/DomApi.scala#L153)

```scala
def createHtmlElement[Ref <: dom.html.Element](tag: HtmlTag[Ref]): Ref = {
  dom.document.createElement(tag.name).asInstanceOf[Ref]
}
```

**What happens:**
- Calls the browser's native `document.createElement("div")`
- Returns a `dom.html.Element` (specifically `HTMLDivElement`)
- This is a **real JavaScript DOM element**, but it's not attached to the DOM tree yet

#### 3b. **Wrapping in ReactiveHtmlElement** - [ReactiveHtmlElement.scala:13](laminar/src/io/github/nguyenyou/laminar/nodes/ReactiveHtmlElement.scala#L13)

```scala
class ReactiveHtmlElement[+Ref <: dom.html.Element](
  override val tag: HtmlTag[Ref],
  final override val ref: Ref
) extends ReactiveElement[Ref]
```

**What happens:**
- Creates a Laminar `ReactiveHtmlElement` that wraps the native DOM element
- Stores reference to both the tag and the native element (`ref`)
- Inherits from `ReactiveElement`, which extends both `ChildNode` and `ParentNode`
- This wrapper enables Laminar's reactive features (event listeners, dynamic children, etc.)

---

### 4. **Applying Modifiers** - Back to [HtmlTag.scala:15](laminar/src/io/github/nguyenyou/laminar/tags/HtmlTag.scala#L15)

After building, modifiers are applied:

```scala
modifiers.foreach(modifier => modifier(element))
```

**For the text "Hello, Laminar":**
- String literals are implicitly converted to text modifiers via [Implicits.scala](laminar/src/io/github/nguyenyou/laminar/api/Implicits.scala)
- A `TextNode` is created and appended as a child to the div
- The text node contains the string content

---

### 5. **Rendering to the DOM** - [Laminar.scala:90](laminar/src/io/github/nguyenyou/laminar/api/Laminar.scala#L90)

Now we have a `ReactiveHtmlElement[HTMLDivElement]` with text content. The `render` function mounts it:

```scala
def render(
  container: dom.Element,
  rootNode: nodes.ReactiveElement.Base
): RootNode = {
  new RootNode(container, rootNode)
}
```

**What happens:**
- Takes the container element (from `document.getElementById("app")`)
- Takes the child element (our div)
- Creates a new `RootNode` to manage the mounting

---

### 6. **Creating RootNode** - [RootNode.scala:21](laminar/src/io/github/nguyenyou/laminar/nodes/RootNode.scala#L21)

```scala
class RootNode(
  val container: dom.Element,
  val child: ReactiveElement.Base
) extends ParentNode[dom.Element] {

  if (container == null) {
    throw new Exception("Unable to mount Laminar RootNode into a null container...")
  }

  if (!DomApi.isDescendantOf(container, dom.document)) {
    throw new Exception("Unable to mount Laminar RootNode into an unmounted container...")
  }

  final override val ref: dom.Element = container

  mount()
}
```

**What happens:**
1. **Validation**: Checks that container exists and is attached to the DOM
2. **Reference**: Uses the container as its own `ref` (doesn't create a new element)
3. **Auto-mount**: Immediately calls `mount()` in the constructor

---

### 7. **Mounting the Child** - [RootNode.scala:42](laminar/src/io/github/nguyenyou/laminar/nodes/RootNode.scala#L42)

```scala
def mount(): Boolean = {
  dynamicOwner.activate()
  ParentNode.appendChild(parent = this, child, hooks = js.undefined)
}
```

**What happens:**
1. **Activate subscriptions**: Activates the `dynamicOwner` which manages reactive subscriptions
2. **Append child**: Calls `ParentNode.appendChild` to add the div to the container

---

### 8. **Appending to DOM** - [ParentNode.scala:35](laminar/src/io/github/nguyenyou/laminar/nodes/ParentNode.scala#L35)

```scala
def appendChild(
  parent: ParentNode.Base,
  child: ChildNode.Base,
  hooks: js.UndefOr[InserterHooks]
): Boolean = {
  val nextParent = Some(parent)
  child.willSetParent(nextParent)

  // 1. Update DOM
  hooks.foreach(_.onWillInsertNode(parent = parent, child = child))
  val appended = DomApi.appendChild(parent = parent.ref, child = child.ref)
  if (appended) {
    // 3. Update child
    child.setParent(nextParent)
  }
  appended
}
```

**What happens:**
1. **Pre-notification**: Calls `willSetParent` on the child (for lifecycle hooks)
2. **Optional hooks**: Runs inserter hooks if provided (none in this case)
3. **DOM update**: Calls `DomApi.appendChild` with the native DOM references
4. **Post-update**: Updates the Laminar parent-child relationship

---

### 9. **Native DOM Append** - [DomApi.scala:27](laminar/src/io/github/nguyenyou/laminar/DomApi.scala#L27)

```scala
def appendChild(
  parent: dom.Node,
  child: dom.Node
): Boolean = {
  try {
    parent.appendChild(child)
    true
  } catch {
    case JavaScriptException(_: dom.DOMException) => false
  }
}
```

**What happens:**
- **THE ACTUAL DOM MANIPULATION**: Calls the browser's native `parent.appendChild(child)`
- In our case: `container.appendChild(divElement)`
- The div element (with its text node child) is now **physically in the browser's DOM tree**
- The browser will render it visually on the next repaint

---

## Summary: The Complete Path

```
1. div("Hello, Laminar")
   └─> HtmlTag[HTMLDivElement].apply(modifiers)
       ├─> build()
       │   ├─> DomApi.createHtmlElement(tag)
       │   │   └─> document.createElement("div")  ← NATIVE ELEMENT CREATED
       │   └─> new ReactiveHtmlElement(tag, element)
       └─> Apply text modifier
           └─> Create TextNode("Hello, Laminar")

2. render(container, divElement)
   └─> new RootNode(container, divElement)
       └─> mount()
           ├─> dynamicOwner.activate()  ← REACTIVE SUBSCRIPTIONS START
           └─> ParentNode.appendChild(this, child)
               ├─> child.willSetParent(Some(parent))
               └─> DomApi.appendChild(parent.ref, child.ref)
                   └─> parent.appendChild(child)  ← ELEMENT ATTACHED TO DOM!
```

## Key Architectural Components

### Node Hierarchy

```
ReactiveNode
    ├─> ChildNode (can be appended to parents)
    │   └─> ReactiveElement (is both child and parent)
    │       ├─> ReactiveHtmlElement (wraps HTML elements)
    │       └─> ReactiveSvgElement (wraps SVG elements)
    │
    └─> ParentNode (can have children)
        ├─> ReactiveElement (see above)
        └─> RootNode (special case: uses existing container)
```

### Reactive Features

- **DynamicOwner** ([ParentNode.scala:12](laminar/src/io/github/nguyenyou/laminar/nodes/ParentNode.scala#L12)): Manages subscriptions to Observables/EventStreams
- **pilotSubscription** ([ReactiveElement.scala:24](laminar/src/io/github/nguyenyou/laminar/nodes/ReactiveElement.scala#L24)): Auto-activates/deactivates when mounting/unmounting
- **Parent tracking** ([ChildNode.scala:13](laminar/src/io/github/nguyenyou/laminar/nodes/ChildNode.scala#L13)): Each node tracks its Laminar parent (parallel to DOM tree)

### Two Parallel Trees

Laminar maintains two tree structures:

1. **Native DOM Tree**: The actual browser DOM (accessed via `.ref`)
2. **Laminar Tree**: Parent-child relationships tracked in Scala objects

Both trees are kept in sync, but Laminar's tree enables:
- Lifecycle management (mount/unmount hooks)
- Subscription cleanup (when elements are removed)
- Reactive updates (observers, event listeners)

## Performance Notes

- Element creation uses `document.createElement` (fast, native browser API)
- No virtual DOM diffing - direct DOM manipulation
- Lazy evaluation: tags are lazy vals, created only when accessed
- Type safety: Full compile-time type checking for element types