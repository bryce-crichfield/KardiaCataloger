package org.bpc
package view.controls

import processing.MorphologyVideoProcessor

import scala.swing.Swing._
import scala.swing._
import scala.swing.event._

// View
class MorphologyControlPanel(morpholizer: MorphologyVideoProcessor) extends BoxPanel(Orientation.Vertical) {
    border = TitledBorder(EtchedBorder(Raised), "Morpholizer Module")

    val toggle_enable = new ToggleButton("On/Off") {
        selected = true
        reactions += { case ButtonClicked(_) => morpholizer.enabled = !morpholizer.enabled
        }
    }

    val combo_radius = new ComboBox[Int](Seq(5, 15, 25, 35)) {
        listenTo(selection)
        reactions += { case _: SelectionChanged => morpholizer.radius = selection.item
        }
    }

    val combo_mode = new ComboBox[String](Seq("Erode", "Dilate", "Open", "Close")) {
        listenTo(selection)
        reactions += { case _: SelectionChanged => morpholizer.mode = selection.item
        }
    }

    val toggle_panel = new GridPanel(1, 3) {
        contents += toggle_enable
        contents += HStrut(50)
    }
    contents ++= Seq(toggle_panel, combo_radius, combo_mode)
}
