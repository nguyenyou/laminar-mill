package io.github.nguyenyou.airstream.split

import io.github.nguyenyou.airstream.core.{Observable, BaseObservable, Signal}
import scala.annotation.compileTimeOnly
import io.github.nguyenyou.airstream.split.MacrosUtilities.{CaseAny, HandlerAny, MatchValueHandler}

final case class SplitMatchSeqValueObservable[Self[+_] <: Observable[?] , I, K, O, CC[_], V] private (private val underlying: Unit) extends AnyVal

object SplitMatchSeqValueObservable {

  @compileTimeOnly("`splitMatchSeq` without `toSignal` is illegal")
  def build[Self[+_] <: Observable[?] , I, K, O, CC[_], V](
    keyFn: Function1[I, K],
    distinctCompose: Function1[Signal[I], Signal[I]],
    duplicateKeysConfig: DuplicateKeysConfig,
    observable: BaseObservable[Self, CC[I]]
  )(
    caseList: CaseAny*
  )(
    handleList: HandlerAny[O]*
  )(
    valueHandler: MatchValueHandler[V]
  ): SplitMatchSeqValueObservable[Self, I, K, O, CC, V] = throw new UnsupportedOperationException("`splitMatchSeq` without `toSignal` is illegal")

}
