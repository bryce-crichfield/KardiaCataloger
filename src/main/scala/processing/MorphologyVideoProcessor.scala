package org.bpc
package processing

import org.jocl.CL._
import org.jocl._

import java.awt.image.BufferedImage
import scala.concurrent.Future


class MorphologyVideoProcessor(val service: ImageProcessingService) extends VideoProcessor {
    private val kernel = ImageKernel.load("/morph.cl", service.context).get
    var enabled: Boolean = false
    var radius: Int = 1
    var mode: String = "None"

    def enqueue(): Unit = {
        if (!enabled) {
            return Future.successful(())
        }

        kernel.setArgInt(0, radius)
        kernel.setArgInt(1, modeToInt(mode))
        service.enqueue(kernel)
    }

    private def modeToInt(mode: String): Int = {
        mode match {
            case "Erode" => 0
            case "Dilate" => 1
            case _ => -1
        }
    }

}


