package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.util.ProgressMonitor

class DummyProgressMonitor : ProgressMonitor {
    override fun message(text: String) {
        println("message: $text")
    }

    override fun progress(number: Long, max: Long) {
        println("progress: $number/$max")
    }
}