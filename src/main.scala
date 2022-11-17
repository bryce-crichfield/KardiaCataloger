import scala.swing.*
import scala.swing.event.*
import java.awt.image.BufferedImage
import java.awt.{Color, Graphics2D, Point, geom, Component, Dimension}
import javax.imageio.ImageIO
import javax.swing.Timer
import java.awt.event.ActionListener
import java.io.File

import net.sourceforge.tess4j.*
import org.bytedeco.tesseract.global.tesseract

class ControlPanel(adapter: ControlAdapter)
    extends BoxPanel(Orientation.Vertical) {
  background = Color.BLACK

  val button = new Button("Capture") {
    reactions += { case ButtonClicked(_) =>
      adapter.capture()
    }
  }

  val slider_alpha = new Slider() {
    min = 0
    max = 255
    value = 0
    reactions += {
      case _: ValueChanged => {
        adapter.setAlpha(value)
      }
    }
  }

  val slider_beta = new Slider() {
    min = 0
    max = 255
    value = 0
    reactions += {
      case _: ValueChanged => {
        adapter.setBeta(value)
      }
    }
  }

  val slider_min = new Slider() {
    min = 0
    max = 255
    value = 0
    reactions += {
      case _: ValueChanged => {
        adapter.setMin(value)
      }
    }
  }

  val slider_max = new Slider() {
    min = 0
    max = 255
    value = 0
    reactions += {
      case _: ValueChanged => {
        adapter.setMax(value)
      }
    }
  }
  contents ++= Seq(button, slider_alpha, slider_beta, slider_min, slider_max)
}

class ControlAdapter(
    tesseract: Tesseract,
    video_feed: VideoFeed,
    processed: ProcessedFeed
) {
  private val alpha: Array[Char] = "abcdefghijklmnopqrstuvwxyz".toCharArray()
  def capture(): Unit = {
    processed { image =>
      val text = tesseract.doOCR(image)
      val clean = text
        .toLowerCase()
        .toCharArray()
        .filter { char =>
          alpha.contains(char)
        }
        .mkString
      println(clean)
    }
  }

  def setAlpha(value: Int): Unit = {
    processed.alpha = value
  }

  def setBeta(value: Int): Unit = {
    processed.beta = value
  }

  def setMin(value: Int): Unit = {
    processed.min = value
  }

  def setMax(value: Int): Unit = {
    processed.max = value
  }
}

object Main extends SwingApplication {
  val tesseract = new Tesseract()
  tesseract.setDatapath("tessdata")

  val webcam = new Webcam()
  val processed_feed = new ProcessedFeed(webcam)

  val adapter = new ControlAdapter(tesseract, webcam, processed_feed)
  def top(): Frame = new MainFrame {
    title = "Card Scanner"
    contents = new BoxPanel(Orientation.Horizontal) {
      contents += new ControlPanel(adapter)
      contents += new VideoPanel(webcam, 30)
      contents += new VideoPanel(processed_feed, 30)
    }
  }

  override def startup(args: Array[String]): Unit = {
    println("Starting Application...")
    val frame = top()
    if (frame.size == new Dimension(0, 0)) frame.pack()
    frame.visible = true
    frame.centerOnScreen()
  }

  override def shutdown(): Unit = {
    processed_feed.close()
    webcam.close()
  }

}
