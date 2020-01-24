package io.dkozak.profiler.scanner.dto

import io.dkozak.profiler.scanner.fs.FsNode
import javafx.scene.control.TreeItem

/**
 * Updates from the ScannerManager back to it's client,
 * their purpose is to update the fs tree ( which does not belong to the Manager so he can't perform the update himself)
 */
sealed class TreeUpdateRequest {
    /**
     * Add a new child to specific parent
     * if the parent is null, new child is meant to be the root of the fs tree
     */
    data class AddChild(
            val parent: TreeItem<FsNode>?,
            val newChild: TreeItem<FsNode>,
            val stats: ScanStatistics
    ) : TreeUpdateRequest()

    /**
     * Replace given node with new version of it.
     *
     * Used when a particular subtree is refreshed or when lazy dirs aggregating scans finishes, so that real size of it is known.
     */
    data class ReplaceNode(
            val oldNode: TreeItem<FsNode>,
            val newNode: TreeItem<FsNode>,
            val stats: ScanStatistics
    ) : TreeUpdateRequest()
}