package org.bpc
package view.controls

import org.bpc.processing.TransformVideoProcessor

import java.awt.Color
import scala.swing.GridBagPanel._
import scala.swing.Swing._
import scala.swing._
import scala.swing.event._

class TransformControlPanel(transform: TransformVideoProcessor) extends BoxPanel(Orientation.Vertical) {
    border = TitledBorder(EtchedBorder(Raised), "Transformer Module")

    val toggleEnable = new ToggleButton("On/Off") {
        selected = true
        reactions += { case ButtonClicked(_) => ()
        }
    }

    val sliderCropX = Controls.makeSlider("Translate Up")(value => transform.tUp = value.toFloat)
    val sliderCropY = Controls.makeSlider("Translate Right")(value => transform.tRight = value.toFloat)
    val sliderCropWidth = Controls.makeSlider("Translate Down")(value => transform.tDown = value.toFloat)
    val sliderCropHeight = Controls.makeSlider("Translate Left")(value => transform.tLeft = value.toFloat)
    val sliderRotation = Controls.makeSlider("Rotation")(value => transform.rotation = value.toFloat)
    val sliderSkewX = Controls.makeSlider("Skew X")(value => transform.skewX = value.toFloat)
    val sliderSkewY = Controls.makeSlider("Skew Y")(value => transform.skewY = value.toFloat)

    val sliders = List(sliderCropX, sliderCropY, sliderCropWidth, sliderCropHeight, sliderRotation, sliderSkewX, sliderSkewY)

    contents += toggleEnable
    contents += Controls.makeLabelGrid(sliders)
}
