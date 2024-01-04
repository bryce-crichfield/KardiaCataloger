package org.bpc

import org.jocl.Pointer

import java.awt.Graphics2D
import java.awt.image.{BufferedImage, DataBufferInt}
import javax.swing.SwingWorker
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Util {
    def toInt(bool: Boolean): Int = if (bool) 1 else 0

    def rate(fps: Int): Int = 1000 / fps

    implicit class BufferedImageExtension(image: BufferedImage) {
        def copy(format: Int = BufferedImage.TYPE_INT_RGB): BufferedImage = {
            val buffer = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB)
            buffer.graphics(_.drawImage(image, 0, 0, null))
            buffer
        }

        def graphics(f: Graphics2D => Unit): Unit = {
            val graphics = image.createGraphics()
            f(graphics)
            graphics.dispose()
        }

        def pointer(): Pointer = {
            Pointer.to(image.getRaster().getDataBuffer().asInstanceOf[DataBufferInt].getData())
        }
    }

}
