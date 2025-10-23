/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package java.util

import scala.scalajs.js
import scala.scalajs.js.timers.RawTimers._
import scala.scalajs.js.timers.SetTimeoutHandle

abstract class TimerTask {
  private[util] var owner: Timer = null
  private[util] var canceled: Boolean = false
  private[util] var scheduledOnceAndStarted: Boolean = false
  private[util] var lastScheduled: Long = 0L
  private[util] var handle: SetTimeoutHandle = null

  def run(): Unit

  def cancel(): Boolean = {
    if (handle != null) {
      clearTimeout(handle)
      handle = null
    }
    if (canceled || owner == null || scheduledOnceAndStarted) {
      canceled = true
      false
    } else {
      canceled = true
      true
    }
  }

  def scheduledExecutionTime(): Long = lastScheduled

  private[util] def timeout(delay: Long)(body: js.Function0[Any]): Unit = {
    if (!canceled) {
      handle = setTimeout(body, delay.toDouble)
    }
  }

  private[util] def doRun(): Unit = {
    if (!canceled && !owner.canceled) {
      lastScheduled = System.currentTimeMillis()
      try {
        run()
      } catch {
        case t: Throwable =>
          canceled = true
          throw t
      }
    }
  }

}
