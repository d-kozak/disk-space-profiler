package io.dkozak.profiler.client.view

import javafx.geometry.Pos
import tornadofx.*

class ProgressView : Fragment() {
    val status: TaskStatus by inject()

    override val root = hbox(4) {
        alignment = Pos.CENTER
        label(status.message)
        progressbar(status.progress)
        visibleWhen { status.running }
    }
}