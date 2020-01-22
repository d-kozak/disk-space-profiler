package io.dkozak.profiler.scanner.fs

import javafx.scene.control.TreeItem

/**
 * Simple comparator putting directories before files
 */
val FsNodeDirectoryBeforeFileComparator = Comparator<TreeItem<FsNode>> { left, right ->
    when {
        left.value is FsNode.DirectoryNode && right.value !is FsNode.DirectoryNode -> -1
        left.value !is FsNode.DirectoryNode && right.value is FsNode.DirectoryNode -> 1
        else -> 0
    }
}

/**
 * Sort criteria(descending priority):
 * directories before files
 * size - descending
 * name - ascending
 */
val FsNodeBySizeComparator = FsNodeDirectoryBeforeFileComparator
        .thenByDescending { it.value.size }
        .thenBy { it.file.name.toUpperCase() }


/**
 * Sort criteria(descending priority):
 * directories before files
 * name - ascending
 * size - descending
 */
val FsNodeByNameComparator = FsNodeDirectoryBeforeFileComparator
        .thenBy { it.file.name.toUpperCase() }
        .thenByDescending { it.value.size }
