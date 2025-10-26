package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*

case class TooltipStore(isHoveringSignal: Signal[Boolean], onHoverChange: Observer[Boolean])
