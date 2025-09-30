# Rebuilding Laminar from Scratch: A Ground-Up Guide

This guide outlines a progressive approach to rebuilding Laminar from scratch, understanding each layer before moving to the next.

## Architecture Overview

Laminar consists of three main layers:
1. **ew** - JavaScript interop utilities (foundation)
2. **airstream** - FRP (Functional Reactive Programming) library (reactive core)
3. **laminar** - Reactive UI library (DOM abstraction)

## Phase 1: JavaScript Interop Layer (ew)

**Goal**: Create type-safe Scala.js wrappers for JavaScript types

**What to build first:**
- Extension methods for `js.Array` (push, unshift, splice, forEach, etc.)
- Extension methods for `js.Map` and `js.Set`
- String utilities with `.ew` extension

**Why start here:**
- No dependencies
- Small, contained scope (~7 files)
- Provides performance-optimized JS operations
- Foundation for efficient collections in upper layers

**Key files to study:**
- Check `ew/src/` directory for implementation patterns

---

## Phase 2: Airstream Core (~144 files)

This is the most complex layer. Build it incrementally:

### Step 2.1: Observable Foundation

**Core abstractions:**
1. **BaseObservable** ([airstream/src/.../core/BaseObservable.scala](airstream/src/io/github/nguyenyou/airstream/core/BaseObservable.scala))
   - Parent trait for all reactive values
   - Defines `map`, `filter`, `recover` operations
   - Observer management (add/remove)
   - Start/stop lifecycle

2. **Observable** ([airstream/src/.../core/Observable.scala](airstream/src/io/github/nguyenyou/airstream/core/Observable.scala))
   - Type alias wrapper around BaseObservable
   - Extension method holders

**What to implement:**
```scala
trait BaseObservable[+A] {
  def map[B](f: A => B): Self[B]
  def filter(f: A => Boolean): Self[A]
  def addObserver(observer: Observer[A]): Unit
  def removeObserver(observer: Observer[A]): Unit
  protected def onStart(): Unit
  protected def onStop(): Unit
}
```

### Step 2.2: EventStream

**Purpose**: Discrete events without initial value

**Key file:** [airstream/src/.../core/EventStream.scala](airstream/src/io/github/nguyenyou/airstream/core/EventStream.scala)

**What to implement:**
- `EventStream[A]` trait extending Observable
- Basic operators:
  - `map`, `filter`, `collect`
  - `delay`, `throttle`, `debounce`
  - `mergeWith`, `combineWith`
- Factory methods:
  - `EventStream.fromValue`
  - `EventStream.fromSeq`
  - `EventStream.empty`

**Core pattern:**
```scala
trait EventStream[+A] extends Observable[A] {
  // Streams fire discrete events
  // No concept of "current value"
  def delay(ms: Int): EventStream[A]
  def collect[B](pf: PartialFunction[A, B]): EventStream[B]
}
```

### Step 2.3: Signal

**Purpose**: Continuous values with current state

**Key file:** [airstream/src/.../state/Signal.scala](airstream/src/io/github/nguyenyou/airstream/state/Signal.scala)

**What to implement:**
- `Signal[A]` trait with `now(): A` method
- `Var[A]` - writable signal
- `scanLeft` - fold stream into signal
- Signal operators preserve signal nature

**Core pattern:**
```scala
trait Signal[+A] extends Observable[A] {
  // Always has a current value
  def now(): A
  def changes: EventStream[A]
}

class Var[A](initial: A) extends Signal[A] {
  def set(value: A): Unit
  def update(fn: A => A): Unit
  def writer: Observer[A]
}
```

### Step 2.4: Ownership System

**Critical for memory management**

**Key files:**
- [airstream/src/.../ownership/Owner.scala](airstream/src/io/github/nguyenyou/airstream/ownership/Owner.scala)
- [airstream/src/.../ownership/DynamicOwner.scala](airstream/src/io/github/nguyenyou/airstream/ownership/DynamicOwner.scala)
- `Subscription.scala`

**What to implement:**
1. **Owner** - manages subscriptions lifecycle
   ```scala
   trait Owner {
     protected val subscriptions: JsArray[Subscription]
     protected def killSubscriptions(): Unit
   }
   ```

2. **DynamicOwner** - can activate/deactivate repeatedly
   ```scala
   class DynamicOwner {
     def activate(): Unit
     def deactivate(): Unit
     def isActive: Boolean
   }
   ```

3. **Subscription** - represents a connection between observer and observable
   ```scala
   class Subscription(owner: Owner) {
     def kill(): Unit
   }
   ```

**Why this matters:**
- Prevents memory leaks
- Enables proper cleanup when components unmount
- Core to Laminar's lifecycle management

### Step 2.5: Transaction System

**Purpose**: Batching updates, glitch-free propagation

**Key concepts:**
- All events fire within a transaction
- Ensures each observer sees each value exactly once
- Topological ordering prevents glitches

**What to implement:**
- `Transaction` object with `onStart` hooks
- Topological rank tracking
- Pending observer management

---

## Phase 3: Laminar Core (~136 files)

Build on top of Airstream:

### Step 3.1: DOM API Layer

**First file to study:** [laminar/src/.../DomApi.scala](laminar/src/io/github/nguyenyou/laminar/DomApi.scala)

**What to implement:**
- Low-level DOM operations:
  - `createElement`, `createTextNode`
  - `appendChild`, `removeChild`, `insertBefore`
  - `setAttribute`, `setProperty`
  - `addEventListener`, `removeEventListener`
- Error handling (catches DOMException)
- Type-safe wrappers around `org.scalajs.dom`

**Why start here:**
- Self-contained
- No Laminar dependencies
- Foundation for all DOM manipulation

### Step 3.2: Node Abstractions

**Build the node hierarchy:**

1. **ChildNode** - base trait for anything that can be a child
   ```scala
   trait ChildNode[+Ref <: dom.Node] {
     val ref: Ref
     private[laminar] def setParent(parent: Option[ParentNode.Base]): Unit
   }
   ```

2. **ParentNode** - can have children
   ```scala
   trait ParentNode[+Ref <: dom.Node] extends ChildNode[Ref] {
     val dynamicOwner: DynamicOwner
     def appendChild(child: ChildNode.Base): Boolean
     def removeChild(child: ChildNode.Base): Boolean
   }
   ```

3. **ReactiveElement** ([laminar/src/.../nodes/ReactiveElement.scala](laminar/src/io/github/nguyenyou/laminar/nodes/ReactiveElement.scala))
   ```scala
   trait ReactiveElement[+Ref <: dom.Element]
     extends ChildNode[Ref] with ParentNode[Ref] {

     val tag: Tag[ReactiveElement[Ref]]
     val dynamicOwner: DynamicOwner

     def amend(mods: Modifier[this.type]*): this.type
     def events[Ev <: dom.Event](prop: EventProcessor[Ev]): EventStream[Ev]
   }
   ```

**Key insight:** Every element has a `dynamicOwner` that activates when mounted to DOM, deactivates when unmounted.

### Step 3.3: RootNode & Mounting

**Key file:** [laminar/src/.../nodes/RootNode.scala](laminar/src/io/github/nguyenyou/laminar/nodes/RootNode.scala)

**What to implement:**
```scala
class RootNode(
  val container: dom.Element,
  val child: ReactiveElement.Base
) extends ParentNode[dom.Element] {

  def mount(): Boolean = {
    dynamicOwner.activate()
    ParentNode.appendChild(this, child)
  }

  def unmount(): Boolean = {
    dynamicOwner.deactivate()
    ParentNode.removeChild(this, child)
  }
}
```

**Lifecycle:**
1. Create RootNode
2. Check container is in DOM
3. Activate dynamicOwner (starts all subscriptions)
4. Append child to container

### Step 3.4: Modifier System

**Key file:** [laminar/src/.../modifiers/Modifier.scala](laminar/src/io/github/nguyenyou/laminar/modifiers/Modifier.scala)

**Core abstraction:**
```scala
trait Modifier[-El <: ReactiveElement.Base] {
  def apply(element: El): Unit
}
```

**Types of modifiers to implement:**
1. **Static modifiers** - `attr := "value"`
2. **Reactive modifiers** - `attr <-- signal`
3. **Event listeners** - `onClick --> observer`
4. **Child inserters** - `child <-- stream`

### Step 3.5: Reactive Bindings

**Core pattern:**
```scala
// Static binding
element.setAttribute("id", "my-id")

// Reactive binding
signal.foreach { value =>
  element.setAttribute("id", value)
}(using element.dynamicOwner)
```

**Key receivers to implement:**
- `ChildReceiver` - single child
- `ChildrenReceiver` - list of children ([laminar/src/.../inserters/ChildrenInserter.scala](laminar/src/io/github/nguyenyou/laminar/inserters/ChildrenInserter.scala))
- `TextReceiver` - text content
- `FocusReceiver` - focus management

### Step 3.6: Dynamic Children

**Most complex part:** [laminar/src/.../inserters/ChildrenInserter.scala](laminar/src/io/github/nguyenyou/laminar/inserters/ChildrenInserter.scala)

**Challenge**: Efficiently update DOM when list changes

**Algorithm overview:**
1. Maintain map of current children
2. Compare with new children list
3. Minimal DOM operations:
   - Insert new nodes
   - Remove deleted nodes
   - Move existing nodes to correct position
4. Preserve element identity and state

**Key optimization**: Uses JS Map for O(1) lookups instead of repeated array scans

### Step 3.7: Tags, Attributes, Properties

**Generate type-safe definitions:**
- HTML tags: `div`, `span`, `input`, etc.
- HTML attributes: `href`, `src`, `className`, etc.
- HTML properties: `value`, `checked`, etc.
- CSS styles: `backgroundColor`, `fontSize`, etc.
- Event props: `onClick`, `onInput`, etc.

**Pattern:**
```scala
object div extends HtmlTag[html.Div]("div")
object href extends HtmlAttr[String]("href")
object onClick extends EventProp[MouseEvent]("click")
```

### Step 3.8: Public API

**Key file:** [laminar/src/.../api/Laminar.scala](laminar/src/io/github/nguyenyou/laminar/api/Laminar.scala)

**Assemble everything:**
```scala
trait Laminar
  extends HtmlTags
  with HtmlAttrs
  with HtmlProps
  with GlobalEventProps
  with StyleProps
  with MountHooks
  with AirstreamAliases
  with Implicits {

  def render(container: dom.Element, rootNode: ReactiveElement.Base): RootNode
  def renderOnDomContentLoaded(...): Unit

  val child: ChildReceiver.type
  val children: ChildrenReceiver.type
  val text: ChildTextReceiver.type

  def inContext[El](makeModifier: El => Modifier[El]): Modifier[El]
}
```

---

## Recommended Implementation Order

**Philosophy: Start static, then add reactivity layer by layer**

### Phase 1: Static DOM Only (Week 1-2)

**Goal: Render static HTML trees without any reactivity**

1. **Minimal DOM operations**
   ```scala
   object DomApi {
     def createElement(tagName: String): dom.Element
     def appendChild(parent: dom.Element, child: dom.Node): Unit
     def createTextNode(text: String): dom.Text
   }
   ```

2. **Basic node wrappers**
   ```scala
   class HtmlElement(tagName: String) {
     val ref: dom.Element = DomApi.createElement(tagName)
     def appendChild(child: ChildNode): Unit
   }

   class TextNode(text: String) {
     val ref: dom.Text = DomApi.createTextNode(text)
   }
   ```

3. **Static rendering**
   ```scala
   def render(container: dom.Element, element: HtmlElement): Unit = {
     container.appendChild(element.ref)
   }
   ```

4. **Test with:**
   ```scala
   val app = div(
     h1("Hello"),
     p("Static text")
   )
   render(dom.document.getElementById("app"), app)
   ```

### Phase 2: Static Modifiers (Week 2-3)

**Goal: Add attributes, properties, and inline styles**

5. **Modifier trait**
   ```scala
   trait Modifier {
     def apply(element: HtmlElement): Unit
   }
   ```

6. **Attribute setter** - `className := "header"`
   ```scala
   def setAttribute(name: String, value: String): Modifier = {
     element => element.ref.setAttribute(name, value)
   }
   ```

7. **Style setter** - `color := "red"`, `backgroundColor := "blue"`
   ```scala
   def setStyle(property: String, value: String): Modifier = {
     element => element.ref.style.setProperty(property, value)
   }
   ```

8. **Text content** - Simple text modifier
   ```scala
   def setText(text: String): Modifier = {
     element => element.ref.textContent = text
   }
   ```

9. **Test with:**
   ```scala
   val app = div(
     className := "container",
     backgroundColor := "lightblue",
     h1(
       color := "darkblue",
       "Styled Header"
     ),
     p("Normal text")
   )
   ```

### Phase 3: Dynamic Text (Week 3-4)

**Goal: Make text content reactive**

10. **Minimal Var** (just writable value holder)
    ```scala
    class Var[A](private var currentValue: A) {
      private var listeners: List[A => Unit] = Nil

      def set(value: A): Unit = {
        currentValue = value
        listeners.foreach(_(value))
      }

      def subscribe(f: A => Unit): Unit = {
        listeners = f :: listeners
        f(currentValue) // Fire immediately
      }
    }
    ```

11. **Text binder**
    ```scala
    object text {
      def <--(source: Var[String]): Modifier = { element =>
        source.subscribe { value =>
          element.ref.textContent = value
        }
      }
    }
    ```

12. **Test with:**
    ```scala
    val nameVar = Var("World")

    val app = div(
      h1("Hello, ", text <-- nameVar),
      button(
        onClick := (() => nameVar.set("Laminar")),
        "Change name"
      )
    )
    ```

### Phase 4: Dynamic Styles (Week 4-5)

**Goal: Make styles reactive**

13. **Style binder**
    ```scala
    def styleBinder(prop: String)(source: Var[String]): Modifier = {
      element =>
        source.subscribe { value =>
          element.ref.style.setProperty(prop, value)
        }
    }

    // Usage: backgroundColor <-- colorVar
    ```

14. **Test with:**
    ```scala
    val colorVar = Var("red")

    val app = div(
      h1(
        color <-- colorVar,
        "Colored text"
      ),
      button("Red", onClick := (() => colorVar.set("red"))),
      button("Blue", onClick := (() => colorVar.set("blue")))
    )
    ```

### Phase 5: Dynamic Children (Week 5-7)

**Goal: Add/remove elements dynamically**

15. **Single child binder** - `child <-- var`
    ```scala
    object child {
      def <--(source: Var[HtmlElement]): Modifier = { parent =>
        var currentChild: Option[HtmlElement] = None
        source.subscribe { newChild =>
          currentChild.foreach(old => parent.ref.removeChild(old.ref))
          parent.ref.appendChild(newChild.ref)
          currentChild = Some(newChild)
        }
      }
    }
    ```

16. **Multiple children binder** - `children <-- var`
    ```scala
    object children {
      def <--(source: Var[List[HtmlElement]]): Modifier = { parent =>
        source.subscribe { newChildren =>
          // Simple: clear all and re-add
          parent.ref.innerHTML = ""
          newChildren.foreach(child => parent.ref.appendChild(child.ref))
        }
      }
    }
    ```

### Phase 6: Events (Week 7-8)

**Goal: Handle user interactions**

17. **EventStream** (just callbacks for now)
    ```scala
    class EventStream[A] {
      private var listeners: List[A => Unit] = Nil

      def fire(value: A): Unit = listeners.foreach(_(value))

      def foreach(f: A => Unit): Unit = {
        listeners = f :: listeners
      }
    }
    ```

18. **Event listeners** - `onClick --> observer`
    ```scala
    def onClick: EventStream[MouseEvent] = {
      val stream = new EventStream[MouseEvent]
      element.ref.addEventListener("click", e => stream.fire(e))
      stream
    }

    // Usage
    button(
      onClick.foreach(e => println("Clicked!")),
      "Click me"
    )
    ```

### Phase 7: Advanced Reactivity (Week 8-12)

19. **Signal** - like Var but read-only + operators
20. **map/filter operators**
21. **EventBus** - manual event emitter
22. **Ownership system** - proper memory management
23. **Transaction system** - batched updates
24. **Efficient children diffing** - minimize DOM operations

### Phase 8: Complete API (Week 12-14)

25. **All HTML tags and attributes**
26. **All style properties**
27. **SVG support**
28. **Comprehensive testing**

---

## Why This Order Works

### ✅ Advantages of Static-First Approach:

1. **Immediate gratification** - See results in DOM right away
2. **Incremental complexity** - Add one feature at a time
3. **Easier debugging** - No hidden reactivity issues
4. **Natural progression** - Each step builds on previous
5. **Working app at each stage** - Always have something runnable

### 🎯 Key Insight:

You suggested starting with:
1. Static DOM + children ✅
2. Text modifier ✅
3. Style modifiers (color, backgroundColor) ✅

**This is exactly right!** This order lets you:
- Build muscle memory with the patterns
- Test thoroughly at each layer
- Understand the "why" before the "how"
- Avoid premature optimization

### Example progression:

**Week 1:** Static tree
```scala
div(h1("Title"), p("Text"))
```

**Week 2:** Static styles
```scala
div(
  backgroundColor := "blue",
  h1(color := "white", "Title")
)
```

**Week 3:** Dynamic text
```scala
val text = Var("Hello")
div(h1(child.text <-- text))
```

**Week 4:** Dynamic styles
```scala
val color = Var("red")
div(h1(color <-- color, "Title"))
```

**Week 5:** Dynamic children
```scala
val items = Var(List(div("A"), div("B")))
div(children <-- items)
```

Each week, you have a **working, testable system**!

---

## Key Concepts to Master

### 1. Laziness
- Observables only work when observed
- Start on first observer, stop on last removal
- Enables memory efficiency and GC

### 2. Ownership
- Every subscription needs an owner
- Owner lifecycle determines subscription lifecycle
- Prevents memory leaks from forgotten subscriptions

### 3. Dynamic Ownership
- Elements have DynamicOwners
- Activate when mounted, deactivate when unmounted
- Subscriptions automatically start/stop with DOM lifecycle

### 4. Transactions
- Batch updates together
- Topological sorting ensures correct order
- Each observer fires exactly once per transaction

### 5. Glitch Freedom
- No intermediate states observed
- Changes propagate in dependency order
- Rank system ensures proper execution order

---

## Testing Strategy

### Test pyramid:
1. **Unit tests** - Each operator individually
2. **Integration tests** - Observable chains
3. **DOM tests** - Use jsdom for element behavior
4. **E2E tests** - Real browser with applications

### Critical test areas:
- Memory leaks (subscription cleanup)
- Glitch freedom (no duplicate/missed events)
- Dynamic children (efficient DOM updates)
- Error handling and recovery
- Edge cases (empty lists, null values, etc.)

---

## Common Pitfalls to Avoid

1. **Memory leaks** - Always use owners, never forget to kill subscriptions
2. **Creating transactions in onWillStart** - This hook must be pure
3. **Removing observers during iteration** - Use pending removals
4. **Calling onStart/onStop directly** - Use proper lifecycle methods
5. **Forgetting topological ranks** - Required for transaction ordering
6. **Mutation during propagation** - Can cause inconsistent state

---

## Learning Resources

### Code to read first:
1. [DomApi.scala](laminar/src/io/github/nguyenyou/laminar/DomApi.scala) - Pure functions, easy to understand
2. [Owner.scala](airstream/src/io/github/nguyenyou/airstream/ownership/Owner.scala) - Simple ownership model
3. [RootNode.scala](laminar/src/io/github/nguyenyou/laminar/nodes/RootNode.scala) - Complete mounting flow
4. [EventStream.scala](airstream/src/io/github/nguyenyou/airstream/core/EventStream.scala) - Stream operations

### Code to read later (more complex):
1. [BaseObservable.scala](airstream/src/io/github/nguyenyou/airstream/core/BaseObservable.scala) - Observer management
2. [ReactiveElement.scala](laminar/src/io/github/nguyenyou/laminar/nodes/ReactiveElement.scala) - Element lifecycle
3. [ChildrenInserter.scala](laminar/src/io/github/nguyenyou/laminar/inserters/ChildrenInserter.scala) - Diffing algorithm
4. [DynamicOwner.scala](airstream/src/io/github/nguyenyou/airstream/ownership/DynamicOwner.scala) - Activation logic

### Official docs:
- Laminar docs: https://laminar.dev
- Airstream docs: https://github.com/raquo/Airstream

---

## Minimal Working Example

Here's what a minimal implementation should achieve:

```scala
import io.github.nguyenyou.laminar.api.L._

val countVar = Var(0)

val app = div(
  h1("Counter"),
  p(
    "Count: ",
    child.text <-- countVar.signal.map(_.toString)
  ),
  button(
    "Increment",
    onClick.mapTo(1) --> countVar.updater[Int](_ + _)
  )
)

render(dom.document.getElementById("app"), app)
```

**What this requires:**
- ✅ Var (writable signal)
- ✅ Signal with map
- ✅ EventStream from onClick
- ✅ Child text inserter (`child.text <--`)
- ✅ Observer (`-->`)
- ✅ Element creation (div, h1, p, button)
- ✅ Render function with mounting

---

## Progressive Complexity

### Level 1: Static UI
```scala
div(className := "container", "Hello World")
```
*Needs: Tags, attributes, static modifiers*

### Level 2: Simple Reactivity
```scala
div(child.text <-- signal)
```
*Needs: Signals, receivers, dynamic owner*

### Level 3: Events
```scala
button(onClick --> observer)
```
*Needs: Event streams, listeners*

### Level 4: Two-way Binding
```scala
input(value <-- var.signal, onInput.mapToValue --> var.writer)
```
*Needs: Vars, event value extraction*

### Level 5: Dynamic Children
```scala
div(children <-- itemsSignal.split(_.id)(renderItem))
```
*Needs: Children inserter, split operator*

---

## Estimated Timeline

**Total: ~14 weeks for full implementation**

- **Weeks 1-4**: Airstream core (50% of total work)
- **Weeks 5-8**: Laminar foundation (30% of total work)
- **Weeks 9-14**: Advanced features + polish (20% of total work)

Each phase should include:
- Implementation
- Unit tests
- Documentation
- Small example app

---

## Success Criteria

You've successfully rebuilt Laminar when you can:

1. ✅ Run the official Laminar examples
2. ✅ Pass all original test suites
3. ✅ Build a complex app (TodoMVC, etc.)
4. ✅ No memory leaks in long-running apps
5. ✅ Understand every line of code you wrote

---

## Final Advice

- **Start small**: Get one feature working end-to-end before adding more
- **Test early**: Write tests as you go, not at the end
- **Read the source**: This codebase is well-structured, learn from it
- **Draw diagrams**: Visualize ownership graphs and event flows
- **Use the REPL**: Experiment with small pieces interactively
- **Ask why**: Understand the reasoning behind each design decision

Good luck! 🚀