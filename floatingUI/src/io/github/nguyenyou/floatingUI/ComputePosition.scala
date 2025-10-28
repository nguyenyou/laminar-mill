package io.github.nguyenyou.floatingUI

import Types.*
import ComputeCoordsFromPlacement.*
import org.scalajs.dom

/** Main computePosition function for floating element positioning.
  *
  * Ported from @floating-ui/core/src/computePosition.ts
  */
object ComputePosition {

  def computePosition(
    reference: ReferenceElement,
    floating: dom.HTMLElement,
    config: ComputePositionConfig
  ): ComputePositionReturn = {
    val validMiddleware = config.middleware.filter(_ != null)

    // Check RTL direction from the floating element
    val rtl = config.platform.isRTL(floating)

    var rects = config.platform.getElementRects(reference, floating, config.strategy)
    var coords = computeCoordsFromPlacement(rects, config.placement, rtl)
    var statefulPlacement = config.placement
    var middlewareData = MiddlewareData()
    var resetCount = 0

    val elements = Elements(reference, floating)

    var i = 0
    while (i < validMiddleware.length && resetCount <= 50) {
      val middleware = validMiddleware(i)

      val state = MiddlewareState(
        x = coords.x,
        y = coords.y,
        initialPlacement = config.placement,
        placement = statefulPlacement,
        strategy = config.strategy,
        middlewareData = middlewareData,
        elements = elements,
        rects = rects,
        platform = config.platform
      )

      val result = middleware.fn(state)

      coords = Coords(
        x = result.x.getOrElse(coords.x),
        y = result.y.getOrElse(coords.y)
      )

      // Update middleware data - merge with existing data instead of replacing
      result.data.foreach { data =>
        middlewareData = middleware.name match {
          case "arrow" =>
            // Merge new arrow data with existing arrow data
            val existingArrow = middlewareData.arrow
            val newArrowData = ArrowData(
              x = data.get("x").map(_.asInstanceOf[Double]).orElse(existingArrow.flatMap(_.x)),
              y = data.get("y").map(_.asInstanceOf[Double]).orElse(existingArrow.flatMap(_.y)),
              centerOffset = data
                .get("centerOffset")
                .map(_.asInstanceOf[Double])
                .getOrElse(existingArrow.map(_.centerOffset).getOrElse(0.0)),
              alignmentOffset = data
                .get("alignmentOffset")
                .map(_.asInstanceOf[Double])
                .orElse(existingArrow.flatMap(_.alignmentOffset))
            )
            middlewareData.copy(arrow = Some(newArrowData))

          case "offset" =>
            // Merge new offset data with existing offset data
            val existingOffset = middlewareData.offset
            val newOffsetData = OffsetData(
              x = data.get("x").map(_.asInstanceOf[Double]).getOrElse(existingOffset.map(_.x).getOrElse(0.0)),
              y = data.get("y").map(_.asInstanceOf[Double]).getOrElse(existingOffset.map(_.y).getOrElse(0.0)),
              placement = data
                .get("placement")
                .map(_.asInstanceOf[Placement])
                .getOrElse(existingOffset.map(_.placement).getOrElse(statefulPlacement))
            )
            middlewareData.copy(offset = Some(newOffsetData))

          case "shift" =>
            // Merge new shift data with existing shift data
            val existingShift = middlewareData.shift
            val newShiftData = ShiftData(
              x = data.get("x").map(_.asInstanceOf[Double]).getOrElse(existingShift.map(_.x).getOrElse(0.0)),
              y = data.get("y").map(_.asInstanceOf[Double]).getOrElse(existingShift.map(_.y).getOrElse(0.0)),
              enabled = data.get("enabled") match {
                case Some(enabledMap: Map[_, _]) =>
                  AxisEnabled(
                    x = enabledMap.asInstanceOf[Map[String, Any]].get("x").map(_.asInstanceOf[Boolean]).getOrElse(false),
                    y = enabledMap.asInstanceOf[Map[String, Any]].get("y").map(_.asInstanceOf[Boolean]).getOrElse(false)
                  )
                case _ => existingShift.map(_.enabled).getOrElse(AxisEnabled(x = false, y = false))
              }
            )
            middlewareData.copy(shift = Some(newShiftData))

          case "flip" =>
            // Merge new flip data with existing flip data
            val existingFlip = middlewareData.flip
            val newFlipData = FlipData(
              index = data.get("index").map(_.asInstanceOf[Int]).orElse(existingFlip.flatMap(_.index)),
              overflows = data
                .get("overflows")
                .map(_.asInstanceOf[Seq[PlacementOverflow]])
                .getOrElse(existingFlip.map(_.overflows).getOrElse(Seq.empty))
            )
            middlewareData.copy(flip = Some(newFlipData))

          case "autoPlacement" =>
            // Merge new autoPlacement data with existing autoPlacement data
            val existingAutoPlacement = middlewareData.autoPlacement
            val newAutoPlacementData = AutoPlacementData(
              index = data.get("index").map(_.asInstanceOf[Int]).orElse(existingAutoPlacement.flatMap(_.index)),
              overflows = data
                .get("overflows")
                .map(_.asInstanceOf[Seq[PlacementOverflow]])
                .getOrElse(existingAutoPlacement.map(_.overflows).getOrElse(Seq.empty))
            )
            middlewareData.copy(autoPlacement = Some(newAutoPlacementData))

          case "hide" =>
            // Merge new hide data with existing hide data
            val existingHide = middlewareData.hide
            val newHideData = HideData(
              referenceHidden = data
                .get("referenceHidden")
                .map(_.asInstanceOf[Boolean])
                .orElse(existingHide.flatMap(_.referenceHidden)),
              escaped = data
                .get("escaped")
                .map(_.asInstanceOf[Boolean])
                .orElse(existingHide.flatMap(_.escaped)),
              referenceHiddenOffsets = data
                .get("referenceHiddenOffsets")
                .map(_.asInstanceOf[SideObject])
                .orElse(existingHide.flatMap(_.referenceHiddenOffsets)),
              escapedOffsets = data
                .get("escapedOffsets")
                .map(_.asInstanceOf[SideObject])
                .orElse(existingHide.flatMap(_.escapedOffsets))
            )
            middlewareData.copy(hide = Some(newHideData))

          case "size" =>
            // Merge new size data with existing size data
            val existingSizeData = middlewareData.size
            val newSizeData = SizeData(
              availableWidth = data
                .get("availableWidth")
                .map(_.asInstanceOf[Double])
                .getOrElse(existingSizeData.map(_.availableWidth).getOrElse(0.0)),
              availableHeight = data
                .get("availableHeight")
                .map(_.asInstanceOf[Double])
                .getOrElse(existingSizeData.map(_.availableHeight).getOrElse(0.0))
            )
            middlewareData.copy(size = Some(newSizeData))

          case "inline" =>
            // Merge new inline data with existing inline data
            val existingInline = middlewareData.inline
            val newInlineData = InlineData(
              x = data.get("x").map(_.asInstanceOf[Double]).orElse(existingInline.flatMap(_.x)),
              y = data.get("y").map(_.asInstanceOf[Double]).orElse(existingInline.flatMap(_.y))
            )
            middlewareData.copy(inline = Some(newInlineData))

          case customName =>
            // For custom middleware, merge the data into the custom map
            val existingCustomData = middlewareData.custom.getOrElse(customName, Map.empty[String, Any])
            val mergedCustomData = existingCustomData match {
              case existingMap: Map[_, _] =>
                existingMap.asInstanceOf[Map[String, Any]] ++ data
              case _ =>
                data
            }
            middlewareData.copy(custom = middlewareData.custom + (customName -> mergedCustomData))
        }
      }

      // Handle reset - support both boolean true and ResetValue object
      result.reset.foreach { reset =>
        // Check reset count limit BEFORE processing (matching TypeScript behavior)
        if (resetCount <= 50) {
          resetCount += 1

          reset match {
            case Left(true) =>
              // Boolean true case: simple restart without changing placement or rects
              // Just recalculate coords with current placement and rects
              coords = computeCoordsFromPlacement(rects, statefulPlacement, rtl)
              i = -1

            case Left(false) =>
              // Boolean false case: no reset (shouldn't happen, but handle it)
              ()

            case Right(resetValue) =>
              // Object case: handle placement and rects changes
              resetValue.placement.foreach { newPlacement =>
                statefulPlacement = newPlacement
              }

              // Recalculate rects if requested
              resetValue.rects.foreach {
                case Left(true) =>
                  // Recalculate rects from platform
                  rects = config.platform.getElementRects(reference, floating, config.strategy)
                case Right(newRects) =>
                  // Use provided rects
                  rects = newRects
                case Left(false) =>
                  // Don't recalculate rects
                  ()
              }

              // Recalculate coordinates with new placement and/or rects
              coords = computeCoordsFromPlacement(rects, statefulPlacement, rtl)

              // Restart middleware loop
              i = -1
          }
        }
      }

      i += 1
    }

    ComputePositionReturn(
      x = coords.x,
      y = coords.y,
      placement = statefulPlacement,
      strategy = config.strategy,
      middlewareData = middlewareData
    )
  }
}
