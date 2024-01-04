package org.bpc
package view.controls

import processing.DenoiseVideoProcessor

import scala.swing.GridBagPanel._
import scala.swing.Swing._
import scala.swing._
import scala.swing.event._

class DenoiseControlPanel(denoise: DenoiseVideoProcessor) extends BoxPanel(Orientation.Vertical) {
    border = TitledBorder(EtchedBorder(Raised), "Denoiser Module")

    val toggleEnable = new ToggleButton("On/Off") {
        selected = true
        reactions += { case ButtonClicked(_) => ()
        }
    }

    val comboPatchSize = new Label("Patch Size") -> new ComboBox[Int](Seq(5, 15, 25, 35)) {
        listenTo(selection)
        reactions += { case _: SelectionChanged =>
            denoise.patchSize = selection.item
        }
    }

    val comboWindowSize = new Label("Window Size") -> new ComboBox[Int](Seq(5, 15, 25, 35)) {
        listenTo(selection)
        reactions += { case _: SelectionChanged =>
            denoise.windowSize = selection.item
        }
    }

    val comboFilterStrength = new Label("Filter Strength") -> new ComboBox[Int](Seq(5, 15, 25, 35)) {
        listenTo(selection)
        reactions += { case _: SelectionChanged =>
            denoise.filterStrength = selection.item
        }
    }

    val comboBoxes = List(comboPatchSize, comboWindowSize, comboFilterStrength)


    contents += toggleEnable

    contents += new GridBagPanel() {
        for (row <- 0 until comboBoxes.length) {
            val (label, combo) = comboBoxes(row)

            // the 1st column is all labels and should be 25% of the width
            add(label, new Constraints() {
                gridx = 0
                gridy = row
                weightx = 0.25
                anchor = Anchor.West
                insets = new Insets(5, 5, 5, 5)
            })

            // the 2nd column is all sliders and should be 75% of the width
            add(combo, new Constraints() {
                gridx = 1
                gridy = row
                weightx = 0.75
                fill = Fill.Horizontal
                insets = new Insets(5, 5, 5, 5)
            })
        }
    }


}
