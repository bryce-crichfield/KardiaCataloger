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

    val sliderPatchSize = Controls.makeSlider("Patch Size", 1 to 15)(value => denoise.patchSize = (value * 15).toInt)
    val sliderWindowSize = Controls.makeSlider("Window Size", 1 to 15)(value => denoise.windowSize = (value * 15).toInt)
    val sliderSigma = Controls.makeSlider("Sigma")(value => denoise.sigma = value.toFloat)
    val sliderH = Controls.makeSlider("H")(value => denoise.h = value.toFloat)


    val sliders = List(sliderPatchSize, sliderWindowSize, sliderSigma, sliderH)

    contents += toggleEnable

    contents += Controls.makeLabelGrid(sliders)

}
