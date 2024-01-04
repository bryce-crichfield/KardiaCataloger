package org.bpc
package processing

class DenoiseVideoProcessor(val service: ImageProcessingService) extends VideoProcessor {
    private val kernel = ImageKernel.load("/denoise.cl", service.context).get

    var patchSize: Int = 1
    var windowSize: Int = 1
    var filterStrength: Double = 1

    def enqueue(): Unit = {
        kernel.setArgInt(0, patchSize)
        kernel.setArgInt(1, windowSize)
        kernel.setArgFloat(2, filterStrength.toFloat)
        service.enqueue(kernel)
    }
}

