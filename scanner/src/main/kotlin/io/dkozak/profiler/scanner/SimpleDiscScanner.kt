package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.model.FileTreeEntry
import io.dkozak.profiler.scanner.model.RootEntry
import io.dkozak.profiler.scanner.model.asFileTreeEntry
import io.dkozak.profiler.scanner.util.ProgressMonitor

class SimpleDiscScanner : DiscScanner {

    override fun newScan(root: String, monitor: ProgressMonitor): FileTreeEntry = newScan(root.asFileTreeEntry(), monitor)

    override fun newScan(root: FileTreeEntry, monitor: ProgressMonitor): FileTreeEntry {
        monitor.message("scanning: ${root.fullPath}")
        monitor.progress(3, 10)
        Thread.sleep(1000)
        monitor.progress(6, 10)
        Thread.sleep(1000)
        monitor.progress(9, 10)
        Thread.sleep(1000)
        return RootEntry()
    }

    override fun partialScan(startNode: FileTreeEntry, root: FileTreeEntry, monitor: ProgressMonitor): FileTreeEntry {
        return newScan(root, monitor)
    }
}