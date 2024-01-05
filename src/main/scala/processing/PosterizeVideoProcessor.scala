package org.bpc
package processing

import org.jocl.CL._
import org.jocl._

import java.awt.image.BufferedImage
import scala.concurrent.Future


// Model/Controller
class PosterizeVideoProcessor(val service: ImageProcessingService) extends VideoProcessor {
    private val kernel = ImageKernel.load("/posterize.cl", service.context).get

    var enabled: Boolean = false
    var alpha: Double = 1
    var beta: Double = 1
    var invert: Boolean = false

    def enqueue(): Unit = {
        if (!enabled) {
            return Future.successful(())
        }

        kernel.setArgInt(0, (alpha * 255).toInt)
        kernel.setArgInt(1, (beta * 255).toInt)
        kernel.setArgInt(2, Util.toInt(invert))
        service.enqueue(kernel)
    }

    def close(): Unit = {
    }
}


