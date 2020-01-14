package io.dkozak.profiler.client.view

import javafx.geometry.Pos
import tornadofx.*

/**
 * Displays the current progress of scanning.
 */
class ProgressView : Fragment() {

    val showMessage: Boolean by param(true)

    private val status: TaskStatus by inject()

    override val root = hbox(4) {
        alignment = Pos.CENTER
        if (showMessage)
            label(status.message)
        progressbar(status.progress)
        visibleWhen { status.running }
    }
}