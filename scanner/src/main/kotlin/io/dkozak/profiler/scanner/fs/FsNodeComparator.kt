package io.dkozak.profiler.scanner.fs

import javafx.scene.control.TreeItem

class FsNodeComparator : Comparator<TreeItem<FsNode>> {

    override fun compare(left: TreeItem<FsNode>, right: TreeItem<FsNode>): Int = when {
        left.value is FsNode.DirectoryNode && right.value !is FsNode.DirectoryNode -> 1
        left.value !is FsNode.DirectoryNode && right.value is FsNode.DirectoryNode -> -1
        else -> left.value.size.compareTo(right.value.size)
    }


}