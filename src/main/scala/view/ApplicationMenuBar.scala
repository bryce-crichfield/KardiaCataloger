package org.bpc
package view

import scala.swing._

class ApplicationMenuBar extends MenuBar {
    contents += new Menu("File") {
        contents += new MenuItem(Action("Exit") {
            sys.exit(0)
        })
    }

    contents += new Menu("Edit") {
        contents += new MenuItem(Action("Preferences") {
            val dialog = new Dialog() {
                title = "Preferences"
                contents = new BoxPanel(Orientation.Vertical) {
                    contents += new Label("Preferences")
                }
            }
            dialog.open()
            dialog.centerOnScreen()
        })
    }

    contents += new Menu("About") {
        contents += new MenuItem(Action("About") {
            val dialog = new Dialog() {
                title = "About"
                contents = new BoxPanel(Orientation.Vertical) {
                    contents += new Label("About")
                }
            }
            dialog.open()
            dialog.size = new Dimension(400, 300)
            dialog.centerOnScreen()
        })
    }
}
