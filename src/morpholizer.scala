import scala.swing.*
import scala.swing.Swing.*
import scala.swing.event.*
import java.awt.image.BufferedImage
import java.awt.{Color, Graphics2D, Point, geom, Component, Dimension}
import javax.imageio.ImageIO
import javax.swing.Timer
import java.awt.event.ActionListener
import util.rate
import org.jocl.*
import org.jocl.CL.*
import util.toInt
import javax.swing.border.BevelBorder

class Morpholizer() {
  var enabled: Boolean = false
  var radius: Int = 1
  var mode: String = "None"
  private val kernel = new ImageKernel("kernel/morph.cl")
  def run(image: BufferedImage): Unit = {
    if !enabled then return
    kernel.run(image, image) { k =>
      clSetKernelArg(k, 2, Sizeof.cl_int, Pointer.to(Array(radius)))
      clSetKernelArg(k, 3, Sizeof.cl_int, Pointer.to(Array(modeToInt(mode))))
    }
  }

  private def modeToInt(mode: String): Int = {
    mode match {
      case "Erode" => 0
      case "Dilate" => 1
      case _ => -1
    }
  } 

  export kernel.close
}

// View
class MorpholizerPanel(morpholizer: Morpholizer)
    extends BoxPanel(Orientation.Vertical) {
  border = TitledBorder(EtchedBorder(Raised), "Morpholizer Module")
  background = Color.GRAY

  val toggle_enable = new ToggleButton("On/Off") {
    selected = true
    reactions += { case ButtonClicked(_) =>
      morpholizer.enabled = !morpholizer.enabled
    }
  }

  val combo_radius = new ComboBox[Int](Seq(5, 15, 25, 35)) {
    listenTo(selection)
    reactions += { case _: SelectionChanged =>
      morpholizer.radius = selection.item
    }
  }

  val combo_mode = new ComboBox[String](Seq("Erode", "Dilate", "Open", "Close")) {
    listenTo(selection)
    reactions += { case _: SelectionChanged =>
      morpholizer.mode = selection.item
    }
  }

  val toggle_panel = new GridPanel(1, 3) {
    contents += toggle_enable
    contents += HStrut(50)
  }
  contents ++= Seq(toggle_panel, combo_radius, combo_mode)
}
