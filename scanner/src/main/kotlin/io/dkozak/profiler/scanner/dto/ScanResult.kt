package io.dkozak.profiler.scanner.dto

import io.dkozak.profiler.scanner.fs.FsNode
import javafx.scene.control.TreeItem

/**
 * Overall result of one scan
 */
sealed class ScanResult {
    /**
     * Node from which the scan originally started,
     */
    abstract val startNode: TreeItem<FsNode>

    /**
     * Is any analysis running still running?
     */
    abstract var anyAnalysisRunning: Boolean

    data class Success(
            override val startNode: TreeItem<FsNode>,
            /**
             * Overall statistics of the scan
             */
            val stats: ScanStatistics,
            override var anyAnalysisRunning: Boolean = false
    ) : ScanResult()

    data class Failure(
            override val startNode: TreeItem<FsNode>,
            /**
             * Description of the error
             */
            val message: String,
            override var anyAnalysisRunning: Boolean = false
    ) : ScanResult()

}