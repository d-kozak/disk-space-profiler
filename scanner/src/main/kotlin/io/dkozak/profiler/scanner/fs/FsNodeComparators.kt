package io.dkozak.profiler.scanner.fs

import javafx.scene.control.TreeItem

object FsNodeBySizeComparator : Comparator<TreeItem<FsNode>> {

    override fun compare(left: TreeItem<FsNode>, right: TreeItem<FsNode>): Int = when {
        left.value is FsNode.DirectoryNode && right.value !is FsNode.DirectoryNode -> -1
        left.value !is FsNode.DirectoryNode && right.value is FsNode.DirectoryNode -> 1
        else -> right.value.size.compareTo(left.value.size)
    }
}

object FsNodeByNameComparator : Comparator<TreeItem<FsNode>> {

    override fun compare(left: TreeItem<FsNode>, right: TreeItem<FsNode>): Int = when {
        left.value is FsNode.DirectoryNode && right.value !is FsNode.DirectoryNode -> -1
        left.value !is FsNode.DirectoryNode && right.value is FsNode.DirectoryNode -> 1
        else -> left.value.file.name.compareTo(right.value.file.name)
    }
}