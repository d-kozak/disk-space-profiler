package io.dkozak.profiler.client.view

import javafx.scene.Parent
import tornadofx.*

class StatusBarView : View() {
    override val root: Parent = borderpane {
        left = label("Last scan at DD/MM/YYYY")
        right = hbox(2) {
            add<ProgressView>()
            label("Used 23/50 gb")
        }

        children.style {
            padding = box(2.px, 5.px)
        }
    }
}