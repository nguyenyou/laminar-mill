package io.github.nguyenyou.airstream.extensions

import io.github.nguyenyou.airstream.UnitSpec
import io.github.nguyenyou.airstream.core.EventStream
import io.github.nguyenyou.airstream.eventbus.EventBus
import io.github.nguyenyou.airstream.fixtures.{Effect, TestableOwner}
import io.github.nguyenyou.airstream.state.Var
import io.github.nguyenyou.airstream.status.{Pending, Resolved}

import scala.collection.mutable

class StatusObservableSpec extends UnitSpec {

  it("StatusStream: splitStatus") {

    val owner: TestableOwner = new TestableOwner

    val innerOwner: TestableOwner = new TestableOwner

    val bus = new EventBus[Int]

    val effects = mutable.Buffer[Effect[?]]()
    bus
      .stream
      .flatMapWithStatus(v => EventStream.fromSeq(v :: v + 1 :: Nil).map(_ * 10))
      .map { v =>
        effects += Effect("tap", v)
        v
      }
      .splitStatus(
        resolved = (init, signal) => {
          effects += Effect("resolved", init)
          signal.foreach(v => effects += Effect("resolved-signal", v))(innerOwner)
          init
        },
        pending = (init, signal) => {
          effects += Effect("pending", init)
          signal.foreach(v => effects += Effect("pending-signal", v))(innerOwner)
          init
        }
      )
      .foreach(v => effects += Effect("obs", v))(owner)

    effects `shouldBe` mutable.Buffer()

    // --

    bus.emit(10)

    locally {
      val pending1 = Pending(10)
      val resolved1 = Resolved(10, 100, 1)
      val resolved2 = Resolved(10, 110, 2)

      effects `shouldBe` mutable.Buffer(
        Effect("tap", pending1),
        Effect("pending", pending1),
        Effect("pending-signal", pending1),
        Effect("obs", pending1),
        //
        Effect("tap", resolved1),
        Effect("resolved", resolved1),
        Effect("resolved-signal", resolved1),
        Effect("obs", resolved1),
        //
        Effect("tap", resolved2),
        Effect("obs", resolved1),
        Effect("resolved-signal", resolved2)
      )
    }

    effects.clear()
    innerOwner.killSubscriptions()

    // --

    bus.emit(20)

    locally {
      val pending1 = Pending(20)
      val resolved1 = Resolved(20, 200, 1)
      val resolved2 = Resolved(20, 210, 2)

      effects `shouldBe` mutable.Buffer(
        Effect("tap", pending1),
        Effect("pending", pending1),
        Effect("pending-signal", pending1),
        Effect("obs", pending1),
        //
        Effect("tap", resolved1),
        Effect("resolved", resolved1),
        Effect("resolved-signal", resolved1),
        Effect("obs", resolved1),
        //
        Effect("tap", resolved2),
        Effect("obs", resolved1),
        Effect("resolved-signal", resolved2)
      )
    }

    effects.clear()
    innerOwner.killSubscriptions()
  }

  it("StatusSignal: splitStatus") {

    val owner: TestableOwner = new TestableOwner

    val innerOwner: TestableOwner = new TestableOwner

    val _var = Var(10)

    val effects = mutable.Buffer[Effect[?]]()
    _var
      .signal
      .flatMapWithStatus(v => EventStream.fromSeq(v :: v + 1 :: Nil).map(_ * 10))
      .map { v =>
        effects += Effect("tap", v)
        v
      }
      .setDisplayName("tap")
      .splitStatus(
        resolved = (init, signal) => {
          effects += Effect("resolved", init)
          signal.foreach(v => effects += Effect("resolved-signal", v))(innerOwner)
          init
        },
        pending = (init, signal) => {
          effects += Effect("pending", init)
          signal.foreach(v => effects += Effect("pending-signal", v))(innerOwner)
          init
        }
      )
      .foreach(v => effects += Effect("obs", v))(owner)

    locally {
      val pending1 = Pending(10)
      val resolved1 = Resolved(10, 100, 1)
      val resolved2 = Resolved(10, 110, 2)

      effects `shouldBe` mutable.Buffer(
        Effect("tap", pending1),
        Effect("pending", pending1),
        Effect("pending-signal", pending1),
        Effect("obs", pending1),
        //
        Effect("tap", resolved1),
        Effect("resolved", resolved1),
        Effect("resolved-signal", resolved1),
        Effect("obs", resolved1),
        //
        Effect("tap", resolved2),
        Effect("obs", resolved1),
        Effect("resolved-signal", resolved2)
      )
    }

    effects.clear()
    innerOwner.killSubscriptions()

    // --

    _var.set(20)

    locally {
      val pending1 = Pending(20)
      val resolved1 = Resolved(20, 200, 1)
      val resolved2 = Resolved(20, 210, 2)

      effects `shouldBe` mutable.Buffer(
        Effect("tap", pending1),
        Effect("pending", pending1),
        Effect("pending-signal", pending1),
        Effect("obs", pending1),
        //
        Effect("tap", resolved1),
        Effect("resolved", resolved1),
        Effect("resolved-signal", resolved1),
        Effect("obs", resolved1),
        //
        Effect("tap", resolved2),
        Effect("obs", resolved1),
        Effect("resolved-signal", resolved2)
      )
    }

    effects.clear()
    innerOwner.killSubscriptions()
  }

}
