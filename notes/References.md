┌─────────────────────────────────────────────────────────────┐
│                    REFERENCE FLOW                            │
└─────────────────────────────────────────────────────────────┘

Parent Observable                    Child Observable
┌──────────────┐                    ┌──────────────┐
│              │                    │              │
│  Parent      │◄───────────────────│  parent: P   │  (1) Child ALWAYS
│              │   Strong ref       │              │      holds parent
│              │   (permanent)      │              │
└──────────────┘                    └──────────────┘
       │                                    ▲
       │                                    │
       │ (2) Parent holds child             │
       │     ONLY when child                │
       │     is started (has                │
       │     observers)                     │
       │                                    │
       ▼                                    │
┌──────────────┐                           │
│ internal     │                           │
│ Observers:   │                           │
│ [child]      │───────────────────────────┘
│              │   Strong ref
└──────────────┘   (conditional - only when started)


LIFECYCLE:

1. Child created:
   - Child holds parent reference ✓
   - Parent does NOT hold child reference ✗

2. Child.addObserver() called:
   - Child calls parent.addInternalObserver(this)
   - Parent NOW holds child reference ✓

3. Child subscription killed:
   - Child calls parent.removeInternalObserver(this)
   - Parent removes child reference ✗
   - If child is unreachable, it can be GC'd


TIMELINE OF PARENT → CHILD REFERENCE:

Time 0: Child Created
┌────────┐                    ┌────────┐
│ Parent │                    │ Child  │
│        │                    │ parent ├──► Parent
│ []     │                    └────────┘
└────────┘
  ▲ internalObservers is EMPTY


Time 1: child.addObserver() called
┌────────┐                    ┌────────┐
│ Parent │                    │ Child  │
│        │                    │ parent ├──► Parent
│        │                    │        │
│        │                    │ onStart() called
│        │                    │   │
│        │◄───────────────────┤   │ parent.addInternalObserver(this)
│ [Child]│                    └───┘
└────────┘
  ▲ internalObservers NOW HAS CHILD


Time 2: subscription.kill() called
┌────────┐                    ┌────────┐
│ Parent │                    │ Child  │
│        │                    │ parent ├──► Parent
│        │                    │        │
│        │                    │ onStop() called
│        │                    │   │
│        │◄───────────────────┤   │ parent.removeInternalObserver(this)
│ []     │                    └───┘
└────────┘
  ▲ internalObservers EMPTY AGAIN - Child can be GC'd!

┌─────────────────────────────────────────────────────────────────┐
│  OWNERSHIP FLOW: From DOM Element to Subscription              │
└─────────────────────────────────────────────────────────────────┘

1. ELEMENT CREATION
   ┌──────────┐
   │   div    │
   │          │
   │ dynamic  │
   │ Owner    │◄─── Created with element
   └──────────┘


2. BINDER CREATION (onClick --> observer)
   ┌──────────────────────────────────────┐
   │ Binder                               │
   │   fn: element => DynamicSubscription │
   └──────────────────────────────────────┘


3. BINDER APPLIED TO ELEMENT
   div.amend(binder)
     ↓
   binder.apply(div)
     ↓
   binder.bind(div)
     ↓
   ReactiveElement.bindSink(div, observable)(observer)
     ↓
   DynamicSubscription.subscribeSink(div.dynamicOwner, observable, observer)
                                      ↑
                                      Element's dynamicOwner extracted!


4. DYNAMICSUBSCRIPTION CREATED
   ┌──────────────────────────────────────┐
   │ DynamicSubscription                  │
   │   dynamicOwner: div.dynamicOwner     │◄─── Holds reference to element's owner
   │   activate: Owner => Subscription    │◄─── Lambda that will create Subscription
   │   maybeCurrentSubscription: None     │     (not activated yet)
   └──────────────────────────────────────┘
                │
                │ registers itself
                ▼
   ┌──────────────────────────────────────┐
   │ div.dynamicOwner                     │
   │   subscriptions: [DynamicSub]        │
   │   maybeCurrentOwner: None            │     (not activated yet)
   └──────────────────────────────────────┘


5. ELEMENT MOUNTED TO DOM
   mount(div)
     ↓
   div.dynamicOwner.activate()
     ↓
   Creates OneTimeOwner
     ↓
   ┌──────────────────────────────────────┐
   │ OneTimeOwner (fresh!)                │
   │   subscriptions: []                  │
   └──────────────────────────────────────┘
     │
     │ passed to
     ▼
   DynamicSubscription.onActivate(oneTimeOwner)
     ↓
   activate(oneTimeOwner)  // Calls the lambda!
     ↓
   observable.addObserver(observer)(using oneTimeOwner)
                                    ↑
                                    OneTimeOwner passed as implicit!


6. SUBSCRIPTION CREATED
   new Subscription(oneTimeOwner, cleanup)
     ↓
   Subscription constructor calls: oneTimeOwner.own(this)
     ↓
   ┌──────────────────────────────────────┐
   │ OneTimeOwner                         │
   │   subscriptions: [Subscription]      │◄─── Subscription registered!
   └──────────────────────────────────────┘
     ▲
     │ owned by
     │
   ┌──────────────────────────────────────┐
   │ Subscription                         │
   │   owner: OneTimeOwner                │◄─── Holds reference to owner
   │   cleanup: () => remove observer     │
   └──────────────────────────────────────┘


FINAL STATE:
┌────────────────────────────────────────────────────────────┐
│ div                                                        │
│   dynamicOwner                                             │
│     maybeCurrentOwner: Some(OneTimeOwner)                  │
│     subscriptions: [DynamicSubscription]                   │
│                           │                                │
│                           ▼                                │
│                     DynamicSubscription                    │
│                       maybeCurrentSubscription:            │
│                         Some(Subscription)                 │
│                                 │                          │
│                                 ▼                          │
│                           Subscription                     │
│                             owner: OneTimeOwner            │
│                                       │                    │
│                                       ▼                    │
│                                 OneTimeOwner               │
│                                   subscriptions:           │
│                                     [Subscription] ◄───────┘
└────────────────────────────────────────────────────────────┘

```scala
// User code
onClick --> Observer { _ => println("clicked") }

// Expands to
Binder { element =>
  ReactiveElement.bindSink(element, onClick.toObservable)(observer)
}

// Which calls
DynamicSubscription.subscribeSink(element.dynamicOwner, observable, observer)

// Which creates
DynamicSubscription.unsafe(dynamicOwner, owner => 
  observable.addObserver(observer)(using owner)
  //                                ↑ implicit Owner parameter
)

// When mounted, dynamicOwner.activate() creates OneTimeOwner and calls
activate(oneTimeOwner)  // The lambda is invoked!

// Which calls
observable.addObserver(observer)(using oneTimeOwner)
//                                ↑ OneTimeOwner passed as implicit!

// Which creates
new Subscription(oneTimeOwner, cleanup)
//               ↑ Owner received!
```


┌─────────────────────────────────────────────────────────────┐
│  SCALA 2: Just needs `implicit val` in scope                │
└─────────────────────────────────────────────────────────────┘

1. Define implicit value:
   implicit val owner: Owner = new Owner {}
   ↓
2. Method with implicit parameter:
   def addObserver(observer: Observer[A])(implicit owner: Owner): Subscription
   ↓
3. Call (compiler finds implicit owner):
   addObserver(myObserver)  ← Compiler inserts: addObserver(myObserver)(owner)


┌─────────────────────────────────────────────────────────────┐
│  SCALA 3: Needs `given` to define, `using` to receive      │
└─────────────────────────────────────────────────────────────┘

1. Define given instance:
   given owner: Owner = new Owner {}
   ↓
2. Method with using parameter:
   def addObserver(observer: Observer[A])(using owner: Owner): Subscription
   ↓
3. Call (compiler finds given owner):
   addObserver(myObserver)  ← Compiler inserts: addObserver(myObserver)(using owner)