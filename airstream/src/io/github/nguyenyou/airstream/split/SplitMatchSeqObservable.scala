package io.github.nguyenyou.airstream.split

import io.github.nguyenyou.airstream.core.{Observable, BaseObservable}
import scala.annotation.compileTimeOnly
import io.github.nguyenyou.airstream.core.Signal
import io.github.nguyenyou.airstream.split.MacrosUtilities.{CaseAny, HandlerAny}

final case class SplitMatchSeqObservable[Self[+_] <: Observable[?] , I, K, O, CC[_]] private (private val underlying: Unit) extends AnyVal

object SplitMatchSeqObservable {

  @compileTimeOnly("`splitMatchSeq` without `toSignal` is illegal")
  def build[Self[+_] <: Observable[?] , I, K, O, CC[_]](
    keyFn: Function1[I, K],
    distinctCompose: Function1[Signal[I], Signal[I]],
    duplicateKeysConfig: DuplicateKeysConfig,
    observable: BaseObservable[Self, CC[I]]
  )(
    caseList: CaseAny*
  )(
    handleList: HandlerAny[O]*
  ): SplitMatchSeqObservable[Self, I, K, O, CC] = throw new UnsupportedOperationException("`splitMatchSeq` without `toSignal` is illegal")

}
