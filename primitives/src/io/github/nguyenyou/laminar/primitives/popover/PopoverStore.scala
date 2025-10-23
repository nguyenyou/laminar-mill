package io.github.nguyenyou.laminar.primitives.popover

import io.github.nguyenyou.laminar.api.L.*

case class PopoverStore(openSignal: Signal[Boolean], onOpenChange: Observer[Boolean])
