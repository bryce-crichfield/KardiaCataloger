package org.bpc
package video

import java.awt.image.BufferedImage
import scala.concurrent.{Future, Promise}

trait VideoFeed {
    def pollFrame(): Future[BufferedImage]
    def takeFrame(): Future[BufferedImage]
}
