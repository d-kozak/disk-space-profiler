package io.dkozak.profiler.scanner.fs

import io.dkozak.profiler.scanner.util.toFileSize
import javafx.scene.control.TreeItem
import java.io.File

fun TreeItem<FsNode>.insertSorted(file: File): TreeItem<FsNode> {
    check(file.exists()) { "attempt to insert a file that does not exist ${file.absolutePath}" }
    check(this.value is FsNode.DirectoryNode) { "current node is not a directory node" }
    return if (file.isDirectory) {
        insertSorted(lazyNodeFor(file, value.diskRoot))
    } else {
        insertSorted(TreeItem<FsNode>(FsNode.FileNode(file)))
    }
}

fun TreeItem<FsNode>.insertSorted(node: TreeItem<FsNode>): TreeItem<FsNode> {
    val toInsert = children.binarySearch(node, value.comparator)
    check(toInsert < 0) { "node should NOT be in the tree right now, it was found at $toInsert, $node, $children" }
    children.add(-toInsert - 1, node)
    node.value.diskRoot = this.value.diskRoot
    return node
}


internal fun lazyNodeFor(currentFile: File, diskRoot: TreeItem<FsNode>): TreeItem<FsNode> {
    val lazyDir = TreeItem<FsNode>(FsNode.DirectoryNode(currentFile))
    lazyDir.value.diskRoot = diskRoot
    lazyDir.value.size = currentFile.length().toFileSize()
    val lazyNode: TreeItem<FsNode> = TreeItem(FsNode.LazyNode(currentFile))
    lazyNode.value.diskRoot = diskRoot
    lazyDir.children.add(lazyNode)
    return lazyDir
}