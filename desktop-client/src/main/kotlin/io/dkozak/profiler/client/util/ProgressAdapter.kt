package io.dkozak.profiler.client.util

import io.dkozak.profiler.scanner.util.ProgressMonitor
import mu.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger { }

/**
 * Adapter translating messages from the ProgressMonitor to FxXTask.
 * @see ProgressMonitor
 * @see FXTask
 */
class ProgressAdapter(private val task: FXTask<*>) : ProgressMonitor {
    override fun message(text: String) {
        logger.debug { "message $text" }
        task.updateMessage(text)
    }

    override fun progress(number: Long, max: Long) {
        logger.debug { "progress, number $number,max $max" }
        task.updateProgress(number, max)
    }
}