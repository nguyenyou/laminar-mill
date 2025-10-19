package io.github.nguyenyou.airstream.core

import io.github.nguyenyou.airstream.UnitSpec
import io.github.nguyenyou.airstream.eventbus.EventBus
import io.github.nguyenyou.airstream.fixtures.{Effect, TestableOwner}
import io.github.nguyenyou.airstream.ownership.Owner
import org.scalactic.anyvals.NonEmptyList

import java.util.concurrent.Flow
import scala.collection.mutable

class EventStreamSpec extends UnitSpec {

  it("EventStream.fromSeq emit on restart") {

    given owner: Owner = new TestableOwner

    val range = 1 to 3
    val stream = EventStream.fromSeq(range)

    val effects = mutable.Buffer[Effect[?]]()
    val sub1 = stream.foreach(newValue => effects += Effect("obs1", newValue))

    effects.toList `shouldBe` range.map(i => Effect("obs1", i))
    effects.clear()

    sub1.kill()

    val sub2 = stream.foreach(newValue => effects += Effect("obs2", newValue))

    effects.toList `shouldBe` range.map(i => Effect("obs2", i))
    effects.clear()
  }

  it("EventStream.fromSeq.startWith emit on restart") {

    given owner: Owner = new TestableOwner

    val range = 1 to 3
    val signal = EventStream.fromSeq(range).startWith(0)

    val effects = mutable.Buffer[Effect[?]]()
    val sub1 = signal.foreach(newValue => effects += Effect("obs1", newValue))

    effects.toList `shouldBe` (0 +: range).map(i => Effect("obs1", i))
    effects.clear()

    sub1.kill()

    val sub2 = signal.foreach(newValue => effects += Effect("obs2", newValue))

    effects.toList `shouldBe` (3 +: range).map(i => Effect("obs2", i))
    effects.clear()
  }

  it("filter") {

    given owner: Owner = new TestableOwner

    val f = (_: Int) % 2 == 0
    val range = 0 to 10
    val stream = EventStream.fromSeq(range, emitOnce = true)

    val effects = mutable.Buffer[Effect[?]]()
    val subscription0 = stream.filter(f).foreach(newValue => effects += Effect("obs0", newValue))

    subscription0.kill()
    effects.toList `shouldBe` range.filter(f).map(i => Effect("obs0", i))
  }

  it("filterNot") {

    given owner: Owner = new TestableOwner

    val f = (_: Int) % 2 == 0
    val range = 0 to 10
    val stream = EventStream.fromSeq(range, emitOnce = true)

    val effects = mutable.Buffer[Effect[?]]()
    val subscription0 = stream.filterNot(f).foreach(newValue => effects += Effect("obs0", newValue))

    subscription0.kill()
    effects.toList `shouldBe` range.filterNot(f).map(i => Effect("obs0", i))
  }

  it("collect") {

    given owner: Owner = new TestableOwner

    val bus = new EventBus[Either[String, Int]]

    val effects = mutable.Buffer[Effect[?]]()
    bus.events
      .collect { case Right(i) => i }
      .foreach(v => effects += Effect("obs", v))

    effects `shouldBe` mutable.Buffer()

    // --

    bus.emit(Right(1))

    effects `shouldBe` mutable.Buffer(
      Effect("obs", 1)
    )
    effects.clear()

    // --

    bus.emit(Right(2))

    effects `shouldBe` mutable.Buffer(
      Effect("obs", 2)
    )
    effects.clear()

    // --

    bus.emit(Left("yo"))

    effects `shouldBe` mutable.Buffer()

    // --

    bus.emit(Right(3))

    effects `shouldBe` mutable.Buffer(
      Effect("obs", 3)
    )
    effects.clear()

  }

  it("collectOpt") {

    // def NonEmptyList[A](list: List[A]): Option[List[A]] = {
    //  if (list.nonEmpty) Some(list) else None
    // }

    given owner: Owner = new TestableOwner

    val bus = new EventBus[List[Int]]

    val effects = mutable.Buffer[Effect[?]]()
    bus.events
      .collectOpt(NonEmptyList.from(_))
      .foreach(v => effects += Effect("obs", v.head))

    effects `shouldBe` mutable.Buffer()

    // --

    bus.emit(List(1))

    effects `shouldBe` mutable.Buffer(
      Effect("obs", 1)
    )
    effects.clear()

    // --

    bus.emit(List(2))

    effects `shouldBe` mutable.Buffer(
      Effect("obs", 2)
    )
    effects.clear()

    // --

    bus.emit(Nil)

    effects `shouldBe` mutable.Buffer()

    // --

    bus.emit(List(3))

    effects `shouldBe` mutable.Buffer(
      Effect("obs", 3)
    )
    effects.clear()

  }

}
