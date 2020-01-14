package io.dkozak.profiler.scanner.fs

import io.dkozak.profiler.scanner.util.Cleanup
import javafx.scene.control.TreeItem

/**
 * Sort criteria(descending priority):
 * directories before files
 * size - descending
 * name - ascending
 */
@Cleanup("Use fluent comparator builders instead")
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

/**
 * Sort criteria(descending priority):
 * directories before files
 * name - ascending
 * size - descending
 */
@Cleanup("Use fluent comparator builders instead")
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