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

  // ============================================================================
  // CSS and Dimensions
  // ============================================================================

  /** Get CSS dimensions of an element.
    *
    * Returns width, height, and a flag indicating if fallback to offset dimensions was used.
    */
  def getCssDimensions(element: dom.Element): (Double, Double, Boolean) = {
    if (!element.isInstanceOf[dom.HTMLElement]) {
      val rect = element.getBoundingClientRect()
      return (rect.width, rect.height, false)
    }

    val htmlElement = element.asInstanceOf[dom.HTMLElement]
    val css = dom.window.getComputedStyle(htmlElement)

    var width = css.width.replace("px", "").toDoubleOption.getOrElse(0.0)
    var height = css.height.replace("px", "").toDoubleOption.getOrElse(0.0)

    val offsetWidth = htmlElement.offsetWidth.toDouble
    val offsetHeight = htmlElement.offsetHeight.toDouble

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

  /** Check if an element is a containing block. */
  def isContainingBlock(element: dom.Element): Boolean = {
    val css = dom.window.getComputedStyle(element)
    // Simplified check - full implementation would check for more properties
    css.transform != "none" ||
    css.perspective != "none" ||
    css.getPropertyValue("will-change") == "transform" ||
    css.getPropertyValue("will-change") == "perspective" ||
    css.getPropertyValue("filter") != "none" ||
    css.getPropertyValue("backdrop-filter") != "none"
  }

  /** Check if element is in top layer (dialog, fullscreen). */
  def isTopLayer(element: dom.Element): Boolean = {
    if (!element.isInstanceOf[dom.HTMLElement]) return false
    val htmlElement = element.asInstanceOf[dom.HTMLElement]
    val nodeName = htmlElement.nodeName.toLowerCase
    // Simplified check
    nodeName == "dialog" || {
      val css = dom.window.getComputedStyle(htmlElement)
      css.position == "fixed" && css.getPropertyValue("inset") == "0px"
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
      if (strategy == "fixed") {
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

  /** Get visual offsets (for visual viewport). */
  def getVisualOffsets(element: dom.Element): Coords = {
    val win = dom.window
    val visualViewport = win.asInstanceOf[js.Dynamic].visualViewport

    if (js.isUndefined(visualViewport) || visualViewport == null) {
      return Coords(0, 0)
    }

    Coords(
      visualViewport.offsetLeft.asInstanceOf[Double],
      visualViewport.offsetTop.asInstanceOf[Double]
    )
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
    val isFixed = strategy == "fixed"

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
        val offsetRect = Utils.getBoundingClientRect(htmlOffsetParent)
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

  /** Get rect relative to offset parent. */
  def getRectRelativeToOffsetParent(
    element: dom.Element,
    offsetParent: dom.EventTarget,
    strategy: Strategy
  ): Rect = {
    val clientRect = Utils.getBoundingClientRect(element)
    val rect = Utils.rectToClientRect(Rect(clientRect.x, clientRect.y, clientRect.width, clientRect.height))

    if (offsetParent == dom.window) {
      return Rect(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top)
    }

    // Convert from viewport-relative to offset-parent-relative
    if (!offsetParent.isInstanceOf[dom.Element]) {
      return Rect(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top)
    }

    val offsetParentElement = offsetParent.asInstanceOf[dom.Element]
    val offsetParentClientRect = Utils.getBoundingClientRect(offsetParentElement)
    val scale = getScale(offsetParentElement)

    Rect(
      x = (rect.left - offsetParentClientRect.x) / scale.x,
      y = (rect.top - offsetParentClientRect.y) / scale.y,
      width = (rect.right - rect.left) / scale.x,
      height = (rect.bottom - rect.top) / scale.y
    )
  }

  // ============================================================================
  // Clipping Rect Calculation
  // ============================================================================

  /** Get inner bounding client rect (subtracting scrollbars). */
  private def getInnerBoundingClientRect(element: dom.Element, strategy: Strategy): Rect = {
    val clientRect = Utils.getBoundingClientRect(element)
    val top = clientRect.top + element.asInstanceOf[dom.HTMLElement].clientTop
    val left = clientRect.left + element.asInstanceOf[dom.HTMLElement].clientLeft
    val scale = if (element.isInstanceOf[dom.HTMLElement]) getScale(element) else Coords(1, 1)
    val width = element.asInstanceOf[dom.HTMLElement].clientWidth * scale.x
    val height = element.asInstanceOf[dom.HTMLElement].clientHeight * scale.y
    val x = left * scale.x
    val y = top * scale.y

    Rect(x, y, width, height)
  }

  /** Get client rect from a clipping ancestor. */
  def getClientRectFromClippingAncestor(
    element: dom.Element,
    clippingAncestor: Either[dom.Element, String],
    strategy: Strategy
  ): ClientRectObject = {
    val rect: Rect = clippingAncestor match {
      case Right("viewport") => getViewportRect(element, strategy)
      case Right("document") =>
        val docElement = getDocumentElement(element)
        if (docElement.isInstanceOf[dom.HTMLElement]) {
          getDocumentRect(docElement.asInstanceOf[dom.HTMLElement])
        } else {
          getViewportRect(element, strategy)
        }
      case Left(ancestor) => getInnerBoundingClientRect(ancestor, strategy)
      case Right(other)   =>
        // Custom rect object
        val visualOffsets = getVisualOffsets(element)
        Rect(0 - visualOffsets.x, 0 - visualOffsets.y, 0, 0)
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

  /** Get clipping element ancestors. */
  def getClippingElementAncestors(element: dom.Element): Seq[dom.Element] = {
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

    result
  }

  /** Get clipping rect for an element. */
  def getClippingRect(
    element: dom.Element,
    boundary: String,
    rootBoundary: String,
    strategy: Strategy
  ): Rect = {
    val elementClippingAncestors: Seq[Either[dom.Element, String]] =
      if (boundary == "clippingAncestors") {
        if (isTopLayer(element)) {
          Seq.empty
        } else {
          getClippingElementAncestors(element).map(Left(_))
        }
      } else {
        Seq(Left(dom.document.querySelector(boundary).asInstanceOf[dom.Element]))
      }

    val clippingAncestors = elementClippingAncestors :+ Right(rootBoundary)
    val firstClippingAncestor = clippingAncestors.head

    val clippingRect = clippingAncestors.foldLeft(
      getClientRectFromClippingAncestor(element, firstClippingAncestor, strategy)
    ) { (accRect, clippingAncestor) =>
      val rect = getClientRectFromClippingAncestor(element, clippingAncestor, strategy)

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

    Rect(
      x = clippingRect.left,
      y = clippingRect.top,
      width = clippingRect.right - clippingRect.left,
      height = clippingRect.bottom - clippingRect.top
    )
  }
}
