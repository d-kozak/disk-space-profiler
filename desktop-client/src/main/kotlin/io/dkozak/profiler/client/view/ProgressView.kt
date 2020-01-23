package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.model.FileTreeModel
import javafx.geometry.Pos
import tornadofx.*

/**
 * Displays the current progress of scanning.
 */
class ProgressView : Fragment() {

    val showMessage: Boolean by param(true)

    val fileTreeModel: FileTreeModel by inject()

    private val status: TaskStatus by inject()

    override val root = hbox(4) {
        alignment = Pos.CENTER
        if (showMessage)
            label(status.message)
        progressbar()
        button("Cancel") {
            action { fileTreeModel.cancelScans() }
        }
        visibleWhen { fileTreeModel.anyAnalysisRunningProperty }
    }
}