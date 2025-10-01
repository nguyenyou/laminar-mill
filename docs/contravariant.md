Step 1: A tiny element hierarchy

```scala 3
trait Element
class Div extends Element
class Span extends Element
```

So Div <: Element and Span <: Element.

Step 2: A contravariant Modifier

```scala 3
trait Modifier[-El] {
  def apply(el: El): Unit
}
```

Notice the -El (contravariant).

Step 3: Concrete modifiers

```scala 3
// This modifier can handle ANY Element
val logElement: Modifier[Element] = (el: Element) =>
  println(s"Logging element: ${el.getClass.getSimpleName}")

// This modifier only works for Div
val divOnly: Modifier[Div] = (div: Div) =>
  println(s"Div-specific modifier: ${div.getClass.getSimpleName}")
```

Step 4: How contravariance works

```scala 3
val div = new Div
val span = new Span

// Because Modifier is contravariant, 
// a Modifier[Element] can be used as Modifier[Div]
def useDivModifier(mod: Modifier[Div]): Unit =
  mod(new Div)

// Works: logElement is Modifier[Element], but thanks to contravariance
// it can be passed where Modifier[Div] is required
useDivModifier(logElement)

// Doesn't work the other way around:
// divOnly: Modifier[Div] cannot be used as Modifier[Element]
// useElementModifier(divOnly) ❌ compile error
```

Why?
•	Contravariance (-El) means:
If you can handle a more general type, you can also handle a more specific one.
•	Modifier[Element] can handle any Element.
•	A Div is an Element, so it’s safe to pass a Modifier[Element] where a Modifier[Div] is required.
•	But the reverse isn’t true:
Modifier[Div] only knows how to handle Div. If you give it a Span, it will fail — so the compiler doesn’t allow Modifier[Div] → Modifier[Element].

👉 In short:
•	Covariant (+T): You can produce more specific things safely.
•	Contravariant (-T): You can consume more general things safely.
