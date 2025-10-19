// Minimal Example: Understanding Scala 3's `using` clauses

// ============================================================
// EXAMPLE 1: Basic using clause
// ============================================================

// Step 1: Define a type that we want to pass implicitly
case class Config(apiUrl: String, timeout: Int)

// Step 2: Define a "given" instance - this is the canonical value
given defaultConfig: Config = Config("https://api.example.com", 30)

// Step 3: Define a function that takes a "using" parameter
def makeRequest(endpoint: String)(using config: Config): String =
  s"Calling ${config.apiUrl}/$endpoint with timeout ${config.timeout}s"

// Step 4: Call the function WITHOUT explicitly passing the config
@main def example1(): Unit =
  // The compiler automatically finds and passes `defaultConfig`
  println(makeRequest("users"))
  // Output: Calling https://api.example.com/users with timeout 30s

  // You CAN still pass it explicitly if needed
  val customConfig = Config("https://test.api.com", 60)
  println(makeRequest("users")(using customConfig))
  // Output: Calling https://test.api.com/users with timeout 60s

// ============================================================
// EXAMPLE 2: Anonymous using parameters (no name needed)
// ============================================================

// When you don't need to reference the parameter by name,
// you can omit the name and just specify the type

def logMessage(msg: String)(using Config): Unit =
  // Access the implicit config using `summon`
  val config = summon[Config]
  println(s"[${config.apiUrl}] $msg")

@main def example2(): Unit =
  logMessage("Hello, World!")
  // Output: [https://api.example.com] Hello, World!

// ============================================================
// EXAMPLE 3: Multiple using clauses
// ============================================================

case class User(name: String)
case class Database(url: String)

given currentUser: User = User("Alice")
given mainDb: Database = Database("postgres://localhost:5432")

def saveData(data: String)(using user: User)(using db: Database): String =
  s"User ${user.name} saving '$data' to ${db.url}"

@main def example3(): Unit =
  println(saveData("important data"))
  // Output: User Alice saving 'important data' to postgres://localhost:5432

// ============================================================
// EXAMPLE 4: Real-world example - Execution Context
// ============================================================

import scala.concurrent.{Future, ExecutionContext}

// This is how Scala's Future works!
// It needs an ExecutionContext to run async tasks

given ec: ExecutionContext = ExecutionContext.global

def fetchUser(id: Int)(using ExecutionContext): Future[String] =
  Future {
    Thread.sleep(100)
    s"User #$id"
  }

@main def example4(): Unit =
  import scala.concurrent.Await
  import scala.concurrent.duration._

  val userFuture = fetchUser(42) // ExecutionContext passed automatically!
  val result = Await.result(userFuture, 1.second)
  println(result)
  // Output: User #42

// ============================================================
// EXAMPLE 5: Realistic Laminar/Airstream Pattern
// ============================================================

// This example closely mimics how Laminar actually works!
// Pattern: div(onClick --> Observer { _ => println("clicked") })

// Step 1: Define the ownership system (like Airstream)
trait Owner {
  def own(subscription: Subscription): Unit
  def killSubscriptions(): Unit
}

class Subscription(owner: Owner, cleanup: () => Unit) {
  println(s"  [Subscription] Created with owner: ${owner.hashCode()}")
  owner.own(this) // Register with owner immediately!

  def kill(): Unit = {
    println(s"  [Subscription] Killed, running cleanup")
    cleanup()
  }
}

// Step 2: Define Observable (like Airstream's Observable)
class Observable[A] {
  private var observers: List[A => Unit] = List.empty

  // This is the key method - it takes an implicit/using Owner!
  def addObserver(onNext: A => Unit)(using owner: Owner): Subscription = {
    println(
      s"  [Observable] addObserver called with owner: ${owner.hashCode()}"
    )
    observers = observers :+ onNext
    new Subscription(
      owner,
      cleanup = () => {
        observers = observers.filterNot(_ == onNext)
        println(s"    [Observable] Observer removed")
      }
    )
  }

  def emit(value: A): Unit = {
    println(s"  [Observable] Emitting: $value")
    observers.foreach(_(value))
  }
}

// Step 3: Define EventProp (like Laminar's onClick)
class EventProp[A](val name: String) {
  println(s"[EventProp] Created: $name")
}

// Step 4: Define Binder (like Laminar's Binder)
// A Binder is a function: Element => Unit
trait Binder {
  def apply(element: Element): Unit
}

object Binder {
  // Factory method that creates a Binder from a function
  def apply(bindFn: Element => Unit): Binder = new Binder {
    def apply(element: Element): Unit = bindFn(element)
  }
}

// Step 5: Define the --> operator (like Laminar's RichSource)
// This is an extension method on Observable
extension [A](observable: Observable[A]) {
  def -->(onNext: A => Unit): Binder = {
    println(s"[-->] Creating Binder for observable")

    // The --> operator returns a Binder that will be applied to an element
    Binder { element =>
      println(s"[Binder] Applied to element: ${element.hashCode()}")

      // HERE'S THE MAGIC: Access the element's given owner!
      // The element provides a `given owner: Owner` in its scope
      element.bindObservable(observable, onNext)
    }
  }
}

// Step 6: Define Element (like Laminar's ReactiveElement)
class Element(val tagName: String) {
  println(s"[Element] Created: <$tagName>")

  // Each element has its own Owner (in Laminar, this is a DynamicOwner)
  // This is the SOURCE of the implicit owner!
  given elementOwner: Owner = new Owner {
    private var subscriptions: List[Subscription] = List.empty

    def own(subscription: Subscription): Unit = {
      println(s"    [Owner] Element <$tagName> owns subscription")
      subscriptions = subscriptions :+ subscription
    }

    def killSubscriptions(): Unit = {
      println(s"    [Owner] Killing all subscriptions for <$tagName>")
      subscriptions.foreach(_.kill())
      subscriptions = List.empty
    }
  }

  // This method is called by the Binder
  // It uses the element's given owner implicitly!
  def bindObservable[A](observable: Observable[A], onNext: A => Unit): Unit = {
    println(s"  [Element] bindObservable called")
    // The `using elementOwner` is passed implicitly here!
    // We could write it explicitly as: observable.addObserver(onNext)(using elementOwner)
    // But we can omit it because elementOwner is a `given` in this scope
    observable.addObserver(onNext)
  }

  // Apply modifiers (Binders) to this element
  def apply(modifiers: Binder*): Element = {
    println(s"[Element] Applying ${modifiers.length} modifier(s) to <$tagName>")
    modifiers.foreach(_(this))
    this
  }

  // Simulate unmounting (killing subscriptions)
  def unmount(): Unit = {
    println(s"[Element] Unmounting <$tagName>")
    elementOwner.killSubscriptions()
  }
}

// Step 7: Define the div() constructor (like Laminar's div)
def div(modifiers: Binder*): Element = {
  val element = new Element("div")
  element.apply(modifiers*)
}

// Step 8: Simulate onClick event prop
val onClick: EventProp[String] = new EventProp("click")

// Step 9: Create an Observable that simulates click events
val clickObservable: Observable[String] = new Observable[String]

// Step 10: Define Observer helper (like Airstream's Observer)
object Observer {
  def apply[A](onNext: A => Unit): A => Unit = onNext
}

@main def example5(): Unit = {
  println("\n" + "=" * 60)
  println("EXAMPLE 5: Realistic Laminar Pattern")
  println("=" * 60 + "\n")

  println("--- Creating element with onClick --> Observer ---\n")

  // THIS IS THE ACTUAL LAMINAR PATTERN!
  val myDiv = div(
    clickObservable --> Observer { event =>
      println(s"    [Observer] Click event received: $event")
    }
  )

  println("\n--- Simulating click events ---\n")
  clickObservable.emit("Click 1")
  clickObservable.emit("Click 2")

  println("\n--- Unmounting element (cleanup) ---\n")
  myDiv.unmount()

  println("\n--- Trying to emit after unmount (no observers) ---\n")
  clickObservable.emit("Click 3 (should not be received)")

  println("\n" + "=" * 60)
  println("FLOW EXPLANATION:")
  println("=" * 60)
  println("""
1. div() creates an Element with a `given elementOwner: Owner`
2. clickObservable --> Observer {...} creates a Binder
3. The Binder is applied to the element via div(binder)
4. Binder calls element.bindObservable(observable, onNext)
5. bindObservable calls observable.addObserver(onNext)
6. addObserver has `(using owner: Owner)` parameter
7. Compiler finds the `given elementOwner` from the Element
8. Subscription is created with that owner
9. Owner.own(subscription) registers the subscription
10. When element.unmount() is called, all subscriptions are killed
  """)
}
// Output: Element owns subscription

// ============================================================
// KEY CONCEPTS SUMMARY
// ============================================================

/*
1. `given` - Defines a value that can be passed implicitly

   given myConfig: Config = Config("url", 30)

2. `using` - Declares a parameter that receives implicit values

   def foo(x: Int)(using config: Config): Unit = ???

3. Anonymous using - When you don't need the parameter name

   def foo(x: Int)(using Config): Unit =
     val config = summon[Config]  // Get it with summon

4. Explicit passing - You can always pass explicitly with `using`

   foo(42)(using customConfig)

5. The compiler matches by TYPE, not by name
   - If there's exactly one `given Config` in scope, it's used
   - If there are multiple, you get a compilation error
   - If there are none, you get a compilation error

6. Scope rules:
   - `given` instances must be in scope (imported or defined locally)
   - Import with: `import MyObject.given` (imports all givens)
   - Or: `import MyObject.{given Config}` (imports specific type)
 */
