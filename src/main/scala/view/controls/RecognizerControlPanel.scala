package org.bpc
package view.controls

import org.bpc.Recognizer

import java.awt
import javax.swing.Timer
import scala.swing.Swing._
import scala.swing._
import scala.swing.event._

class RecognizerControlPanel(recognizer: Recognizer) extends BoxPanel(Orientation.Vertical) {
    border = TitledBorder(EtchedBorder(Raised), "Recognition Engine")

    val toggleEnable = new ToggleButton("On/Off") {
        selected = true
        reactions += { case ButtonClicked(_) =>
        }
    }

    val closestMatchLabel = new Label("Closest Match: ")
    val confidenceLabel = new Label("Confidence: ")

    val timer = new Timer(1000, Swing.ActionListener(e => {
        recognizer.recognize()
    }))

    timer.start()

    contents += toggleEnable
    contents += closestMatchLabel
    contents += confidenceLabel
}
