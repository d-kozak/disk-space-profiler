package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.util.BackgroundThread
import io.dkozak.profiler.scanner.util.Precondition
import io.dkozak.profiler.scanner.util.ProgressMonitor
import javafx.scene.control.TreeItem

/**
 * Main interface of the module
 */
@BackgroundThread
interface DiskScanner {
    /**
     * Configuration options for scanning
     */
    data class ScanConfig(
            /**
             * Depth into which internal representation should be created, anything deeper is just scanned and summed up.
             */
            var treeDepth: Int = DEFAULT_TREE_DEPTH
    ) {
        companion object {
            const val DEFAULT_TREE_DEPTH = 2
        }
    }

    /**
     * Resulting statistics after scanning
     */
    data class ScanStatistics(
            val root: TreeItem<FsNode>,
            val time: Long
    )


    /**
     * Execute new scan from a given path
     */
    fun newScan(@Precondition("root.isDirectory") root: String, config: ScanConfig, monitor: ProgressMonitor): ScanStatistics

    /**
     * Execute new scan from a given node
     */
    fun rescanFrom(startNode: TreeItem<FsNode>, config: ScanConfig, monitor: ProgressMonitor): ScanStatistics
}