package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.viewmodel.LastMessageViewModel
import javafx.scene.Parent
import tornadofx.*

class StatusBarView : View() {
    private val messageViewModel: LastMessageViewModel by inject()

    override val root: Parent = borderpane {
        left = label(messageViewModel.lastMessageProperty)
        right = hbox(2) {
            add<ProgressView>()
            label("Used 23/50 gb")
        }

        children.style {
            padding = box(2.px, 5.px)
        }
    }
}