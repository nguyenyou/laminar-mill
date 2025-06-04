package io.github.nguyenyou.airstream.common

import io.github.nguyenyou.airstream.core.Observable

import scala.util.Try

class Observation[A](val observable: Observable[A], val value: Try[A])
