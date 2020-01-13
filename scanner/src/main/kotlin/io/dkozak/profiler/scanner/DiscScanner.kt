package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.util.ProgressMonitor
import javafx.scene.control.TreeItem

interface DiscScanner {

    fun newScan(root: String, config: ScanConfig, monitor: ProgressMonitor): ScanStatistics
    fun partialScan(startNode: TreeItem<FsNode>, config: ScanConfig, monitor: ProgressMonitor): ScanStatistics
}