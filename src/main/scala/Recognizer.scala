package org.bpc

import model.Database
import video.VideoFeed

import net.sourceforge.tess4j.Tesseract

class Recognizer(video_feed: VideoFeed, database: Database) {
    val tesseract = new Tesseract()
    private val alpha: Array[Char] = "abcdefghijklmnopqrstuvwxyz-' ".toCharArray()
    tesseract.setDatapath("tessdata")
    var output_recognize: String = ""
    var output_database: String = ""

    def recognize(): Unit = {
        // This worked, but was too slow.
        // The recognizer would be polling the video feed, forcing us to process on the recognizer's thread.
        //        video_feed { image =>
        //            val text = tesseract.doOCR(image)
        //            val clean = text
        //                .toLowerCase()
        //                .toCharArray()
        //                .filter { char =>
        //                    alpha.contains(char)
        //                }
        //                .mkString
        //            output_recognize = clean
        //            output_database = database.find(clean)
        //        }
    }
}


