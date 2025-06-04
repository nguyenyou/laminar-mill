package io.github.nguyenyou.airstream.split

import io.github.nguyenyou.airstream.core.{Observable, BaseObservable}
import scala.annotation.compileTimeOnly
import io.github.nguyenyou.airstream.core.Signal
import io.github.nguyenyou.airstream.split.MacrosUtilities.{CaseAny, HandlerAny, MatchTypeHandler}

final case class SplitMatchSeqTypeObservable[Self[+_] <: Observable[?] , I, K, O, CC[_], T] private (private val underlying: Unit) extends AnyVal

object SplitMatchSeqTypeObservable {

  @compileTimeOnly("`splitMatchSeq` without `toSignal` is illegal")
  def build[Self[+_] <: Observable[?] , I, K, O, CC[_], T](
    keyFn: Function1[I, K],
    distinctCompose: Function1[Signal[I], Signal[I]],
    duplicateKeysConfig: DuplicateKeysConfig,
    observable: BaseObservable[Self, CC[I]]
  )(
    caseList: CaseAny*
  )(
    handleList: HandlerAny[O]*
  )(
    tHandler: MatchTypeHandler[T]
  ): SplitMatchSeqTypeObservable[Self, I, K, O, CC, T] = throw new UnsupportedOperationException("`splitMatchSeq` without `toSignal` is illegal")

}
