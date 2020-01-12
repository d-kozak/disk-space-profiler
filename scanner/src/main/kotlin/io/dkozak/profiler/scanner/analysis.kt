package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.util.ProgressMonitor

fun scanFromDirectory(rootDirectory: String, monitor: ProgressMonitor) {
    monitor.message("scanning: $rootDirectory")
    monitor.progress(3, 10)
    Thread.sleep(1000)
    monitor.progress(6, 10)
    Thread.sleep(1000)
    monitor.progress(9, 10)
    Thread.sleep(1000)
}
