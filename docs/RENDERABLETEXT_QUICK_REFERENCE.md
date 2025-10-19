# RenderableText Quick Reference

Quick reference for understanding how `RenderableText[A]` works in Laminar.

---

## The Implicit Conversion

```scala
implicit def textToTextNode[A](value: A)(implicit r: RenderableText[A]): TextNode = {
  new TextNode(r.asString(value))
}
```

**File:** `laminar/src/io/github/nguyenyou/laminar/api/Implicits.scala:46-48`

---

## Built-in RenderableText Instances

| Type | Instance Name | Conversion Logic | File Location |
|------|---------------|------------------|---------------|
| `String` | `stringRenderable` | `identity` (no change) | RenderableText.scala:32 |
| `Int` | `intRenderable` | `_.toString` | RenderableText.scala:34 |
| `Double` | `doubleRenderable` | `_.toString` | RenderableText.scala:36 |
| `Boolean` | `boolRenderable` | `_.toString` | RenderableText.scala:38 |
| `TextNode` | `textNodeRenderable` | `_.text` | RenderableText.scala:45 |
| `Char` | `charRenderable` (lazy) | `_.toString` | RenderableText.scala:49 |
| `Byte` | `byteRenderable` (lazy) | `_.toString` | RenderableText.scala:51 |
| `Short` | `shortRenderable` (lazy) | `_.toString` | RenderableText.scala:53 |
| `Long` | `longRenderable` (lazy) | `_.toString` | RenderableText.scala:55 |
| `Float` | `floatRenderable` (lazy) | `_.toString` | RenderableText.scala:57 |

**All instances are defined in:** `laminar/src/io/github/nguyenyou/laminar/modifiers/RenderableText.scala`

---

## Usage Examples

### Basic Usage

```scala
div("hello world")  // String → TextNode
div(42)             // Int → TextNode
div(3.14)           // Double → TextNode
div(true)           // Boolean → TextNode
div('A')            // Char → TextNode
```

### With Reactive Content

```scala
val nameSignal: Signal[String] = ???
val countSignal: Signal[Int] = ???

div(
  child.text <-- nameSignal,   // Uses RenderableText[String]
  child.text <-- countSignal   // Uses RenderableText[Int]
)
```

### Custom Type

```scala
case class Money(amount: Double, currency: String)

// Define custom RenderableText instance
implicit val moneyRenderable: RenderableText[Money] = RenderableText { money =>
  f"${money.currency} ${money.amount}%.2f"
}

// Now you can use Money in div()
div(Money(42.50, "USD"))  // Renders as "USD 42.50"
```

### Override Built-in Instance

```scala
object CustomImplicits {
  // Override default Int rendering
  implicit val intRenderable: RenderableText[Int] = RenderableText("%04d".format(_))
  
  // Override default Boolean rendering
  implicit val boolRenderable: RenderableText[Boolean] = RenderableText(_.toString.toUpperCase())
}

import CustomImplicits._

div(
  1,      // Renders as "0001"
  true    // Renders as "TRUE"
)
```

---

## The RenderableText Trait

```scala
trait RenderableText[-TextLike] {
  def asString(value: TextLike): String = ""
}
```

**Key features:**
- **Contravariant** (`-TextLike`): More general instances can be used for specific types
- **Single method**: `asString(value: TextLike): String`
- **Default implementation**: Empty string (prevents SAM trait optimization)

---

## Creating Custom Instances

### Method 1: Using the Helper

```scala
implicit val myTypeRenderable: RenderableText[MyType] = RenderableText(_.toString)
```

### Method 2: Anonymous Class

```scala
implicit val myTypeRenderable: RenderableText[MyType] = new RenderableText[MyType] {
  override def asString(value: MyType): String = value.customToString()
}
```

### Method 3: Extending the Trait

```scala
implicit object MyTypeRenderable extends RenderableText[MyType] {
  override def asString(value: MyType): String = value.customToString()
}
```

---

## Implicit Resolution Order

When the compiler looks for `RenderableText[A]`:

1. **Local scope** - Current block, method parameters
2. **Enclosing scope** - Enclosing class, object, package
3. **Imported implicits** - Explicitly imported implicits
4. **Companion objects** - `RenderableText` companion object ← **Most common**
5. **Companion objects of type parameters** - Companion of `A`, etc.

---

## Common Patterns

### Pattern 1: Simple Value Rendering

```scala
div("Static text")
div(42)
div(3.14)
```

### Pattern 2: Reactive Value Rendering

```scala
val signal: Signal[String] = ???
div(child.text <-- signal)
```

### Pattern 3: Conditional Text

```scala
val showText: Signal[Boolean] = ???
div(text("Hello") <-- showText)
```

### Pattern 4: Custom Type Rendering

```scala
case class User(name: String, age: Int)

implicit val userRenderable: RenderableText[User] = RenderableText { user =>
  s"${user.name} (${user.age})"
}

div(User("Alice", 30))  // Renders as "Alice (30)"
```

### Pattern 5: Scoped Custom Instances

```scala
object MyImplicits {
  implicit val customIntRenderable: RenderableText[Int] = RenderableText("%04d".format(_))
}

// Use in specific scope
{
  import MyImplicits._
  div(42)  // Renders as "0042"
}

// Outside scope, uses default
div(42)  // Renders as "42"
```

---

## Where RenderableText is Used

| Location | Purpose | File |
|----------|---------|------|
| `textToTextNode` | Convert values to TextNode | Implicits.scala:46-48 |
| `textToInserter` | Convert values to StaticInserter | Implicits.scala:169-175 |
| `ChildTextReceiver.apply` | Lock text value | ChildTextReceiver.scala:18-22 |
| `ChildTextReceiver.<--` | Reactive text updates | ChildTextReceiver.scala:28-38 |
| `ChildTextInserter.apply` | Dynamic text insertion | ChildTextInserter.scala:11-14 |

---

## Type Hierarchy

```
Value (e.g., "hello world")
  ↓ (implicit conversion via textToTextNode)
TextNode
  ↓ (extends)
ChildNode
  ↓ (extends)
Modifier[ReactiveElement[dom.Element]]
  ↓ (accepted by)
div(modifiers: Modifier*)
```

---

## Compilation Flow

```
USER CODE:
  div("hello world")

COMPILER:
  1. Type check: div expects Modifier*, String is not Modifier
  2. Implicit search: Find String => Modifier
  3. Found: textToTextNode[A](value: A)(implicit r: RenderableText[A])
  4. Type inference: A = String
  5. Implicit parameter search: Find RenderableText[String]
  6. Found: RenderableText.stringRenderable
  7. Apply: textToTextNode[String]("hello world")(stringRenderable)
  8. Execute: new TextNode(stringRenderable.asString("hello world"))
  9. Result: TextNode wrapping dom.Text("hello world")
```

---

## Error Messages

### Missing Implicit

```scala
case class Person(name: String)
div(Person("Alice"))  // Compile error!
```

**Error:**
```
Implicit instance of RenderableText[Person] not found. If you want to render `Person` as a string, 
define an implicit `RenderableText[Person]` instance for it.
```

**Fix:**
```scala
implicit val personRenderable: RenderableText[Person] = RenderableText(_.name)
div(Person("Alice"))  // Now works!
```

---

## Performance Tips

1. **Eager vs Lazy**: Common types (String, Int, Double, Boolean) are eager, less common are lazy
2. **Avoid Complex Logic**: Keep `asString` simple for better performance
3. **Reuse Instances**: Define instances once, reuse across codebase
4. **Avoid Runtime Checks**: Use compile-time type safety instead

---

## Debugging

### Check if Implicit is Found

```scala
// This will compile only if RenderableText[MyType] exists
implicitly[RenderableText[MyType]]

// Or in Scala 3
summon[RenderableText[MyType]]
```

### Print Implicit Value

```scala
val r = implicitly[RenderableText[String]]
println(r.asString("test"))  // Prints: test
```

### Check Conversion Result

```scala
val textNode = textToTextNode("hello")
println(textNode.text)  // Prints: hello
```

---

## Comparison with Other Approaches

### Without RenderableText (Hypothetical)

```scala
// Would need separate conversions for each type
implicit def stringToTextNode(value: String): TextNode = new TextNode(value)
implicit def intToTextNode(value: Int): TextNode = new TextNode(value.toString)
implicit def doubleToTextNode(value: Double): TextNode = new TextNode(value.toString)
// ... and so on
```

**Problems:**
- Code duplication
- Not extensible for custom types
- No way to customize conversion logic

### With RenderableText (Actual)

```scala
// Single conversion function
implicit def textToTextNode[A](value: A)(implicit r: RenderableText[A]): TextNode = {
  new TextNode(r.asString(value))
}

// Extensible instances
implicit val stringRenderable: RenderableText[String] = RenderableText(identity)
implicit val intRenderable: RenderableText[Int] = RenderableText(_.toString)
// ... users can add their own
```

**Benefits:**
- Single conversion function
- Extensible for custom types
- Customizable conversion logic
- Type safe

---

## Advanced: Contravariance

```scala
trait RenderableText[-TextLike] {  // Note the `-`
  def asString(value: TextLike): String
}
```

**What it means:**
- If `Dog <: Animal`, then `RenderableText[Animal] <: RenderableText[Dog]`
- More general instances can be used for specific types

**Example:**

```scala
trait Animal
class Dog extends Animal

implicit val animalRenderable: RenderableText[Animal] = RenderableText(_.toString)

// RenderableText[Animal] can be used for Dog
div(new Dog())  // Uses animalRenderable
```

---

## Related Concepts

### RenderableNode

Similar pattern for rendering custom components as nodes:

```scala
trait RenderableNode[-Component] {
  def asNode(value: Component): ChildNode.Base
}
```

**Usage:**
```scala
trait MyComponent {
  val node: ChildNode.Base
}

implicit val componentRenderable: RenderableNode[MyComponent] = RenderableNode(_.node)

div(myComponent)  // Renders the component's node
```

### Modifier Trait

The target type for implicit conversions:

```scala
trait Modifier[-El <: ReactiveElement.Base] {
  def apply(element: El): Unit
}
```

**TextNode extends Modifier**, so it can be used in `div(...)`

---

## Best Practices

1. **Define instances in companion objects** - Automatic implicit resolution
2. **Use descriptive names** - `moneyRenderable`, not `mr`
3. **Keep conversion logic simple** - Avoid side effects
4. **Document custom instances** - Explain conversion logic
5. **Test custom instances** - Ensure correct rendering
6. **Avoid ambiguous implicits** - Only one instance per type in scope
7. **Use type aliases** - For complex types

---

## Common Mistakes

### Mistake 1: Multiple Instances in Scope

```scala
implicit val r1: RenderableText[Int] = RenderableText(_.toString)
implicit val r2: RenderableText[Int] = RenderableText("%04d".format(_))

div(42)  // Compile error: ambiguous implicit values
```

**Fix:** Only have one instance in scope

### Mistake 2: Forgetting to Import

```scala
object MyImplicits {
  implicit val myRenderable: RenderableText[MyType] = ???
}

div(myValue)  // Compile error: implicit not found
```

**Fix:** Import the implicit
```scala
import MyImplicits._
div(myValue)  // Now works
```

### Mistake 3: Side Effects in asString

```scala
// BAD: Side effects
implicit val badRenderable: RenderableText[MyType] = RenderableText { value =>
  println("Converting!")  // Side effect!
  value.toString
}
```

**Fix:** Keep `asString` pure (no side effects)

---

## Further Reading

- [IMPLICIT_RENDERABLETEXT_EXPLAINED.md](IMPLICIT_RENDERABLETEXT_EXPLAINED.md) - Complete detailed explanation
- [COMPLETE_RENDERING_FLOW.md](COMPLETE_RENDERING_FLOW.md) - How rendering works
- [element-nesting-explained.md](element-nesting-explained.md) - How elements nest

---

**Last Updated:** 2025-10-19

