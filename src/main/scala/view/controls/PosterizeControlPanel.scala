package org.bpc
package view.controls

import processing.PosterizeVideoProcessor

import scala.swing.GridBagPanel._
import scala.swing.Swing._
import scala.swing._
import scala.swing.event._

// View
class PosterizeControlPanel(posterizer: PosterizeVideoProcessor) extends BoxPanel(Orientation.Vertical) {
    border = TitledBorder(EtchedBorder(Raised), "Posterize Module")

    val sliderAlpha = Controls.makeSlider("Alpha")(value => posterizer.alpha = value)
    val sliderBeta = Controls.makeSlider("Beta")(value => posterizer.beta = value)


    val toggleEnable = new ToggleButton("On/Off") {
        selected = true
        reactions += { case ButtonClicked(_) => posterizer.enabled = !posterizer.enabled
        }
    }

    val toggleInvert = new ToggleButton("Invert") {
        reactions += { case ButtonClicked(_) => posterizer.invert = !posterizer.invert }
    }

    contents += new GridPanel(1, 3) {
        contents += toggleEnable
        contents += toggleInvert

        contents.foreach { c =>
            c.xLayoutAlignment = 0.5
            c.yLayoutAlignment = 0.5
        }
    }

    val sliders = List(sliderAlpha, sliderBeta)
    contents += Controls.makeLabelGrid(sliders)
}
