import scala.swing.*
import scala.swing.Swing.*
import scala.swing.event.*
import java.awt.image.BufferedImage
import java.awt.{Color, Graphics2D, Point, geom, Component, Dimension}
import javax.imageio.ImageIO
import javax.swing.Timer
import java.awt.event.ActionListener
import java.io.File
import util.rate
import org.jocl.*
import org.jocl.CL.*
import util.toInt
import javax.swing.border.BevelBorder
import net.sourceforge.lept4j.Sel

// Model/Controller
class Posterizer {
  var enabled: Boolean = true
  var alpha: Int = 1
  var beta: Int = 1
  var invert: Boolean = false

  private val kernel = new ImageKernel("kernel/posterize.cl")
  def run(image: BufferedImage): Unit = {
    if !enabled then return
    kernel.run(image, image) { k =>
      clSetKernelArg(k, 2, Sizeof.cl_int, Pointer.to(Array(alpha)))
      clSetKernelArg(k, 3, Sizeof.cl_int, Pointer.to(Array(beta)))
      clSetKernelArg(k, 4, Sizeof.cl_int, Pointer.to(Array(toInt(invert))))
    }
  }
  export kernel.close
}

// View
class PosterizerPanel(posterizer: Posterizer)
    extends BoxPanel(Orientation.Vertical) {
  border = TitledBorder(EtchedBorder(Raised), "Posterize Module")
  background = Color.GRAY
  val toggle_enable = new ToggleButton("On/Off") {
    selected = true
    reactions += { case ButtonClicked(_) =>
      posterizer.enabled = !posterizer.enabled
    }
  }
  val slider_alpha = new Slider() {
    preferredSize = new Dimension(50, 35)
    def updateText(): Unit = {
      val text = f"Alpha = $value"
      border = TitledBorder(EtchedBorder(Raised), text)
    }
    paintLabels = true
    min = 1; max = 255; value = 1
    updateText()
    reactions += { case _: ValueChanged =>
      posterizer.alpha = value
      updateText()
    }
  }
  val slider_beta = new Slider() {
    preferredSize = new Dimension(50, 35)
    def updateText(): Unit = {
      val text = f"Beta = $value"
      border = TitledBorder(EtchedBorder(Raised), text)
    }
    paintLabels = true
    min = 1; max = 255; value = 1
    updateText()
    reactions += { case _: ValueChanged =>
      posterizer.beta = value
      updateText()
    }
  }
  val toggle_invert = new ToggleButton("Invert") {
    reactions += { case ButtonClicked(_) =>
      posterizer.invert = !posterizer.invert
    }
  }

  val toggle_panel = new GridPanel(1, 3) {
    contents += toggle_enable
    contents += toggle_invert
    contents += HStrut(50)
  }
  contents ++= Seq(toggle_panel, slider_alpha, slider_beta)
}
