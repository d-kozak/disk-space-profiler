package io.dkozak.profiler.client.util

import io.dkozak.profiler.scanner.util.ProgressMonitor
import tornadofx.*

class ProgressAdapter(private val task: FXTask<*>) : ProgressMonitor {
    override fun message(text: String) {
        task.updateMessage(text)
    }

    override fun progress(number: Long, max: Long) {
        task.updateProgress(number, max)
    }
}