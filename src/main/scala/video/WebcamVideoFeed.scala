package org.bpc
package video

import Util.BufferedImageExtension

import org.bytedeco.javacv.{Java2DFrameConverter, OpenCVFrameGrabber}

import java.awt.image.BufferedImage
import java.util.concurrent.{LinkedBlockingDeque, LinkedBlockingQueue}
import javax.swing.Timer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing._
import scala.util.Try

// TODO: figure out why a single instantiation of the webcam
// requires ~450 MB
class WebcamVideoFeed extends VideoFeed {
    // find the first webcam

    private val peer = new OpenCVFrameGrabber(-1)
    private val imageQueue = new LinkedBlockingDeque[BufferedImage]()
    peer.start()

    val timer = new Timer(1000 / 30, Swing.ActionListener { _ =>
        Try {
            val frame = peer.grab()
            val converter = new Java2DFrameConverter()
            val converted = converter.convert(frame)
            // Note: this conversion is need because OpenCV works in BGR format
            // OpenCL will expect RGB format, so in order to mitigate later
            // copies and downstream complexity, 3BYTE_RGB is chosen as the standard
            val out = converted.copy(BufferedImage.TYPE_INT_RGB)
            frame.close()
            converted.flush()
            imageQueue.put(out)

            // if there are more than 5 images in the queue, drop the oldest one
            if (imageQueue.size() > 5) {
                imageQueue.poll()
            }
        }
    })

    timer.start()

    override def pollFrame(): Future[BufferedImage] = Future {
        // if there is a head in the queue, return a copy of it without removing it
        // otherwise, block until there is a head in the queue

        val head = imageQueue.take()
        val copy = head.copy()
        imageQueue.putFirst(head)
        copy
    }

    override def takeFrame(): Future[BufferedImage] = Future {
        // if there is a head in the queue, return a copy of it and remove it
        // otherwise, block until there is a head in the queue

        val head = imageQueue.take()
        val copy = head.copy()
        head.flush()
        copy
    }

    def close(): Unit = {
        // stop the thread
        peer.close()
    }
}
