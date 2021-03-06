package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.client.viewmodel.LastMessageViewModel
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Parent
import tornadofx.*

/**
 * Displays the bottom status bar containing last message, progressbar and information about disk usage
 */
class StatusBarView : View() {
    private val messageViewModel: LastMessageViewModel by inject()

    private val fileTreeViewModel: FileTreeViewModel by inject()

    private val diskInfoProperty = SimpleStringProperty(this, "diskInfo", "")

    override val root: Parent = borderpane {
        left = label(messageViewModel.lastMessageProperty)
        right = hbox(2) {
            add<ProgressView>()
            label(diskInfoProperty)
        }

        children.style {
            padding = box(2.px, 5.px)
        }
    }

    init {
        fileTreeViewModel.fileTreeProperty.onChange { root ->
            if (root != null)
                diskInfoProperty.set("Used ${root.value.usedSpace} / ${root.value.totalSpace}")
        }
    }
}