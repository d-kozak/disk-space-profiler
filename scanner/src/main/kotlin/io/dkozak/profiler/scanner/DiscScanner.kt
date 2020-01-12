package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.DiskRoot
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.util.ProgressMonitor

interface DiscScanner {

    fun newScan(root: String, monitor: ProgressMonitor): DiskRoot
    fun partialScan(startNode: FsNode, monitor: ProgressMonitor): FsNode
}