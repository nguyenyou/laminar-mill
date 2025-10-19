package io.github.nguyenyou.airstream.misc

import io.github.nguyenyou.airstream.UnitSpec
import io.github.nguyenyou.airstream.core.{EventStream, Observer}
import io.github.nguyenyou.airstream.fixtures.{Effect, TestableOwner}

import scala.collection.mutable

class TakeStreamSpec extends UnitSpec {

  it("Take first N events") {

    given owner: TestableOwner = new TestableOwner

    val effects = mutable.Buffer[Effect[Int]]()

    val noresetS = EventStream
      .fromSeq(List(1, 2, 3, 4))
      .take(numEvents = 2, resetOnStop = false)
      .map(Effect.log("noreset", effects))

    val resetS = EventStream
      .fromSeq(List(1, 2, 3, 4))
      .take(numEvents = 2, resetOnStop = true)
      .map(Effect.log("reset", effects))

    // --

    val sub1 = noresetS.addObserver(Observer.empty)
    val sub2 = resetS.addObserver(Observer.empty)

    effects.toList `shouldBe` List(
      Effect("noreset", 1),
      Effect("noreset", 2),
      Effect("reset", 1),
      Effect("reset", 2)
    )
    effects.clear()

    // --

    sub1.kill()
    sub2.kill()

    effects.toList `shouldBe` Nil

    // --

    val sub3 = noresetS.addObserver(Observer.empty)
    val sub4 = resetS.addObserver(Observer.empty)

    effects.toList `shouldBe` List(
      Effect("reset", 1),
      Effect("reset", 2)
    )
    effects.clear()

  }

  it("Take first ZERO events") {

    given owner: TestableOwner = new TestableOwner

    val effects = mutable.Buffer[Effect[Int]]()

    val noresetS = EventStream
      .fromSeq(List(1, 2, 3, 4))
      .take(numEvents = 0, resetOnStop = false)
      .map(Effect.log("noreset", effects))

    val resetS = EventStream
      .fromSeq(List(1, 2, 3, 4))
      .take(numEvents = 0, resetOnStop = true)
      .map(Effect.log("reset", effects))

    // --

    val sub1 = noresetS.addObserver(Observer.empty)
    val sub2 = resetS.addObserver(Observer.empty)

    effects.toList `shouldBe` Nil

    // --

    sub1.kill()
    sub2.kill()

    effects.toList `shouldBe` Nil

    // --

    val sub3 = noresetS.addObserver(Observer.empty)
    val sub4 = resetS.addObserver(Observer.empty)

    effects.toList `shouldBe` Nil
    effects.clear()

  }

  it("Take while") {

    given owner: TestableOwner = new TestableOwner

    val effects = mutable.Buffer[Effect[Int]]()

    val noresetS = EventStream
      .fromSeq(List(1, 2, 3, 4, 0, 5))
      .takeWhile(_ <= 3, resetOnStop = false)
      .map(Effect.log("noreset", effects))

    val resetS = EventStream
      .fromSeq(List(1, 2, 3, 4, 0, 5))
      .takeWhile(_ <= 3, resetOnStop = true)
      .map(Effect.log("reset", effects))

    // --

    val sub1 = noresetS.addObserver(Observer.empty)
    val sub2 = resetS.addObserver(Observer.empty)

    effects.toList `shouldBe` List(
      Effect("noreset", 1),
      Effect("noreset", 2),
      Effect("noreset", 3),
      Effect("reset", 1),
      Effect("reset", 2),
      Effect("reset", 3)
    )
    effects.clear()

    // --

    sub1.kill()
    sub2.kill()

    effects.toList `shouldBe` Nil

    // --

    val sub3 = noresetS.addObserver(Observer.empty)
    val sub4 = resetS.addObserver(Observer.empty)

    effects.toList `shouldBe` List(
      Effect("reset", 1),
      Effect("reset", 2),
      Effect("reset", 3)
    )
    effects.clear()

  }

  it("Take until") {

    given owner: TestableOwner = new TestableOwner

    val effects = mutable.Buffer[Effect[Int]]()

    val noresetS = EventStream
      .fromSeq(List(1, 2, 3, 4, 0, 5))
      .takeUntil(_ >= 4, resetOnStop = false)
      .map(Effect.log("noreset", effects))

    val resetS = EventStream
      .fromSeq(List(1, 2, 3, 4, 0, 5))
      .takeUntil(_ >= 4, resetOnStop = true)
      .map(Effect.log("reset", effects))

    // --

    val sub1 = noresetS.addObserver(Observer.empty)
    val sub2 = resetS.addObserver(Observer.empty)

    effects.toList `shouldBe` List(
      Effect("noreset", 1),
      Effect("noreset", 2),
      Effect("noreset", 3),
      Effect("reset", 1),
      Effect("reset", 2),
      Effect("reset", 3)
    )
    effects.clear()

    // --

    sub1.kill()
    sub2.kill()

    effects.toList `shouldBe` Nil

    // --

    val sub3 = noresetS.addObserver(Observer.empty)
    val sub4 = resetS.addObserver(Observer.empty)

    effects.toList `shouldBe` List(
      Effect("reset", 1),
      Effect("reset", 2),
      Effect("reset", 3)
    )
    effects.clear()

  }
}
