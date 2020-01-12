package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.model.FileTreeEntry
import io.dkozak.profiler.scanner.util.ProgressMonitor

interface DiscScanner {

    fun newScan(root: String, monitor: ProgressMonitor): FileTreeEntry

    fun newScan(root: FileTreeEntry, monitor: ProgressMonitor): FileTreeEntry

    fun partialScan(startNode: FileTreeEntry, root: FileTreeEntry, monitor: ProgressMonitor): FileTreeEntry
}