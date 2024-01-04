package org.bpc
package processing


class TransformVideoProcessor(val service: ImageProcessingService) extends VideoProcessor {
    private val kernel = ImageKernel.load("/transform.cl", service.context).get

    var tUp: Float = 0
    var tRight: Float = 0
    var tDown: Float = 0
    var tLeft: Float = 0
    var rotation: Float = 0
    var skewX: Float = 0
    var skewY: Float = 0

    def enqueue(): Unit = {
        kernel.setArgFloat(0, tUp, tRight, tDown, tLeft)
        kernel.setArgFloat(1, rotation)
        kernel.setArgFloat(2, skewX, skewY)
        service.enqueue(kernel)
    }
}
