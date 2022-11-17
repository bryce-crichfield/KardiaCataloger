import org.bytedeco.javacv.{
  OpenCVFrameGrabber,
  Frame as OpenCVFrame
}
import java.awt.image.BufferedImage
import javax.swing.Timer
import scala.swing.*
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import org.bytedeco.javacv.Java2DFrameConverter

trait VideoFeed {
  def apply(f: BufferedImage => Unit): Unit
  def close(): Unit
}


class Webcam extends VideoFeed {
  private val peer = new OpenCVFrameGrabber(0)
  val converter = new Java2DFrameConverter()
  peer.start()
  private var image = peer.grab()
  // Will Automatically refresh every ~33 ms and store captured frame
  new Timer(util.rate(30), Swing.ActionListener {
    _ => peer.grab()
  }).start()

  override def apply(f: BufferedImage => Unit): Unit = {
    for {
      frame <- Option(image)
      buffer <- Option(converter.convert(frame))
    } yield f(buffer)
  }

  override def close(): Unit = {
    peer.close()
  }
}

class VideoPanel(video_feed: VideoFeed, refresh_rate: Int) extends Panel {
  preferredSize = new Dimension(250, 250)
  new Timer(util.rate(refresh_rate), Swing.ActionListener {
    _ => this.repaint()
  }).start()
  override protected def paintComponent(g: Graphics2D): Unit = {
    video_feed { img => 
      g.drawImage(img, 0, 0, this.size.width, this.size.height, null)  
    }
  }
}
