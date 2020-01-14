package io.dkozak.profiler.client.view.dialog

import io.dkozak.profiler.scanner.util.Cleanup
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Parent
import tornadofx.*

/**
 * Abstract class for any simple dialog.
 * Exposes an abstract method that it's children can use to create the content.
 */
@Cleanup("Issue with parent vs child initiation order, maybe refactor this into a builder DSL method?")
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