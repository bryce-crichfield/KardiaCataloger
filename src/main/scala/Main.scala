package org.bpc


import model.Database
import processing._
import video.{ProcessedVideoFeed, WebcamVideoFeed}
import view._
import view.controls.{Controls, DenoiseControlPanel, MorphologyControlPanel, PosterizeControlPanel, RecognizerControlPanel, TransformControlPanel}

import com.formdev.flatlaf.FlatDarkLaf

import javax.swing.UIManager
import scala.swing.Swing.VStrut
import scala.swing._

object Main extends SwingApplication {
    val processingService = new ImageProcessingService()

    val webcam = new WebcamVideoFeed()
    val transformVideoProcessor = new TransformVideoProcessor(processingService)
    val denoiseVideoProcessor = new DenoiseVideoProcessor(processingService)
    val posterizer = new PosterizeVideoProcessor(processingService)
    val morpholizer = new MorphologyVideoProcessor(processingService)
    val processedFeed = new ProcessedVideoFeed(webcam, processingService, transformVideoProcessor,posterizer, denoiseVideoProcessor, morpholizer)
    val database = new Database("resource/clean.json")
    val recognizer = new Recognizer(processedFeed, database)

    override def startup(args: Array[String]): Unit = {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf())
        } catch {
            case e: Exception => println("Failed to load look and feel")
        }

        val frame = top()
        if (frame.size == new Dimension(0, 0)) frame.pack()
        frame.visible = true
        frame.resizable = true
        frame.size = new Dimension(800, 600)
        frame.centerOnScreen()
    }

    def top(): Frame = new MainFrame {
        title = "Card Scanner"

        menuBar = new ApplicationMenuBar()

        val videoPanel = new BoxPanel(Orientation.Vertical) {
            contents += new VideoFeedPanel(webcam, 30, "Raw Feed")
            contents += new VideoFeedPanel(processedFeed, 30, "Filtered Feed")
        }

        val controlPanel = new ScrollPane() {
            contents = new BoxPanel(Orientation.Vertical) {
                contents += new TransformControlPanel(transformVideoProcessor)
                contents += VStrut(10)
                contents += new PosterizeControlPanel(posterizer)
                contents += VStrut(10)
                contents += new DenoiseControlPanel(denoiseVideoProcessor)
                contents += VStrut(10)
                contents += new MorphologyControlPanel(morpholizer)
                contents += VStrut(10)
                contents += new RecognizerControlPanel(recognizer)

                maximumSize = new Dimension(300, Short.MaxValue)
            }

            // increase scroll speed
            peer.getVerticalScrollBar.setUnitIncrement(16)

        }

        val videoAndControlPanel = new SplitPane(Orientation.Vertical, videoPanel, controlPanel) {
            dividerLocation = 400
        }

        contents = new TabbedPane {
            pages += new TabbedPane.Page("Video", videoAndControlPanel)
            pages += new TabbedPane.Page("Catalogue", new Label("Catalogue"))

            // tabs at the bottom
            tabPlacement = Alignment.Bottom
        }
    }

    override def shutdown(): Unit = {
        // kill the webcam thread
        webcam.close()
    }
}