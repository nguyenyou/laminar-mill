# How RenderableText Implicit Parameter Works

This document provides a comprehensive explanation of how the implicit parameter `r: RenderableText[A]` works in Laminar's text node conversion.

---

## The Code in Question

```scala
implicit def textToTextNode[A](value: A)(implicit r: RenderableText[A]): TextNode = {
  new TextNode(r.asString(value))
}
```

**File:** `laminar/src/io/github/nguyenyou/laminar/api/Implicits.scala:46-48`

---

## 1. How Implicit Resolution Works

### **When is this implicit conversion triggered?**

When you write:

```scala
div("hello world")
```

The Scala compiler goes through these steps:

1. **Type checking**: `div(...)` expects `Modifier[ReactiveHtmlElement[Ref]]*` (varargs)
2. **Type mismatch**: `"hello world"` is a `String`, not a `Modifier`
3. **Implicit search**: Compiler looks for an implicit conversion from `String` to `Modifier`
4. **Finds `textToTextNode`**: This converts `A` to `TextNode`, and `TextNode` extends `Modifier`
5. **Type parameter inference**: Compiler infers `A = String`
6. **Implicit parameter search**: Compiler looks for an implicit `RenderableText[String]`
7. **Finds `stringRenderable`**: In `RenderableText` companion object
8. **Conversion applied**: `textToTextNode("hello world")(RenderableText.stringRenderable)`

### **How does the compiler find the implicit `RenderableText[A]`?**

The compiler searches for implicits in this order:

1. **Local scope**: Current block, method parameters
2. **Enclosing scope**: Enclosing class, object, package
3. **Imported implicits**: Explicitly imported implicits
4. **Companion objects**: Companion object of the type (`RenderableText` companion object)
5. **Companion objects of type parameters**: Companion objects of `A`, `RenderableText[A]`, etc.

For `RenderableText[String]`, the compiler finds `stringRenderable` in the `RenderableText` companion object (step 4).

---

## 2. RenderableText Instances Defined in the Codebase

### **File:** `laminar/src/io/github/nguyenyou/laminar/modifiers/RenderableText.scala`

All implicit instances are defined in the `RenderableText` companion object:

#### **Eager Instances** (lines 32-38)

```scala
implicit val stringRenderable: RenderableText[String] = RenderableText[String](identity)

implicit val intRenderable: RenderableText[Int] = RenderableText[Int](_.toString)

implicit val doubleRenderable: RenderableText[Double] = RenderableText[Double](_.toString)

implicit val boolRenderable: RenderableText[Boolean] = RenderableText[Boolean](_.toString)
```

#### **Special Instance** (line 45)

```scala
/** #Warning: Using this naively in ChildTextInserter is not efficient.
  *  When we encounter this renderable instance, we use ChildInserter instead.
  */
implicit val textNodeRenderable: RenderableText[TextNode] = RenderableText[TextNode](_.text)
```

#### **Lazy Instances** (lines 49-57)

```scala
implicit lazy val charRenderable: RenderableText[Char] = RenderableText[Char](_.toString)

implicit lazy val byteRenderable: RenderableText[Byte] = RenderableText[Byte](_.toString)

implicit lazy val shortRenderable: RenderableText[Short] = RenderableText[Short](_.toString)

implicit lazy val longRenderable: RenderableText[Long] = RenderableText[Long](_.toString)

implicit lazy val floatRenderable: RenderableText[Float] = RenderableText[Float](_.toString)
```

### **Why are some lazy?**

- **Eager instances** (String, Int, Double, Boolean): Most commonly used, initialized immediately
- **Lazy instances** (Char, Byte, Short, Long, Float): Less commonly used, initialized only when needed
- This is a performance optimization to reduce startup time

---

## 3. The RenderableText Trait Definition

### **File:** `laminar/src/io/github/nguyenyou/laminar/modifiers/RenderableText.scala:7-22`

```scala
/** `RenderableText[A]` is evidence that you can convert a value of type A to
  * a string for the purpose of rendering it as a TextNode.
  *
  * If you have an implicit val of RenderableText[A], Laminar can render your
  * `A` type values by converting them to strings (and ultimately into
  * [[io.github.nguyenyou.laminar.nodes.TextNode]]), and will accept your values as
  * a valid [[Modifier]], and in `child.text <--`.
  *
  * See also – [[RenderableNode]]
  */
@implicitNotFound("Implicit instance of RenderableText[${TextLike}] not found. If you want to render `${TextLike}` as a string, define an implicit `RenderableText[${TextLike}]` instance for it.")
trait RenderableText[-TextLike] {

  // override this default value, it's only here to prevent this from qualifying as a SAM trait
  def asString(value: TextLike): String = ""
}
```

### **Key Features:**

1. **Type parameter**: `[-TextLike]` - Contravariant type parameter
2. **Single method**: `asString(value: TextLike): String` - Converts value to string
3. **@implicitNotFound**: Provides helpful error message when implicit is not found
4. **Not a SAM trait**: Default implementation prevents Single Abstract Method optimization

### **Companion Object Helper** (lines 26-28)

```scala
def apply[A](render: A => String): RenderableText[A] = new RenderableText[A] {
  override def asString(value: A): String = render(value)
}
```

This factory method makes it easy to create instances:

```scala
RenderableText[Int](_.toString)  // Instead of writing the full anonymous class
```

---

## 4. Complete Resolution Chain for `div("hello world")`

### **Step-by-Step Trace:**

```
USER CODE:
  div("hello world")

STEP 1: Type Checking
  div(...) expects: Modifier[ReactiveHtmlElement[Ref]]*
  "hello world" is: String
  Type mismatch!

STEP 2: Implicit Search
  Compiler looks for: String => Modifier[ReactiveHtmlElement[Ref]]
  Finds: textToTextNode[A](value: A)(implicit r: RenderableText[A]): TextNode
  TextNode extends ChildNode extends Modifier ✓

STEP 3: Type Parameter Inference
  A = String (inferred from "hello world")
  Need: RenderableText[String]

STEP 4: Implicit Parameter Search
  Compiler looks for: implicit RenderableText[String]
  Searches in:
    1. Local scope - not found
    2. Enclosing scope - not found
    3. Imported implicits - not found
    4. RenderableText companion object - FOUND!
  
  Found: RenderableText.stringRenderable
  Location: laminar/modifiers/RenderableText.scala:32

STEP 5: Implicit Conversion Applied
  textToTextNode[String]("hello world")(RenderableText.stringRenderable)
  
STEP 6: Execute Conversion
  r = RenderableText.stringRenderable
  r.asString("hello world") = identity("hello world") = "hello world"
  new TextNode("hello world")

STEP 7: TextNode Creation
  TextNode constructor calls:
  DomApi.createTextNode("hello world")
  → document.createTextNode("hello world")
  → dom.Text node created

RESULT:
  TextNode wrapping dom.Text("hello world")
  TextNode extends Modifier, so it can be used in div(...)
```

### **What is the value of `r.asString("hello world")`?**

```scala
// stringRenderable is defined as:
implicit val stringRenderable: RenderableText[String] = RenderableText[String](identity)

// RenderableText[String](identity) expands to:
new RenderableText[String] {
  override def asString(value: String): String = identity(value)
}

// identity is the identity function: x => x
// So r.asString("hello world") = identity("hello world") = "hello world"
```

**Result:** `"hello world"` (unchanged)

---

## 5. All Related Usage Sites

### **5.1. Primary Usage: Implicit Conversion to TextNode**

**File:** `laminar/src/io/github/nguyenyou/laminar/api/Implicits.scala:46-48`

```scala
implicit def textToTextNode[A](value: A)(implicit r: RenderableText[A]): TextNode = {
  new TextNode(r.asString(value))
}
```

**Usage:**
```scala
div("hello")      // String → TextNode
div(42)           // Int → TextNode
div(3.14)         // Double → TextNode
div(true)         // Boolean → TextNode
```

---

### **5.2. Implicit Conversion to Inserter**

**File:** `laminar/src/io/github/nguyenyou/laminar/api/Implicits.scala:169-175`

```scala
implicit def textToInserter[TextLike](value: TextLike)(implicit r: RenderableText[TextLike]): StaticInserter = {
  if (r == RenderableText.textNodeRenderable) {
    StaticChildInserter.noHooks(value.asInstanceOf[TextNode])
  } else {
    new StaticTextInserter(r.asString(value))
  }
}
```

**Usage:** Used internally for inserters (advanced feature)

---

### **5.3. ChildTextReceiver**

**File:** `laminar/src/io/github/nguyenyou/laminar/receivers/ChildTextReceiver.scala:18-22`

```scala
def apply[TextLike](text: TextLike)(implicit renderable: RenderableText[TextLike]): LockedChildTextReceiver = {
  new LockedChildTextReceiver(renderable.asString(text))
}
```

**Usage:**
```scala
text("hello") <-- signalOfBoolean
text(42) <-- signalOfBoolean
```

---

### **5.4. ChildTextReceiver with Observables**

**File:** `laminar/src/io/github/nguyenyou/laminar/receivers/ChildTextReceiver.scala:28-38`

```scala
def <--[TextLike](textSource: Source[TextLike])(implicit renderable: RenderableText[TextLike]): DynamicInserter = {
  if (renderable == RenderableText.textNodeRenderable) {
    val nodes = textSource.toObservable.asInstanceOf[Observable[TextNode]]
    ChildInserter(nodes, RenderableNode.nodeRenderable, initialHooks = js.undefined)
  } else {
    ChildTextInserter(textSource.toObservable, renderable)
  }
}
```

**Usage:**
```scala
child.text <-- signalOfString
child.text <-- signalOfInt
child.text <-- signalOfDouble
```

---

### **5.5. ChildTextInserter**

**File:** `laminar/src/io/github/nguyenyou/laminar/inserters/ChildTextInserter.scala:11-14`

```scala
def apply[Component](
  textSource: Observable[Component],
  renderable: RenderableText[Component]
): DynamicInserter = {
  // ... uses renderable.asString(newValue) to convert values
}
```

**Usage:** Internal implementation for reactive text updates

---

### **5.6. Custom RenderableText Instances**

**File:** `laminar/test/src/com/raquo/laminar/tests/RenderableSpec.scala:14-20`

```scala
object TextNodeImplicits extends BaseTrait {
  implicit val intRenderableX: RenderableText[Int] = RenderableText("%04d".format(_))
}

trait BaseTrait {
  implicit val boolRenderable: RenderableText[Boolean] = RenderableText(_.toString.toUpperCase())
}
```

**Usage:**
```scala
import TextNodeImplicits._

div(
  1,      // Renders as "0001" (custom Int formatter)
  true,   // Renders as "TRUE" (custom Boolean formatter)
  2.0     // Renders as "2" (default Double formatter)
)
```

---

### **5.7. Custom Type Example**

**File:** `laminar/test/src/com/raquo/laminar/tests/ChildTextReceiverSpec.scala:174-176`

```scala
class TextLike(val str: String)

implicit val renderable: RenderableText[TextLike] = RenderableText(_.str)

div(
  text(new TextLike("hello")) <-- signalOfBoolean
)
```

---

## 6. Why Use Generic Type Parameter `A`?

### **Flexibility for Multiple Types**

Instead of:
```scala
implicit def stringToTextNode(value: String): TextNode = new TextNode(value)
implicit def intToTextNode(value: Int): TextNode = new TextNode(value.toString)
implicit def doubleToTextNode(value: Double): TextNode = new TextNode(value.toString)
// ... and so on for every type
```

We have:
```scala
implicit def textToTextNode[A](value: A)(implicit r: RenderableText[A]): TextNode = {
  new TextNode(r.asString(value))
}
```

### **Benefits:**

1. **Single conversion function**: One implicit conversion handles all types
2. **Extensibility**: Users can add support for custom types by defining `RenderableText[CustomType]`
3. **Type safety**: Compiler ensures `RenderableText[A]` exists for type `A`
4. **Separation of concerns**: Conversion logic is in `RenderableText`, not in the implicit conversion

### **Supported Types:**

- **Primitives**: String, Int, Double, Boolean, Char, Byte, Short, Long, Float
- **Laminar types**: TextNode (special case)
- **Custom types**: Any type with an implicit `RenderableText[T]` instance

### **Example with Custom Type:**

```scala
case class Money(amount: Double, currency: String)

implicit val moneyRenderable: RenderableText[Money] = RenderableText { money =>
  f"${money.currency} ${money.amount}%.2f"
}

div(Money(42.50, "USD"))  // Renders as "USD 42.50"
```

---

## 7. The Typeclass Pattern

`RenderableText[A]` is an example of the **typeclass pattern** in Scala:

### **What is a Typeclass?**

A typeclass is a type system construct that enables ad-hoc polymorphism. It allows you to add new behavior to existing types without modifying them.

### **Components:**

1. **Typeclass trait**: `RenderableText[A]` - defines the interface
2. **Typeclass instances**: `stringRenderable`, `intRenderable`, etc. - provide implementations
3. **Typeclass usage**: `textToTextNode[A](value: A)(implicit r: RenderableText[A])` - uses the typeclass

### **Advantages:**

- **Extensibility**: Add support for new types without modifying existing code
- **Type safety**: Compiler ensures instances exist
- **Separation of concerns**: Conversion logic is separate from the types
- **Retroactive extension**: Add behavior to types you don't own (e.g., `Int`, `String`)

---

## 8. Error Messages

### **Missing Implicit Error**

If you try to render a type without a `RenderableText` instance:

```scala
case class Person(name: String)

div(Person("Alice"))  // Compile error!
```

**Error message:**
```
Implicit instance of RenderableText[Person] not found. If you want to render `Person` as a string, 
define an implicit `RenderableText[Person]` instance for it.
```

This helpful error message comes from the `@implicitNotFound` annotation on the `RenderableText` trait.

### **Solution:**

```scala
implicit val personRenderable: RenderableText[Person] = RenderableText(_.name)

div(Person("Alice"))  // Now compiles! Renders as "Alice"
```

---

## 9. Contravariance Explained

### **Why is RenderableText contravariant?**

```scala
trait RenderableText[-TextLike] {  // Note the `-` before TextLike
  def asString(value: TextLike): String
}
```

**Contravariance** means: If `A` is a subtype of `B`, then `RenderableText[B]` is a subtype of `RenderableText[A]`.

### **Example:**

```scala
trait Animal
class Dog extends Animal

implicit val animalRenderable: RenderableText[Animal] = RenderableText(_.toString)

// Because RenderableText is contravariant:
// RenderableText[Animal] can be used as RenderableText[Dog]

div(new Dog())  // Uses animalRenderable
```

### **Why is this useful?**

- Allows more general instances to be used for more specific types
- Reduces the number of instances you need to define
- Follows the Liskov Substitution Principle

---

## 10. Summary

### **Key Takeaways:**

1. **Implicit conversion**: `textToTextNode` converts values to `TextNode` using `RenderableText`
2. **Typeclass pattern**: `RenderableText[A]` is a typeclass for converting `A` to `String`
3. **Implicit resolution**: Compiler finds `RenderableText[A]` in companion object
4. **Built-in support**: String, Int, Double, Boolean, Char, Byte, Short, Long, Float, TextNode
5. **Extensible**: Define `implicit RenderableText[CustomType]` for custom types
6. **Type safe**: Compiler ensures instances exist at compile time
7. **Single conversion**: One generic function handles all types

### **The Complete Flow:**

```
div("hello world")
  ↓ (type mismatch: String is not Modifier)
textToTextNode[String]("hello world")(implicit r: RenderableText[String])
  ↓ (compiler finds RenderableText.stringRenderable)
textToTextNode[String]("hello world")(RenderableText.stringRenderable)
  ↓ (execute conversion)
new TextNode(RenderableText.stringRenderable.asString("hello world"))
  ↓ (asString calls identity function)
new TextNode("hello world")
  ↓ (TextNode constructor)
DomApi.createTextNode("hello world")
  ↓ (browser API)
document.createTextNode("hello world")
  ↓ (result)
dom.Text node containing "hello world"
```

This is the **complete mechanism** that makes `div("hello world")` work in Laminar! 🎯

