package org.bpc
package view.controls

import processing._

import scala.swing.GridBagPanel.{Anchor, Fill}
import scala.swing.Swing.VStrut
import scala.swing._
import scala.swing.event.ValueChanged

object Controls {

    def makeSlider(text: String, range: Range)(f: Double => Unit): (Label, Component) = {
        val label = new Label(text)

        val panel = new BoxPanel(Orientation.Horizontal) {
            val slider = new Slider() {
                preferredSize = new Dimension(50, 35)

                min = range.min
                max = range.max
                value = range.max / 2
                reactions += { case _: ValueChanged => f(value / range.max.toDouble)
                }
            }

            val label = new Label {
                val getText = () => f"${slider.value / range.max.toDouble}%1.2f"
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

    def makeSlider(text: String)(f: Double => Unit): (Label, Component) = {
        makeSlider(text, 1 to 255)(f)
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
