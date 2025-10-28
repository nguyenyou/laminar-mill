package io.github.nguyenyou.floatingUI.integration.helpers

import org.scalajs.dom

/** Utility functions for creating and managing test DOM elements in Playwright integration tests.
  *
  * These helpers simplify the setup and cleanup of DOM elements needed for testing
  * Floating UI positioning behavior in a real browser environment.
  */
object TestHelpers {

  /** Creates a positioned reference element with specified dimensions and position.
    *
    * The element is absolutely positioned and appended to document.body.
    *
    * @param left Left position in pixels
    * @param top Top position in pixels
    * @param width Width in pixels
    * @param height Height in pixels
    * @param id Optional element ID for debugging
    * @return The created HTMLElement
    */
  def createReferenceElement(
    left: Double,
    top: Double,
    width: Double,
    height: Double,
    id: String = "reference"
  ): dom.HTMLElement = {
    val element = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    element.id = id
    element.style.position = "absolute"
    element.style.left = s"${left}px"
    element.style.top = s"${top}px"
    element.style.width = s"${width}px"
    element.style.height = s"${height}px"
    element.style.backgroundColor = "#ddd"
    dom.document.body.appendChild(element)
    element
  }

  /** Creates a floating element with specified dimensions.
    *
    * The element is initially positioned absolutely at (0, 0) and appended to document.body.
    * Floating UI will calculate and apply the correct position.
    *
    * @param width Width in pixels
    * @param height Height in pixels
    * @param id Optional element ID for debugging
    * @return The created HTMLElement
    */
  def createFloatingElement(
    width: Double,
    height: Double,
    id: String = "floating"
  ): dom.HTMLElement = {
    val element = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    element.id = id
    element.style.position = "absolute"
    element.style.width = s"${width}px"
    element.style.height = s"${height}px"
    element.style.backgroundColor = "#333"
    element.style.color = "#fff"
    element.textContent = "Tooltip"
    dom.document.body.appendChild(element)
    element
  }

  /** Removes elements from the DOM.
    *
    * Use this in test cleanup (finally blocks) to ensure a clean state between tests.
    *
    * @param elements Elements to remove
    */
  def cleanup(elements: dom.Element*): Unit = {
    elements.foreach { element =>
      if (element.parentNode != null) {
        element.parentNode.removeChild(element)
      }
    }
  }

  /** Sets the viewport size for testing.
    *
    * This is useful for testing viewport-aware positioning behavior.
    * Note: In Playwright, the viewport size is typically set via browser launch options,
    * but this can be used for dynamic resizing during tests.
    *
    * @param width Viewport width in pixels
    * @param height Viewport height in pixels
    */
  def setViewportSize(width: Int, height: Int): Unit = {
    // Note: This only works in some browser environments
    // For Playwright, viewport size is typically set in launch options
    dom.window.resizeTo(width, height)
  }

  /** Creates a scrollable container element.
    *
    * Useful for testing scroll-aware positioning.
    *
    * @param width Container width in pixels
    * @param height Container height in pixels
    * @param contentHeight Height of scrollable content in pixels
    * @param id Optional element ID for debugging
    * @return The created HTMLElement
    */
  def createScrollContainer(
    width: Double,
    height: Double,
    contentHeight: Double,
    id: String = "scroll-container"
  ): dom.HTMLElement = {
    val container = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    container.id = id
    container.style.position = "relative"
    container.style.width = s"${width}px"
    container.style.height = s"${height}px"
    container.style.overflow = "auto"
    container.style.border = "1px solid #ccc"
    
    // Create inner content that's taller than container
    val content = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    content.style.height = s"${contentHeight}px"
    container.appendChild(content)
    
    dom.document.body.appendChild(container)
    container
  }
}

