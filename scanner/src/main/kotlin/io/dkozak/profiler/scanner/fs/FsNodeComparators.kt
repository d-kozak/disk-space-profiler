package io.dkozak.profiler.scanner.fs

import javafx.scene.control.TreeItem

object FsNodeBySizeComparator : Comparator<TreeItem<FsNode>> {

    override fun compare(left: TreeItem<FsNode>, right: TreeItem<FsNode>): Int = when {
        left.value is FsNode.DirectoryNode && right.value !is FsNode.DirectoryNode -> -1
        left.value !is FsNode.DirectoryNode && right.value is FsNode.DirectoryNode -> 1
        else -> {
            val cmp = right.value.size.compareTo(left.value.size)
            if (cmp != 0) cmp
            else left.value.file.name.compareTo(right.value.file.name)
        }
    }
}

object FsNodeByNameComparator : Comparator<TreeItem<FsNode>> {

    override fun compare(left: TreeItem<FsNode>, right: TreeItem<FsNode>): Int = when {
        left.value is FsNode.DirectoryNode && right.value !is FsNode.DirectoryNode -> -1
        left.value !is FsNode.DirectoryNode && right.value is FsNode.DirectoryNode -> 1
        else -> {
            val cmp = left.value.file.name.compareTo(right.value.file.name)
            if (cmp != 0) cmp
            else right.value.size.compareTo(left.value.size)
        }
    }
}