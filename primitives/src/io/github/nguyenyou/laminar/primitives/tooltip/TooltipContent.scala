package io.github.nguyenyou.laminar.primitives.tooltip

import io.github.nguyenyou.laminar.api.L.*
import io.github.nguyenyou.laminar.api.L
import io.github.nguyenyou.laminar.nodes.{ChildNode, DetachedRoot}
import io.github.nguyenyou.laminar.modifiers.RenderableNode
import org.scalajs.dom
import io.github.nguyenyou.facades.floatingui.FloatingUIDOM.*
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import scala.scalajs.js.Thenable.Implicits.thenable2future
import scala.util.Failure
import scala.util.Success
import scala.scalajs.js
import io.github.nguyenyou.laminar.primitives.base.*

class TooltipContent(
  val root: TooltipRoot
) extends Component {
  private var mounted = false

  def render() = div(
    onMountCallback { ctx =>
      println("MOUNTED > CONTENT")
    }
  )

  def setChildren(children: Seq[ChildNode.Base]) = {
    element.amend(
      children
    )
  }

  def updateChildren(values: Source[Seq[ChildNode.Base]]) = {
    element.amend(
      children <-- values.toObservable
    )
  }
}

object TooltipContent extends HasClassNameProp[TooltipContent] {
  object ChildrenProp extends ComponentProp[Seq[ChildNode.Base], TooltipContent] {
    private[primitives] def setProp(component: TooltipContent, value: Seq[ChildNode.Base]): Unit = {
      component.setChildren(value)
    }

    private[primitives] def updateProp(component: TooltipContent, values: Source[Seq[ChildNode.Base]]): Unit = {
      component.updateChildren(values)
    }
  }

  object Props extends PropSelector[TooltipContent] {
    lazy val className: ClassNameProp.type = ClassNameProp
  }

  def apply(mods: Props.Selector*)(children: ChildNode.Base*)(using root: TooltipRoot, portal: TooltipPortal): TooltipContent = {
    val resolvedMods: Seq[ComponentModifier[TooltipContent]] = mods.map(_(Props))
    val tooltipContent = new TooltipContent(root = root)
    resolvedMods.foreach(_(tooltipContent))

    // children
    tooltipContent.setChildren(children)

    root.setContent(tooltipContent)

    // portal add contetn
    portal.element.amend(
      tooltipContent
    )

    tooltipContent
  }
}
