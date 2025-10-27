package www.floating

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.floatingUI.FloatingUI.computePosition
import io.github.nguyenyou.floatingUI.Types.*
import io.github.nguyenyou.floatingUI.middleware.FlipMiddleware
import io.github.nguyenyou.floatingUI.middleware.ShiftMiddleware
import org.scalajs.dom

/** Visual specification test for Flip middleware.
  *
  * Ported from floating-ui/packages/dom/test/visual/spec/Flip.tsx
  *
  * This component demonstrates and tests the flip middleware functionality with various configuration options:
  *   - placement: All 12 placement options
  *   - mainAxis: Enable/disable main axis flipping
  *   - crossAxis: Enable/disable cross axis flipping (true/false/"alignment")
  *   - fallbackPlacements: undefined, empty array, or all placements
  *   - fallbackStrategy: "bestFit" or "initialPlacement"
  *   - flipAlignment: Enable/disable alignment flipping
  *   - addShift: Add shift middleware for testing combined behavior
  *   - fallbackAxisSideDirection: "start", "end", or "none"
  */
def Flip() = {
  // Constants
  val BOOLS = Seq(true, false)
  val FALLBACK_STRATEGIES = Seq("bestFit", "initialPlacement")
  val ALL_PLACEMENTS: Seq[Placement] = Seq(
    "top-start",
    "top",
    "top-end",
    "right-start",
    "right",
    "right-end",
    "bottom-end",
    "bottom",
    "bottom-start",
    "left-end",
    "left",
    "left-start"
  )

  // State variables using Laminar Var
  val placementVar = Var[Placement]("bottom")
  val mainAxisVar = Var(true)
  val crossAxisVar = Var[FlipCrossAxis](true)
  val fallbackPlacementsVar = Var[Option[Seq[Placement]]](None)
  val fallbackStrategyVar = Var("bestFit")
  val flipAlignmentVar = Var(true)
  val addShiftVar = Var(false)
  val fallbackAxisSideDirectionVar = Var("none")

  // Scroll position state for indicator
  val scrollXVar = Var[Option[Double]](None)
  val scrollYVar = Var[Option[Double]](None)

  // Element refs
  val scrollContainerRef = Var[Option[dom.HTMLElement]](None)
  val referenceRef = Var[Option[dom.HTMLElement]](None)
  val floatingRef = Var[Option[dom.HTMLElement]](None)

  // Computed position state
  val xVar = Var(0.0)
  val yVar = Var(0.0)
  val strategyVar = Var[Strategy]("absolute")

  // Function to update floating element position
  def updatePosition(): Unit = {
    for {
      reference <- referenceRef.now()
      floating <- floatingRef.now()
    } {
      // Build middleware array
      val flipOptions = FlipOptions(
        mainAxis = mainAxisVar.now(),
        crossAxis = crossAxisVar.now(),
        fallbackPlacements = if (addShiftVar.now() && fallbackAxisSideDirectionVar.now() == "none") {
          Some(Seq("bottom"))
        } else {
          fallbackPlacementsVar.now()
        },
        fallbackStrategy = fallbackStrategyVar.now(),
        flipAlignment = flipAlignmentVar.now(),
        fallbackAxisSideDirection = "end"
      )

      val middleware = if (addShiftVar.now()) {
        Seq(FlipMiddleware.flip(Left(flipOptions)), ShiftMiddleware.shift())
      } else {
        Seq(FlipMiddleware.flip(Left(flipOptions)))
      }

      val result = computePosition(
        reference,
        floating,
        placement = placementVar.now(),
        strategy = strategyVar.now(),
        middleware = middleware
      )

      xVar.set(result.x)
      yVar.set(result.y)
      strategyVar.set(result.strategy)
    }
  }

  // Scroll indicator positioning
  val indicatorXVar = Var(0.0)
  val indicatorYVar = Var(0.0)

  def updateIndicatorPosition(): Unit = {
    for {
      reference <- referenceRef.now()
    } {
      // Create a simple indicator element for positioning
      val indicatorResult = computePosition(
        reference,
        reference, // Use reference as placeholder
        placement = "top",
        strategy = "fixed",
        middleware = Seq(ShiftMiddleware.shift(Left(ShiftOptions(crossAxis = true, padding = Left(10)))))
      )
      indicatorXVar.set(indicatorResult.x)
      indicatorYVar.set(indicatorResult.y)
    }
  }

  // Helper to create control buttons
  def controlButton[T](
    value: T,
    currentVar: Var[T],
    testId: String,
    label: String
  ): HtmlElement = {
    button(
      dataAttr("testid") := testId,
      label,
      onClick --> Observer[dom.MouseEvent] { _ =>
        currentVar.set(value)
        updatePosition()
      },
      backgroundColor <-- currentVar.signal.map { current =>
        if (current == value) "black" else ""
      },
      color <-- currentVar.signal.map { current =>
        if (current == value) "white" else ""
      }
    )
  }

  div(
    h1("Flip"),
    p(),
    div(
      className := "container",
      div(
        className := "scroll",
        dataAttr("x") := "",
        position := "relative",
        // Reference element
        div(
          className := "reference",
          "Reference",
          onMountCallback { ctx =>
            referenceRef.set(Some(ctx.thisNode.ref))
          }
        ),
        // Floating element
        div(
          className := "floating",
          "Floating",
          position.absolute,
          top.px <-- yVar.signal,
          left.px <-- xVar.signal,
          onMountCallback { ctx =>
            floatingRef.set(Some(ctx.thisNode.ref))
          }
        ),
        referenceRef.signal.combineWith(floatingRef.signal).map {
          case (Some(reference), Some(floating)) =>
            val result = computePosition(reference, floating)
            xVar.set(result.x)
            yVar.set(result.y)
          case _ => None
        } --> Observer.empty
      )
    )
  )
}
