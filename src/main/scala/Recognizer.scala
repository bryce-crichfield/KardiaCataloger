package org.bpc

import model.Database
import video.VideoFeed

import net.sourceforge.tess4j.Tesseract

import scala.concurrent.ExecutionContext.Implicits.global

class Recognizer(video_feed: VideoFeed, database: Database) {
    private val tesseract = new Tesseract()
    private val alpha: Array[Char] = "abcdefghijklmnopqrstuvwxyz-' ".toCharArray()

    // get the path to resource folder
    val path = getClass.getResource("/tessdata").getPath
    tesseract.setDatapath(path)
    var output_recognize: String = ""
    var output_database: String = ""
    var confidence: Double = 0

    def recognize(): Unit = {
        video_feed.pollFrame() .foreach { image =>
                // resize the image to a dpi of 300

                // scale the image up to 1000x1000
            val scaled = new java.awt.image.BufferedImage(1000, 1000, image.getType())
            val g = scaled.createGraphics()
            g.drawImage(image, 0, 0, 1000, 1000, null)
            g.dispose()

                val text = tesseract.doOCR(scaled)
                val clean = text
                    .toLowerCase()
                    .toCharArray()
                    .mkString
                output_recognize = clean
//                output_database = database.find(clean)
        }

    }

    def getConfidence(): Double = {
        0
    }

    def getClosestMatch(): String = {
        println( output_recognize)
        output_recognize
    }

    def close(): Unit = {
    }
}


