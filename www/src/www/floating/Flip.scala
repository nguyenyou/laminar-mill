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
        Seq(FlipMiddleware.flip(flipOptions), ShiftMiddleware.shift())
      } else {
        Seq(FlipMiddleware.flip(flipOptions))
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
        middleware = Seq(ShiftMiddleware.shift(ShiftOptions(crossAxis = true, padding = Left(10))))
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
        onMountCallback { ctx =>
          scrollContainerRef.set(Some(ctx.thisNode.ref))

          // Center scroll position on mount
          val scrollEl = ctx.thisNode.ref
          val y = scrollEl.scrollHeight / 2 - scrollEl.offsetHeight / 2
          val x = scrollEl.scrollWidth / 2 - scrollEl.offsetWidth / 2
          scrollEl.scrollTop = y
          scrollEl.scrollLeft = x

          // Update position on mount
          updatePosition()
          updateIndicatorPosition()

          // Listen to scroll events
          ctx.thisNode.ref.addEventListener(
            "scroll",
            { (_: dom.Event) =>
              scrollXVar.set(Some(scrollEl.scrollLeft))
              scrollYVar.set(Some(scrollEl.scrollTop))
              updatePosition()
              updateIndicatorPosition()
            }
          )
        },
        // Scroll indicator
        div(
          className := "scroll-indicator",
          position := "fixed",
          left <-- indicatorXVar.signal.map(x => s"${x}px"),
          top <-- indicatorYVar.signal.map(y => s"${y}px"),
          child.text <-- scrollXVar.signal.combineWith(scrollYVar.signal).map {
            case (Some(x), Some(y)) => s"x: ${x.toInt}, y: ${y.toInt}"
            case _                  => "x: 0, y: 0"
          }
        ),
        // Reference element
        div(
          className := "reference",
          "Reference"
        ),
        // Floating element
        div(
          className := "floating",
          "Floating"
        )
      )
    ),

    // Placement controls
    h2("placement"),
    div(
      className := "controls",
      ALL_PLACEMENTS.map { localPlacement =>
        controlButton(
          localPlacement,
          placementVar,
          s"placement-$localPlacement",
          localPlacement
        )
      }
    ),

    // Main axis controls
    h2("mainAxis"),
    div(
      className := "controls",
      BOOLS.map { bool =>
        controlButton(
          bool,
          mainAxisVar,
          s"mainAxis-$bool",
          bool.toString
        )
      }
    ),

    // Cross axis controls
    h2("crossAxis"),
    div(
      className := "controls",
      (BOOLS :+ "alignment").map { value =>
        val crossAxisValue: FlipCrossAxis = value match {
          case b: Boolean => b
          case s: String  => s
        }
        button(
          dataAttr("testid") := s"crossAxis-$value",
          value.toString,
          onClick --> Observer[dom.MouseEvent] { _ =>
            crossAxisVar.set(crossAxisValue)
            updatePosition()
          },
          backgroundColor <-- crossAxisVar.signal.map { current =>
            val matches = (current, value) match {
              case (b1: Boolean, b2: Boolean) => b1 == b2
              case (s1: String, s2: String)   => s1 == s2
              case _                          => false
            }
            if (matches) "black" else ""
          },
          color <-- crossAxisVar.signal.map { current =>
            val matches = (current, value) match {
              case (b1: Boolean, b2: Boolean) => b1 == b2
              case (s1: String, s2: String)   => s1 == s2
              case _                          => false
            }
            if (matches) "white" else ""
          }
        )
      }
    ),

    // Fallback placements controls
    h2("fallbackPlacements"),
    div(
      className := "controls",
      Seq(
        ("undefined", None),
        ("[]", Some(Seq.empty[Placement])),
        ("all", Some(ALL_PLACEMENTS))
      ).map { case (label, value) =>
        button(
          dataAttr("testid") := s"fallbackPlacements-$label",
          label match {
            case "undefined" => "undefined"
            case "[]"        => "[]"
            case "all"       => s"[${ALL_PLACEMENTS.mkString(", ")}]"
            case _           => label
          },
          onClick --> Observer[dom.MouseEvent] { _ =>
            fallbackPlacementsVar.set(value)
            updatePosition()
          },
          backgroundColor <-- fallbackPlacementsVar.signal.map { current =>
            val matches = (current, value) match {
              case (None, None)                               => true
              case (Some(c), Some(v)) if c.length == v.length => true
              case _                                          => false
            }
            if (matches) "black" else ""
          },
          color <-- fallbackPlacementsVar.signal.map { current =>
            val matches = (current, value) match {
              case (None, None)                               => true
              case (Some(c), Some(v)) if c.length == v.length => true
              case _                                          => false
            }
            if (matches) "white" else ""
          }
        )
      }
    ),

    // Fallback strategy controls
    h2("fallbackStrategy"),
    div(
      className := "controls",
      FALLBACK_STRATEGIES.map { strategy =>
        controlButton(
          strategy,
          fallbackStrategyVar,
          s"fallbackStrategy-$strategy",
          strategy
        )
      }
    ),

    // Flip alignment controls
    h2("flipAlignment"),
    div(
      className := "controls",
      BOOLS.map { bool =>
        controlButton(
          bool,
          flipAlignmentVar,
          s"flipAlignment-$bool",
          bool.toString
        )
      }
    ),

    // Add shift controls
    h2("Add shift"),
    div(
      className := "controls",
      BOOLS.map { bool =>
        controlButton(
          bool,
          addShiftVar,
          s"shift-$bool",
          bool.toString
        )
      }
    ),

    // Fallback axis side direction controls
    h2("fallbackAxisSideDirection"),
    div(
      className := "controls",
      Seq("start", "end", "none").map { value =>
        controlButton(
          value,
          fallbackAxisSideDirectionVar,
          s"fallbackAxisSideDirection-$value",
          value
        )
      }
    )
  )
}
