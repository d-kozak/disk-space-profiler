package io.dkozak.profiler.client.viewmodel

import io.dkozak.profiler.client.util.ProgressAdapter
import io.dkozak.profiler.scanner.scanFromDirectory
import tornadofx.*

fun FXTask<*>.runScan(rootDirectory: String) {
    scanFromDirectory(rootDirectory, ProgressAdapter(this))
}