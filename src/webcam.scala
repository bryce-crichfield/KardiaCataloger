import org.bytedeco.javacv.{OpenCVFrameGrabber, Frame as OpenCVFrame}
import java.awt.Color
import java.awt.image.BufferedImage

import javax.swing.Timer
import scala.swing.*
import scala.swing.Swing.*
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import org.bytedeco.javacv.Java2DFrameConverter
import util.{copy, graphics}
trait VideoFeed {
  def apply(f: BufferedImage => Unit): Unit
  def close(): Unit
}

// TODO: figure out why a single instantiation of the webcam
// requires ~450 MB
class Webcam extends VideoFeed {
  private val peer = new OpenCVFrameGrabber(0)
  val converter = new Java2DFrameConverter()
  peer.start()
  private var image = peer.grab()
  // Will Automatically refresh every ~33 ms and store captured frame
  new Timer(
    util.rate(30),
    Swing.ActionListener { _ =>
      peer.grab()
    }
  ).start()

  override def apply(f: BufferedImage => Unit): Unit = {
    for {
      frame <- Option(image)
      buffer <- Option {
        val converted = converter.convert(frame)
        // Note: this conversion is need because OpenCV works in BGR format
        // OpenCL will expect RGB format, so in order to mitigate later
        // copies and downstream complexity, 3BYTE_RGB is chosen as the standard
        val out = converted.copy(BufferedImage.TYPE_INT_RGB)
        frame.close()
        converted.flush()
        out
      }
    } yield f(buffer)
  }

  override def close(): Unit = {
    peer.close()
  }
}

class ProcessedFeed(
    input: VideoFeed,
    posterizer: Posterizer,
    morpholizer: Morpholizer
) extends VideoFeed {

  override def apply(function: BufferedImage => Unit): Unit = {
    input { image =>
      posterizer.run(image)
      morpholizer.run(image)
      function(image)
    }
  }

  override def close(): Unit = {
    input.close()
  }
}

class VideoPanel(video_feed: VideoFeed, refresh_rate: Int, id: String)
    extends BoxPanel(Orientation.NoOrientation) {
  border = TitledBorder(EtchedBorder(Raised), f"$id")
  background = Color.GRAY
  contents += new Panel {
    private val chrono = new Chronometer()
    preferredSize = new Dimension(250, 250)
    new Timer(
      util.rate(refresh_rate),
      Swing.ActionListener { _ =>
        this.repaint()
      }
    ).start()
    override protected def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)
      video_feed { img =>
        g.drawImage(img, 0, 0, this.size.width, this.size.height, null)
        chrono.tick { delta =>
          val fps = (1000 / (1000 * delta)).toInt
          val size = 20
          g.setColor(Color.GREEN)
          g.setFont(new Font("SansSerif", java.awt.Font.BOLD, size))
          g.drawString(fps.toString(), 0, size)
        }
      }
    }
  }

}
