package org.bpc
package video

import org.bpc.processing.{ImageProcessingService, VideoProcessor}

import java.awt.image.BufferedImage
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}

class ProcessedVideoFeed(input: VideoFeed, service: ImageProcessingService, processors: VideoProcessor*) extends VideoFeed {
    override def getFrame(): Future[BufferedImage] = {
        val frame = Await.result(input.getFrame(), Duration.Inf)
        processors.foreach(_.enqueue())
        service.execute(frame)

        Future.successful(frame)
    }
}
