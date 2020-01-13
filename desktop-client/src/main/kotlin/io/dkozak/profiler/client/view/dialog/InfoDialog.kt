package io.dkozak.profiler.client.view.dialog

import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Parent
import tornadofx.*


abstract class InfoDialog : Fragment() {
    abstract fun EventTarget.dialogContent()

    override val root: Parent = borderpane {
        title = "Instructions"
        style {
            padding = box(10.px)
        }
        center { dialogContent() }
        bottom {
            hbox {
                alignment = Pos.CENTER_RIGHT
                button("Close") {
                    action {
                        close()
                    }
                }
            }
        }
    }


}