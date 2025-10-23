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

package org.scalajs.testing.bridge

import scala.scalajs.js

import java.net.URL
import java.io.InputStream
import java.util.Enumeration

/** A dummy [[java.lang.ClassLoader]] for Scala.js testing frameworks.
 *
 *  This class loader does *not* implement the contract of
 *  [[java.lang.ClassLoader]]. It is exclusively used as a dummy class loader
 *  to preserve source compatibility with the sbt testing interface.
 */
private[bridge] final class ScalaJSClassLoader extends ClassLoader(null) {
  private def nimp: Nothing = {
    throw new NotImplementedError(
        "A ScalaJSClassLoader is a dummy. " +
        "Use scala.scalajs.reflect.Reflect (JS-only) or " +
        "https://github.com/portable-scala/portable-scala-reflect (portable) " +
        "to instantiate things.")
  }

  override def clearAssertionStatus(): Unit = nimp
  override def getResource(name: String): URL = nimp
  override def getResourceAsStream(name: String): InputStream = nimp
  override def getResources(name: String): Enumeration[URL] = nimp
  override def loadClass(name: String): Class[_] = nimp
  override def setClassAssertionStatus(className: String, enabled: Boolean): Unit = nimp
  override def setDefaultAssertionStatus(enabled: Boolean): Unit = nimp
  override def setPackageAssertionStatus(packageName: String, enabled: Boolean): Unit = nimp
}
