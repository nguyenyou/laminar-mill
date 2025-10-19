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
  
  val userFuture = fetchUser(42)  // ExecutionContext passed automatically!
  val result = Await.result(userFuture, 1.second)
  println(result)
  // Output: User #42


// ============================================================
// EXAMPLE 5: How Airstream/Laminar uses it
// ============================================================

// Simplified version of how Airstream's ownership works

trait Owner {
  def own(subscription: Subscription): Unit
}

class Subscription(owner: Owner, cleanup: () => Unit) {
  owner.own(this)  // Register with owner
  
  def kill(): Unit = cleanup()
}

class Observable[A] {
  def addObserver(onNext: A => Unit)(using owner: Owner): Subscription =
    new Subscription(owner, () => println("Cleaning up observer"))
}

// In Laminar, each DOM element provides an implicit Owner
class Element {
  given elementOwner: Owner = new Owner {
    def own(subscription: Subscription): Unit =
      println("Element owns subscription")
  }
  
  def bind(observable: Observable[Int]): Unit =
    // The `using elementOwner` is passed implicitly!
    observable.addObserver(value => println(s"Received: $value"))
}

@main def example5(): Unit =
  val element = new Element
  val observable = new Observable[Int]
  element.bind(observable)
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

