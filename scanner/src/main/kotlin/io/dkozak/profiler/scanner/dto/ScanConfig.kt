package io.dkozak.profiler.scanner.dto

import io.dkozak.profiler.scanner.fs.FsNode
import javafx.scene.control.TreeItem

/**
 * Configuration options for scanning
 */
data class ScanConfig(
        /**
         * Depth into which internal representation should be created, anything deeper is just scanned and summed up.
         */
        var treeDepth: Int = DEFAULT_TREE_DEPTH,
        /**
         * Node from which to start the analysis
         */
        var startNode: TreeItem<FsNode>
) {
    companion object {
        const val DEFAULT_TREE_DEPTH = 2
    }
}