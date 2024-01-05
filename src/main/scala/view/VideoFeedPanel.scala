package org.bpc
package view

import video.VideoFeed

import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference
import javax.swing.SwingWorker
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.swing.Swing.{EtchedBorder, Raised, TitledBorder}
import scala.swing._

class VideoFeedPanel(videoFeed: VideoFeed, refreshRate: Int, id: String) extends BoxPanel(Orientation.NoOrientation) {
    private val cachedImage: AtomicReference[Option[BufferedImage]] = new AtomicReference(None)

    private def getImageFromFeed(): Unit = {
        // Sends out a request for the next frame, and when it arrives, caches it and repaints the panel
        val swingWorker = new SwingWorker[BufferedImage, Unit] {
            override def doInBackground(): BufferedImage = {
                Await.result(videoFeed.pollFrame(), Duration.Inf)
            }

            override def done(): Unit = {
                val frame = get()
                cachedImage.set(Some(frame))
                repaint()
            }
        }
        swingWorker.execute()
    }

    border = TitledBorder(EtchedBorder(Raised), f"$id")

    contents += new Panel {
        private var lastTime = System.nanoTime()

        override protected def paintComponent(g: Graphics2D): Unit = {
            super.paintComponent(g)

            getImageFromFeed()

            // If there is no cached image, do nothing, otherwise draw the image
            cachedImage.get().foreach { frame =>
                // Calculate the bounds of the image such that the aspect ratio is preserved
                val (width, height) = (frame.getWidth(), frame.getHeight())
                val aspectRatio = width.toDouble / height.toDouble
                val (newWidth, newHeight) = {
                    val (width, height) = (size.width.toDouble, size.height.toDouble)
                    if (width / height > aspectRatio) {
                        (height * aspectRatio, height)
                    } else {
                        (width, width / aspectRatio)
                    }
                }

                // Draw the image in the center of the panel
                val x = (size.width.toDouble - newWidth) / 2
                val y = (size.height.toDouble - newHeight) / 2
                g.drawImage(frame, x.toInt, y.toInt, newWidth.toInt, newHeight.toInt, null)

                // Calculate and draw the FPS
                val nowTime = System.nanoTime()
                val deltaTime = nowTime - lastTime
                lastTime = nowTime
                val fps = 1e9 / deltaTime

                g.setColor(Color.GREEN)
                g.drawString(f"${fps}%.2f FPS", 10, 20)
            }
        }
    }
}
