package org.bpc
package processing

trait VideoProcessor {
    val service: ImageProcessingService
    def enqueue(): Unit
}
