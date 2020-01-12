package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.util.ProgressAdapter
import io.dkozak.profiler.scanner.scanFromDirectory
import tornadofx.*

class FileTreeModel : Controller() {
    fun scan(rootDirectory: String, task: FXTask<*>) {
        scanFromDirectory(rootDirectory, ProgressAdapter(task))
        fire(MessageEvent("Scan of '$rootDirectory' finished"))
    }
}