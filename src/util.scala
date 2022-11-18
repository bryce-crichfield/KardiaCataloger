import java.awt.image.BufferedImage
import org.jocl.Pointer
import java.awt.image.DataBufferInt
import java.awt.Graphics2D

class Chronometer() {
  private val start: Long = System.nanoTime()
  private var (t0, t1, td) = (start, 0L, 0L)

  def tick(f: Double => Unit): Unit = {
    t1 = System.nanoTime()
    td = t1 - t0
    t0 = t1
    f(td / 1e9.toDouble)
  }
}

object util {
  inline def toInt(bool: Boolean): Int = if bool then 1 else 0

  inline def rate(fps: Int): Int = 1000 / fps

  def time(operation: => Unit, msg: String, runs: Int): Unit = {
    val start = System.nanoTime()
    for (_ <- 0 until runs) {
      operation
    }
    val average = (System.nanoTime() - start) / runs.toDouble
    println(f"${msg}:\t${average / 1e6}ms")
  }

  extension (image: BufferedImage)

    def graphics(f: Graphics2D => Unit): Unit = {
      val graphics = image.createGraphics()
      f(graphics)
      graphics.dispose()
    }

    def copy(format: Int = BufferedImage.TYPE_INT_RGB): BufferedImage = {
      val buffer = new BufferedImage(
        image.getWidth(),
        image.getHeight(),
        BufferedImage.TYPE_INT_RGB
      )
      buffer.graphics(_.drawImage(image, 0, 0, null))
      buffer
    }

    def pointer(): Pointer = {
      Pointer.to(
        image
          .getRaster()
          .getDataBuffer()
          .asInstanceOf[DataBufferInt]
          .getData()
      )
    }
}
