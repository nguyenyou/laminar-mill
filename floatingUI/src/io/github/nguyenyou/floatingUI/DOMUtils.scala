package io.github.nguyenyou.floatingUI

import org.scalajs.dom
import scala.scalajs.js
import Types.*
import Utils.*

/** DOM-specific utility functions for floating element positioning.
  *
  * Ported from @floating-ui/dom/src/utils
  */
object DOMUtils {

  private val transformProperties = Seq("transform", "translate", "scale", "rotate", "perspective")
  private val willChangeValues = Seq("transform", "translate", "scale", "rotate", "perspective", "filter")
  private val containValues = Seq("paint", "layout", "strict", "content")
  private val topLayerSelectors = Seq(":popover-open", ":modal")

  // ============================================================================
  // Bounding Client Rect (Full Implementation)
  // ============================================================================

  /** Get bounding client rect for an element with full support for scale, visual offsets, and iframe traversal.
    *
    * This is the complete implementation matching TypeScript's getBoundingClientRect from utils/getBoundingClientRect.ts.
    *
    * @param element
    *   The element or virtual element to get the bounding rect for
    * @param includeScale
    *   Whether to include scale transformations in the calculation
    * @param isFixedStrategy
    *   Whether the element uses fixed positioning strategy
    * @param offsetParent
    *   The offset parent element or window
    * @return
    *   The bounding client rect with all transformations applied
    */
  def getBoundingClientRect(
    element: Types.ReferenceElement,
    includeScale: Boolean = false,
    isFixedStrategy: Boolean = false,
    offsetParent: Option[Any] = None
  ): ClientRectObject = {
    // Get the raw bounding client rect - convert to common format
    val rawRect = element match {
      case ve: Types.VirtualElement => ve.getBoundingClientRect()
      case e: dom.Element           => e.getBoundingClientRect()
    }

    // Extract values from either ClientRectObject or DOMRect
    val (rectLeft, rectTop, rectWidth, rectHeight) = rawRect match {
      case cro: ClientRectObject => (cro.left, cro.top, cro.width, cro.height)
      case dr: dom.DOMRect       => (dr.left, dr.top, dr.width, dr.height)
      case _                     => (0.0, 0.0, 0.0, 0.0)
    }

    // Unwrap virtual element to get the DOM element
    val domElement = Utils.unwrapElement(element)

    // Calculate scale
    var scale = Coords(1, 1)
    if (includeScale) {
      offsetParent match {
        case Some(op) if op.isInstanceOf[dom.Element] =>
          scale = getScale(op.asInstanceOf[dom.Element])
        case Some(_) =>
          // offsetParent is window or other non-element, use default scale
          scale = Coords(1, 1)
        case None =>
          // No offsetParent specified, get scale from element itself
          element match {
            case e: dom.Element => scale = getScale(e)
            case _              => scale = Coords(1, 1)
          }
      }
    }

    // Calculate visual offsets
    val visualOffsets = if (shouldAddVisualOffsets(Option(domElement), isFixedStrategy, offsetParent)) {
      getVisualOffsets(Option(domElement))
    } else {
      Coords(0, 0)
    }

    // Apply scale and visual offsets
    var x = (rectLeft + visualOffsets.x) / scale.x
    var y = (rectTop + visualOffsets.y) / scale.y
    var width = rectWidth / scale.x
    var height = rectHeight / scale.y

    // Handle iframe traversal
    if (domElement != null) {
      val win = Utils.getWindow(domElement)
      val offsetWin = offsetParent match {
        case Some(op) if op.isInstanceOf[dom.Element] =>
          Utils.getWindow(op.asInstanceOf[dom.Element])
        case Some(w: dom.Window) =>
          w
        case _ =>
          null
      }

      var currentWin = win
      var currentIFrame = Utils.getFrameElement(currentWin)

      while (currentIFrame != null && offsetParent.isDefined && offsetWin != currentWin) {
        val iframeScale = getScale(currentIFrame)
        val iframeRect = currentIFrame.getBoundingClientRect()
        val css = dom.window.getComputedStyle(currentIFrame)

        val left = iframeRect.left +
          (currentIFrame.clientLeft + css.paddingLeft.replace("px", "").toDoubleOption.getOrElse(0.0)) * iframeScale.x
        val top = iframeRect.top +
          (currentIFrame.clientTop + css.paddingTop.replace("px", "").toDoubleOption.getOrElse(0.0)) * iframeScale.y

        x *= iframeScale.x
        y *= iframeScale.y
        width *= iframeScale.x
        height *= iframeScale.y

        x += left
        y += top

        currentWin = Utils.getWindow(currentIFrame)
        currentIFrame = Utils.getFrameElement(currentWin)
      }
    }

    // Convert to ClientRectObject
    Utils.rectToClientRect(Rect(x, y, width, height))
  }

  // ============================================================================
  // CSS and Dimensions
  // ============================================================================

  /** Get CSS dimensions of an element.
    *
    * Returns width, height, and a flag indicating if fallback to offset dimensions was used.
    */
  def getCssDimensions(element: dom.Element): (Double, Double, Boolean) = {
    val css = dom.window.getComputedStyle(element)

    // In testing environments, the `width` and `height` properties can be empty
    // strings for SVG elements, resulting in NaN. Fallback to 0.0 in this case.
    var width = css.width.replace("px", "").toDoubleOption.getOrElse(0.0)
    var height = css.height.replace("px", "").toDoubleOption.getOrElse(0.0)

    val hasOffset = element.isInstanceOf[dom.HTMLElement]
    val (offsetWidth, offsetHeight) =
      if (hasOffset) {
        val htmlElement = element.asInstanceOf[dom.HTMLElement]
        (htmlElement.offsetWidth.toDouble, htmlElement.offsetHeight.toDouble)
      } else {
        (width, height)
      }

    val shouldFallback =
      math.round(width) != offsetWidth || math.round(height) != offsetHeight

    if (shouldFallback) {
      width = offsetWidth
      height = offsetHeight
    }

    (width, height, shouldFallback)
  }

  /** Get scale of an element (ratio of visual size to CSS size). */
  def getScale(element: dom.Element): Coords = {
    if (!element.isInstanceOf[dom.HTMLElement]) {
      return Coords(1, 1)
    }

    val rect = element.getBoundingClientRect()
    val (width, height, shouldFallback) = getCssDimensions(element)

    var x = (if (shouldFallback) math.round(rect.width).toDouble else rect.width) / width
    var y = (if (shouldFallback) math.round(rect.height).toDouble else rect.height) / height

    // 0, NaN, or Infinity should always fallback to 1
    if (!x.isFinite || x == 0) x = 1
    if (!y.isFinite || y == 0) y = 1

    Coords(x, y)
  }

  // ============================================================================
  // Positioning Utilities
  // ============================================================================

  /** Check if an element is statically positioned. */
  def isStaticPositioned(element: dom.Element): Boolean = {
    dom.window.getComputedStyle(element).position == "static"
  }

  /** Check if an element is a table element. */
  def isTableElement(element: dom.Element): Boolean = {
    val nodeName = element.nodeName.toLowerCase
    nodeName == "table" || nodeName == "td" || nodeName == "th"
  }

  /** Check if an element is a containing block (matches @floating-ui/utils/dom). */
  def isContainingBlock(element: dom.Element): Boolean = {
    val css =
      if (element != null) dom.window.getComputedStyle(element)
      else return false

    val webkit = isWebKit()

    def value(prop: String): Option[String] = {
      val raw = css.asInstanceOf[js.Dynamic].selectDynamic(prop)
      if (js.isUndefined(raw) || raw == null) None else Some(raw.toString)
    }

    val hasTransform = transformProperties.exists(prop => value(prop).exists(v => v.nonEmpty && v != "none"))
    val containerType = value("containerType").getOrElse("")
    val backdropFilter = value("backdropFilter").getOrElse("")
    val filter = value("filter").getOrElse("")
    val willChange = Option(css.getPropertyValue("will-change")).getOrElse("")
    val contain = Option(css.getPropertyValue("contain")).getOrElse("")

    hasTransform ||
    (containerType.nonEmpty && containerType != "normal") ||
    (!webkit && backdropFilter != "none") ||
    (!webkit && filter != "none") ||
    willChangeValues.exists(willChange.contains) ||
    containValues.exists(contain.contains)
  }

  /** Check if element is in top layer using selector-based detection. */
  def isTopLayer(element: dom.Element): Boolean = {
    if (element == null) return false
    topLayerSelectors.exists { selector =>
      try {
        element.asInstanceOf[js.Dynamic].applyDynamic("matches")(selector).asInstanceOf[Boolean]
      } catch {
        case _: Throwable => false
      }
    }
  }

  /** Get node scroll position. */
  def getNodeScroll(node: dom.Node): (Double, Double) = {
    if (node == dom.window || !node.isInstanceOf[dom.Element]) {
      (dom.window.scrollX, dom.window.scrollY)
    } else {
      val element = node.asInstanceOf[dom.Element]
      (element.scrollLeft, element.scrollTop)
    }
  }

  /** Get node name. */
  def getNodeName(node: dom.Node): String = {
    node.nodeName.toLowerCase
  }

  // ============================================================================
  // Offset Parent
  // ============================================================================

  /** Get the true offset parent of an element. */
  private def getTrueOffsetParent(element: dom.Element): Option[dom.Element] = {
    if (!element.isInstanceOf[dom.HTMLElement]) return None

    val htmlElement = element.asInstanceOf[dom.HTMLElement]
    if (dom.window.getComputedStyle(htmlElement).position == "fixed") {
      return None
    }

    val rawOffsetParent = htmlElement.offsetParent

    if (rawOffsetParent == null) return None

    // Firefox returns <html> as offsetParent if it's non-static,
    // but we should use <body> for correct calculations
    if (getDocumentElement(element) == rawOffsetParent) {
      Option(rawOffsetParent.ownerDocument.asInstanceOf[dom.HTMLDocument].body)
    } else {
      Option(rawOffsetParent)
    }
  }

  /** Get the offset parent of an element (closest positioned ancestor). */
  def getOffsetParent(element: dom.Element): dom.EventTarget = {
    val win = dom.window

    if (isTopLayer(element)) {
      return win
    }

    if (!element.isInstanceOf[dom.HTMLElement]) {
      // For SVG elements, traverse up to find positioned ancestor
      var svgOffsetParent = getParentNode(element)
      while (svgOffsetParent != null && !isLastTraversableNode(svgOffsetParent)) {
        if (
          svgOffsetParent.isInstanceOf[dom.Element] &&
          !isStaticPositioned(svgOffsetParent.asInstanceOf[dom.Element])
        ) {
          return svgOffsetParent
        }
        svgOffsetParent = getParentNode(svgOffsetParent)
      }
      return win
    }

    var offsetParent = getTrueOffsetParent(element)

    // Skip table elements that are statically positioned
    while (
      offsetParent.isDefined &&
      isTableElement(offsetParent.get) &&
      isStaticPositioned(offsetParent.get)
    ) {
      offsetParent = getTrueOffsetParent(offsetParent.get)
    }

    if (
      offsetParent.isDefined &&
      isLastTraversableNode(offsetParent.get) &&
      isStaticPositioned(offsetParent.get) &&
      !isContainingBlock(offsetParent.get)
    ) {
      return win
    }

    offsetParent
      .orElse(getContainingBlockElement(element))
      .getOrElse(win)
  }

  /** Get containing block for an element. */
  private def getContainingBlockElement(element: dom.Element): Option[dom.Element] = {
    var currentNode = getParentNode(element)

    while (currentNode != null && currentNode.isInstanceOf[dom.Element]) {
      val elem = currentNode.asInstanceOf[dom.Element]
      if (isContainingBlock(elem)) {
        return Some(elem)
      }
      if (isLastTraversableNode(elem)) {
        return None
      }
      currentNode = getParentNode(currentNode)
    }

    None
  }

  // ============================================================================
  // Viewport and Document Rects
  // ============================================================================

  /** Get window scrollbar X position. */
  def getWindowScrollBarX(element: dom.Element, rect: Option[dom.DOMRect] = None): Double = {
    val (leftScroll, _) = getNodeScroll(element)

    rect match {
      case Some(r) => r.left + leftScroll
      case None =>
        val docElement = getDocumentElement(element)
        getBoundingClientRect(docElement).left + leftScroll
    }
  }

  /** Get viewport rectangle. */
  def getViewportRect(element: dom.Element, strategy: Strategy): Rect = {
    val win = dom.window
    val html = getDocumentElement(element)

    var width = html.clientWidth.toDouble
    var height = html.clientHeight.toDouble
    var x = 0.0
    var y = 0.0

    // Check for visual viewport support
    val visualViewport = win.asInstanceOf[js.Dynamic].visualViewport
    if (!js.isUndefined(visualViewport) && visualViewport != null) {
      width = visualViewport.width.asInstanceOf[Double]
      height = visualViewport.height.asInstanceOf[Double]

      // For fixed positioning, include visual viewport offsets
      if (strategy == Strategy.Fixed) {
        x = visualViewport.offsetLeft.asInstanceOf[Double]
        y = visualViewport.offsetTop.asInstanceOf[Double]
      }
    }

    Rect(x, y, width, height)
  }

  /** Get document rectangle. */
  def getDocumentRect(element: dom.HTMLElement): Rect = {
    val html = getDocumentElement(element)
    val (scrollLeft, scrollTop) = getNodeScroll(element)
    val body = element.ownerDocument.asInstanceOf[dom.HTMLDocument].body

    val width = Seq(
      html.scrollWidth.toDouble,
      html.clientWidth.toDouble,
      body.scrollWidth.toDouble,
      body.clientWidth.toDouble
    ).max

    val height = Seq(
      html.scrollHeight.toDouble,
      html.clientHeight.toDouble,
      body.scrollHeight.toDouble,
      body.clientHeight.toDouble
    ).max

    var x = -scrollLeft + getWindowScrollBarX(element, None)
    val y = -scrollTop

    if (dom.window.getComputedStyle(body).direction == "rtl") {
      x += math.max(html.clientWidth.toDouble, body.clientWidth.toDouble) - width
    }

    Rect(x, y, width, height)
  }

  /** Check if browser is WebKit-based using CSS.supports detection. */
  def isWebKit(): Boolean = {
    val css = js.Dynamic.global.selectDynamic("CSS")
    if (js.isUndefined(css) || js.isUndefined(css.selectDynamic("supports"))) {
      false
    } else {
      css
        .selectDynamic("supports")
        .asInstanceOf[js.Function2[String, String, Boolean]]
        .apply("-webkit-backdrop-filter", "none")
    }
  }

  /** Get visual offsets (for visual viewport). */
  def getVisualOffsets(element: Option[dom.Element]): Coords = {
    val win = dom.window
    val visualViewport = win.asInstanceOf[js.Dynamic].visualViewport

    if (!isWebKit() || js.isUndefined(visualViewport) || visualViewport == null) {
      return Coords(0, 0)
    }

    Coords(
      visualViewport.offsetLeft.asInstanceOf[Double],
      visualViewport.offsetTop.asInstanceOf[Double]
    )
  }

  /** Check if visual offsets should be added.
    *
    * @param element
    *   The element to check
    * @param isFixed
    *   Whether the element uses fixed positioning
    * @param floatingOffsetParent
    *   The offset parent of the floating element
    * @return
    *   True if visual offsets should be added
    */
  def shouldAddVisualOffsets(
    element: Option[dom.Element],
    isFixed: Boolean = false,
    floatingOffsetParent: Option[Any] = None
  ): Boolean = {
    if (floatingOffsetParent.isEmpty) {
      return false
    }

    if (isFixed && floatingOffsetParent != Some(Utils.getWindow(element.orNull))) {
      return false
    }

    isFixed
  }

  /** Get HTML offset. */
  def getHTMLOffset(documentElement: dom.HTMLElement, scroll: (Double, Double)): Coords = {
    val htmlRect = documentElement.getBoundingClientRect()
    val x = htmlRect.left + scroll._1 - getWindowScrollBarX(documentElement, Some(htmlRect))
    val y = htmlRect.top + scroll._2
    Coords(x, y)
  }

  // ============================================================================
  // Coordinate Conversion
  // ============================================================================

  /** Convert offset-parent-relative rect to viewport-relative rect. */
  def convertOffsetParentRelativeRectToViewportRelativeRect(
    rect: Rect,
    offsetParent: dom.EventTarget,
    strategy: Strategy
  ): Rect = {
    val isFixed = strategy == Strategy.Fixed

    // If offsetParent is window or document element, no conversion needed
    if (!offsetParent.isInstanceOf[dom.Element]) {
      return rect
    }

    val offsetParentElement = offsetParent.asInstanceOf[dom.Element]
    val documentElement = getDocumentElement(offsetParentElement)

    if (offsetParent == documentElement || (isTopLayer(offsetParentElement) && isFixed)) {
      return rect
    }

    var scroll = (0.0, 0.0)
    var scale = Coords(1, 1)
    var offsets = Coords(0, 0)
    val isOffsetParentAnElement = offsetParentElement.isInstanceOf[dom.HTMLElement]

    if (isOffsetParentAnElement || (!isOffsetParentAnElement && !isFixed)) {
      if (
        getNodeName(offsetParentElement) != "body" ||
        isOverflowElement(documentElement)
      ) {
        scroll = getNodeScroll(offsetParentElement)
      }

      if (offsetParentElement.isInstanceOf[dom.HTMLElement]) {
        val htmlOffsetParent = offsetParentElement.asInstanceOf[dom.HTMLElement]
        val offsetRect = getBoundingClientRect(
          htmlOffsetParent,
          includeScale = true,
          isFixedStrategy = isFixed,
          offsetParent = Some(offsetParent)
        )
        scale = getScale(htmlOffsetParent)
        offsets = Coords(
          offsetRect.x + htmlOffsetParent.clientLeft,
          offsetRect.y + htmlOffsetParent.clientTop
        )
      }
    }

    val htmlOffset =
      if (
        documentElement.isInstanceOf[dom.HTMLElement] &&
        !isOffsetParentAnElement && !isFixed
      ) {
        getHTMLOffset(documentElement.asInstanceOf[dom.HTMLElement], scroll)
      } else {
        Coords(0, 0)
      }

    Rect(
      x = rect.x * scale.x - scroll._1 * scale.x + offsets.x + htmlOffset.x,
      y = rect.y * scale.y - scroll._2 * scale.y + offsets.y + htmlOffset.y,
      width = rect.width * scale.x,
      height = rect.height * scale.y
    )
  }

  /** Get rect relative to offset parent.
    *
    * This matches the TypeScript implementation from floating-ui/packages/dom/src/utils/getRectRelativeToOffsetParent.ts
    */
  def getRectRelativeToOffsetParent(
    element: ReferenceElement,
    offsetParent: dom.EventTarget,
    strategy: Strategy
  ): Rect = {
    val isOffsetParentAnElement = offsetParent.isInstanceOf[dom.HTMLElement]
    val documentElement = if (offsetParent.isInstanceOf[dom.Node]) {
      getDocumentElement(offsetParent.asInstanceOf[dom.Node])
    } else {
      dom.document.documentElement
    }
    val isFixed = strategy == Strategy.Fixed
    val rect = getBoundingClientRect(element, includeScale = true, isFixedStrategy = isFixed, offsetParent = Some(offsetParent))

    var scroll = (0.0, 0.0)
    var offsets = Coords(0, 0)

    // Helper function for RTL scrollbar offset
    def setLeftRTLScrollbarOffset(): Unit = {
      offsets = Coords(getWindowScrollBarX(documentElement, None), offsets.y)
    }

    if (isOffsetParentAnElement || (!isOffsetParentAnElement && !isFixed)) {
      val offsetParentNode = if (offsetParent.isInstanceOf[dom.Node]) {
        offsetParent.asInstanceOf[dom.Node]
      } else {
        dom.window.asInstanceOf[dom.Node]
      }

      if (
        getNodeName(offsetParentNode) != "body" ||
        isOverflowElement(documentElement)
      ) {
        scroll = getNodeScroll(offsetParentNode)
      }

      if (isOffsetParentAnElement) {
        val offsetParentElement = offsetParent.asInstanceOf[dom.HTMLElement]
        val offsetRect = getBoundingClientRect(
          offsetParentElement,
          includeScale = true,
          isFixedStrategy = isFixed,
          offsetParent = Some(offsetParent)
        )
        offsets = Coords(
          offsetRect.x + offsetParentElement.clientLeft,
          offsetRect.y + offsetParentElement.clientTop
        )
      } else if (documentElement != null) {
        setLeftRTLScrollbarOffset()
      }
    }

    if (isFixed && !isOffsetParentAnElement && documentElement != null) {
      setLeftRTLScrollbarOffset()
    }

    val htmlOffset =
      if (documentElement != null && !isOffsetParentAnElement && !isFixed) {
        getHTMLOffset(documentElement.asInstanceOf[dom.HTMLElement], scroll)
      } else {
        Coords(0, 0)
      }

    val x = rect.left + scroll._1 - offsets.x - htmlOffset.x
    val y = rect.top + scroll._2 - offsets.y - htmlOffset.y

    Rect(
      x = x,
      y = y,
      width = rect.width,
      height = rect.height
    )
  }

  // ============================================================================
  // Clipping Rect Calculation
  // ============================================================================

  /** Get inner bounding client rect (subtracting scrollbars). */
  private def getInnerBoundingClientRect(element: dom.Element, strategy: Strategy): Rect = {
    // Use full getBoundingClientRect with scale support
    val clientRect = getBoundingClientRect(element, includeScale = true, isFixedStrategy = strategy == Strategy.Fixed)
    val top = clientRect.top + element.asInstanceOf[dom.HTMLElement].clientTop
    val left = clientRect.left + element.asInstanceOf[dom.HTMLElement].clientLeft
    val scale = if (element.isInstanceOf[dom.HTMLElement]) getScale(element) else Coords(1, 1)
    val width = element.asInstanceOf[dom.HTMLElement].clientWidth * scale.x
    val height = element.asInstanceOf[dom.HTMLElement].clientHeight * scale.y
    val x = left * scale.x
    val y = top * scale.y

    Rect(x, y, width, height)
  }

  /** Get client rect from a clipping ancestor.
    *
    * Handles different types of clipping ancestors:
    *   - 'viewport': viewport rect
    *   - 'document': document rect
    *   - Element: inner bounding client rect
    *   - Rect object: custom rect with visual offset adjustment
    */
  private def getClientRectFromClippingAncestor(
    element: dom.Element,
    clippingAncestor: Either[dom.Element, Either[String, Rect]],
    strategy: Strategy
  ): ClientRectObject = {
    val rect: Rect = clippingAncestor match {
      case Right(Left("viewport")) => getViewportRect(element, strategy)
      case Right(Left("document")) =>
        val docElement = getDocumentElement(element)
        getDocumentRect(docElement.asInstanceOf[dom.HTMLElement])
      case Left(ancestor)           => getInnerBoundingClientRect(ancestor, strategy)
      case Right(Right(customRect)) =>
        // Custom rect object (e.g., from VirtualElement or custom boundary)
        val visualOffsets = getVisualOffsets(Some(element))
        Rect(
          x = customRect.x - visualOffsets.x,
          y = customRect.y - visualOffsets.y,
          width = customRect.width,
          height = customRect.height
        )
      case Right(Left(_)) =>
        // Fallback for unknown string values
        getViewportRect(element, strategy)
    }

    Utils.rectToClientRect(rect)
  }

  /** Check if element has fixed position ancestor. */
  private def hasFixedPositionAncestor(element: dom.Element, stopNode: dom.Node): Boolean = {
    val parentNode = Utils.getParentNode(element)
    if (
      parentNode == null || parentNode == stopNode ||
      !parentNode.isInstanceOf[dom.Element] ||
      isLastTraversableNode(parentNode)
    ) {
      return false
    }

    dom.window.getComputedStyle(parentNode.asInstanceOf[dom.Element]).position == "fixed" ||
    hasFixedPositionAncestor(parentNode.asInstanceOf[dom.Element], stopNode)
  }

  /** Get clipping element ancestors.
    *
    * This function is expensive, so it uses a cache that is injected via the platform's _c field. The cache is created in computePosition
    * and lives only for a single positioning calculation.
    *
    * @param element
    *   The element to get clipping ancestors for
    * @param cache
    *   Optional cache map for storing/retrieving results
    * @return
    *   Sequence of clipping element ancestors
    */
  def getClippingElementAncestors(
    element: dom.Element,
    cache: Option[ClippingCache] = None
  ): Seq[dom.Element] = {
    // Check cache first
    cache.flatMap(_.get(element)) match {
      case Some(cachedResult) =>
        // Return cached result
        cachedResult
      case None =>
        // Compute result
        var result = Utils
          .getOverflowAncestors(element)
          .filter(el => el.isInstanceOf[dom.Element] && getNodeName(el.asInstanceOf[dom.Node]) != "body")
          .map(_.asInstanceOf[dom.Element])

        var currentContainingBlockComputedStyle: Option[dom.CSSStyleDeclaration] = None
        val elementIsFixed = dom.window.getComputedStyle(element).position == "fixed"
        var currentNode: dom.Node = if (elementIsFixed) Utils.getParentNode(element) else element

        while (
          currentNode != null && currentNode.isInstanceOf[dom.Element] &&
          !isLastTraversableNode(currentNode)
        ) {
          val elem = currentNode.asInstanceOf[dom.Element]
          val computedStyle = dom.window.getComputedStyle(elem)
          val currentNodeIsContaining = isContainingBlock(elem)

          if (!currentNodeIsContaining && computedStyle.position == "fixed") {
            currentContainingBlockComputedStyle = None
          }

          val shouldDropCurrentNode = if (elementIsFixed) {
            !currentNodeIsContaining && currentContainingBlockComputedStyle.isEmpty
          } else {
            (!currentNodeIsContaining &&
              computedStyle.position == "static" &&
              currentContainingBlockComputedStyle.exists(s => s.position == "absolute" || s.position == "fixed")) ||
            (isOverflowElement(elem) &&
              !currentNodeIsContaining &&
              hasFixedPositionAncestor(element, currentNode))
          }

          if (shouldDropCurrentNode) {
            result = result.filterNot(_ == currentNode)
          } else {
            currentContainingBlockComputedStyle = Some(computedStyle)
          }

          currentNode = Utils.getParentNode(currentNode)
        }

        // Store in cache before returning
        cache.foreach(_.put(element, result))

        result
    }
  }

  /** Get clipping rect for an element.
    *
    * Gets the maximum area that the element is visible in due to any number of clipping ancestors.
    *
    * Matches TypeScript implementation from @floating-ui/dom/src/platform/getClippingRect.ts
    *
    * @param element
    *   The element to get clipping rect for
    * @param boundary
    *   The clipping boundary - can be "clippingAncestors", Element, Array[Element], or Rect
    * @param rootBoundary
    *   The root clipping boundary - "viewport", "document", or custom Rect
    * @param strategy
    *   The positioning strategy
    * @param cache
    *   Optional cache for getClippingElementAncestors
    * @return
    *   The clipping rect
    */
  def getClippingRect(
    element: dom.Element,
    boundary: Boundary,
    rootBoundary: RootBoundary,
    strategy: Strategy,
    cache: Option[ClippingCache] = None
  ): Rect = {
    // Convert boundary to internal representation for type-safe pattern matching
    val boundaryInternal = BoundaryInternal.fromBoundary(boundary)

    // Determine element clipping ancestors based on boundary
    // Matches TypeScript: boundary === 'clippingAncestors' ? isTopLayer(element) ? [] : getClippingElementAncestors(element, this._c) :
    // [].concat(boundary)
    val elementClippingAncestors: Seq[Either[dom.Element, Either[String, Rect]]] =
      boundaryInternal match {
        case BoundaryInternal.ClippingAncestors =>
          if (isTopLayer(element)) {
            Seq.empty
          } else {
            // Pass cache to getClippingElementAncestors
            getClippingElementAncestors(element, cache).map(e => Left(e))
          }

        case BoundaryInternal.Element(el) =>
          // Single element boundary
          Seq(Left(el))

        case BoundaryInternal.Elements(arr) =>
          // Array of elements - convert js.Array to Seq
          arr.toSeq.map(e => Left(e))

        case BoundaryInternal.CustomRect(rect) =>
          // Custom rect boundary
          Seq(Right(Right(rect)))
      }

    // Convert rootBoundary to internal representation
    val rootBoundaryInternal = RootBoundaryInternal.fromRootBoundary(rootBoundary)

    // Add root boundary to the list
    // Matches TypeScript: [...elementClippingAncestors, rootBoundary]
    val rootBoundaryElement: Either[dom.Element, Either[String, Rect]] = rootBoundaryInternal match {
      case RootBoundaryInternal.Viewport         => Right(Left("viewport"))
      case RootBoundaryInternal.Document         => Right(Left("document"))
      case RootBoundaryInternal.CustomRect(rect) => Right(Right(rect))
    }

    val clippingAncestors = elementClippingAncestors :+ rootBoundaryElement
    val firstClippingAncestor = clippingAncestors.head

    // Reduce over all clipping ancestors to find the intersection of all clipping rects
    // Matches TypeScript: clippingAncestors.reduce((accRect, clippingAncestor) => {...}, getClientRectFromClippingAncestor(...))
    val clippingRect = clippingAncestors.foldLeft(
      getClientRectFromClippingAncestor(element, firstClippingAncestor, strategy)
    ) { (accRect, clippingAncestor) =>
      val rect = getClientRectFromClippingAncestor(element, clippingAncestor, strategy)

      // Mutate accRect in TypeScript, but we create a new object in Scala
      // accRect.top = max(rect.top, accRect.top)
      // accRect.right = min(rect.right, accRect.right)
      // accRect.bottom = min(rect.bottom, accRect.bottom)
      // accRect.left = max(rect.left, accRect.left)
      ClientRectObject(
        top = math.max(rect.top, accRect.top),
        right = math.min(rect.right, accRect.right),
        bottom = math.min(rect.bottom, accRect.bottom),
        left = math.max(rect.left, accRect.left),
        width = 0, // Will be recalculated
        height = 0,
        x = 0,
        y = 0
      )
    }

    // Return final rect with calculated width and height
    // Matches TypeScript: {width: clippingRect.right - clippingRect.left, height: clippingRect.bottom - clippingRect.top, x:
    // clippingRect.left, y: clippingRect.top}
    Rect(
      x = clippingRect.left,
      y = clippingRect.top,
      width = clippingRect.right - clippingRect.left,
      height = clippingRect.bottom - clippingRect.top
    )
  }
}
