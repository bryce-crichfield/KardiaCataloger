package org.bpc
package view.controls

import processing._

import scala.swing.GridBagPanel.{Anchor, Fill}
import scala.swing.Swing.VStrut
import scala.swing._
import scala.swing.event.ValueChanged

object Controls {
    def controlPanel(posterize: PosterizeVideoProcessor, morphology: MorphologyVideoProcessor, denoise: DenoiseVideoProcessor, transform: TransformVideoProcessor, recognize: Recognizer): Component = new ScrollPane() {
        contents = new BoxPanel(Orientation.Vertical) {
            contents += new TransformControlPanel(transform)
            contents += VStrut(10)
            contents += new PosterizeControlPanel(posterize)
            contents += VStrut(10)
            contents += new MorphologyControlPanel(morphology)
            contents += VStrut(10)
            contents += new DenoiseControlPanel(denoise)
            contents += VStrut(10)
            contents += new RecognizerControlPanel(recognize)

            maximumSize = new Dimension(300, Short.MaxValue)
        }

        // increase scroll speed
        peer.getVerticalScrollBar.setUnitIncrement(16)

    }

    def makeSlider(text: String)(f: Double => Unit): (Label, Component) = {
        val label = new Label(text)

        val panel = new BoxPanel(Orientation.Horizontal) {
            val slider = new Slider() {
                preferredSize = new Dimension(50, 35)

                min = 1
                max = 255
                value = 128
                reactions += { case _: ValueChanged => f(value / 255.0)
                }
            }

            val label = new Label {
                val getText = () => f"${slider.value / 255.0}%1.2f"
                text = getText()
                listenTo(slider)
                reactions += { case _: ValueChanged => text = getText()
                }

                preferredSize = new Dimension(50, 35)
            }

            contents ++= Seq(slider, label)
        }

        (label, panel)
    }

    def makeLabelGrid(components: List[(Label, Component)]): Component = {
        val grid = new GridBagPanel() {
            for (row <- 0 until components.length) {
                val (label, slider) = components(row)

                // the 1st column is all labels and should be 25% of the width
                add(label, new Constraints() {
                    gridx = 0
                    gridy = row
                    weightx = 0.25
                    anchor = Anchor.West
                    insets = new Insets(5, 5, 5, 5)
                })

                // the 2nd column is all sliders and should be 75% of the width
                add(slider, new Constraints() {
                    gridx = 1
                    gridy = row
                    weightx = 0.75
                    fill = Fill.Horizontal
                    insets = new Insets(5, 5, 5, 5)
                })
            }
        }

        grid
    }


}
