package io.dkozak.profiler.scanner.util

interface ProgressMonitor {
    fun message(text: String)
    fun progress(number: Long, max: Long)
}