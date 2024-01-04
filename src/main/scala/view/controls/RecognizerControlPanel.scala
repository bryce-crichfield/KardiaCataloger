package org.bpc
package view.controls

import org.bpc.Recognizer
import javax.swing.Timer
import scala.swing.Swing._
import scala.swing._
import scala.swing.event._

class RecognizerControlPanel(recognizer: Recognizer)
    extends BoxPanel(Orientation.Vertical) {
    border = TitledBorder(EtchedBorder(Raised), "Recognition Engine")
    val button_recognize = new Button("Recognize") {
        reactions += { case ButtonClicked(_) =>
            recognizer.recognize()
        }
    }

    val label_output_recognize = new Label {
        background = new java.awt.Color(0x33, 0x33, 0x33)
        new Timer(
            1000 / 2,
            Swing.ActionListener { _ =>
                this.text = f"Recognition = ${recognizer.output_recognize}"
            }
        ).start()
    }

    val label_output_database = new Label {
        background = new java.awt.Color(0x33, 0x33, 0x33)
        new Timer(
            1000 / 2,
            Swing.ActionListener { _ =>
                this.text = f"Closest Match = ${recognizer.output_database}"
            }
        ).start()
    }

    contents ++= Seq(
        button_recognize,
        label_output_recognize,
        label_output_database
    )
}
