package org.bpc
package view

import video.VideoFeed

import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.{SwingWorker, Timer}
import scala.concurrent.Await
import scala.swing.Swing.{EtchedBorder, Raised, TitledBorder}
import scala.swing._

class VideoFeedPanel(videoFeed: VideoFeed, refreshRate: Int, id: String) extends BoxPanel(Orientation.NoOrientation) {
    private var cachedImage: BufferedImage = null
    border = TitledBorder(EtchedBorder(Raised), f"$id")
    background = new Color(0, 0, 0, 0)
    contents += new Panel {
        private var lastTime = System.nanoTime()

        override protected def paintComponent(g: Graphics2D): Unit = {
            super.paintComponent(g)

            // Acquire the next frame from the video feed asynchronously and cache it
            //  Each time we paint the component, span a worker to get the next frame, once the frame is retrieved,
            //  cache it and repaint the panel (thus spawning another worker).
            val swingWorker = new SwingWorker[BufferedImage, Unit] {
                override def doInBackground(): BufferedImage = {
                    // await the next frame
                    Await.result(videoFeed.getFrame(), scala.concurrent.duration.Duration.Inf)
                }

                override def done(): Unit = {
                    val frame = get()
                    cachedImage = frame
                    repaint()
                }
            }
            swingWorker.execute()

            // If there is no cached image, do nothing, otherwise draw the image
            val frame = cachedImage
            if (frame != null) {
                // Calculate the bounds of the image such that the aspect ratio is preserved
                val (width, height) = (frame.getWidth(), frame.getHeight())
                val aspectRatio = width.toDouble / height.toDouble
                val (newWidth, newHeight) = {
                    val (width, height) = (this.size.width.toDouble, this.size.height.toDouble)
                    if (width / height > aspectRatio) {
                        (height * aspectRatio, height)
                    } else {
                        (width, width / aspectRatio)
                    }
                }

                // Draw the image in the center of the panel
                val x = (this.size.width.toDouble - newWidth) / 2
                val y = (this.size.height.toDouble - newHeight) / 2
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
