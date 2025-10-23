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

package java.io

class StringReader(s: String) extends Reader {

  private[this] var closed = false
  private[this] var pos = 0
  private[this] var mark = 0

  override def close(): Unit = {
    closed = true
  }

  override def mark(readAheadLimit: Int): Unit = {
    if (readAheadLimit < 0)
      throw new IllegalArgumentException("Read-ahead limit < 0")
    ensureOpen()

    mark = pos
  }

  override def markSupported(): Boolean = true

  override def read(): Int = {
    ensureOpen()

    if (pos < s.length) {
      val res = s.charAt(pos).toInt
      pos += 1
      res
    } else -1
  }

  override def read(cbuf: Array[Char], off: Int, len: Int): Int = {
    ensureOpen()

    if (off < 0 || len < 0 || len > cbuf.length - off)
      throw new IndexOutOfBoundsException

    if (len == 0) 0
    else {
      val count = Math.min(len, s.length - pos)
      var i = 0
      while (i < count) {
        cbuf(off + i) = s.charAt(pos + i)
        i += 1
      }
      pos += count
      if (count == 0) -1 else count
    }
  }

  override def ready(): Boolean = {
    ensureOpen()
    true
  }

  override def reset(): Unit = {
    ensureOpen()
    pos = mark
  }

  override def skip(ns: Long): Long = {
    if (pos >= s.length) {
      // Always return 0 if the entire string has been read or skipped
      0
    } else {
      // Apparently, StringReader.skip allows negative skips
      val count = Math.max(Math.min(ns, s.length - pos).toInt, -pos)
      pos += count
      count.toLong
    }
  }

  private def ensureOpen(): Unit = {
    if (closed)
      throw new IOException("Operation on closed stream")
  }

}
