package io.dkozak.profiler.scanner.fs

import io.dkozak.profiler.scanner.util.FileSize
import io.dkozak.profiler.scanner.util.Invariant
import io.dkozak.profiler.scanner.util.Precondition
import io.dkozak.profiler.scanner.util.toFileSize
import javafx.scene.control.TreeItem
import java.io.File

/**
 * true if this node represents directory
 */
val TreeItem<FsNode>.isDirectory: Boolean
    get() = this.value is FsNode.DirectoryNode

/**
 * true if this node is either lazy (either file or dir)
 */
val TreeItem<FsNode>.isLazy: Boolean
    get() = this.value.isLazy

/**
 * true if this node is either not lazy (either file or dir)
 */
val TreeItem<FsNode>.isNotLazy: Boolean
    get() = !this.isLazy

/**
 * true if this node is a lazy file
 */
val TreeItem<FsNode>.isLazyFile: Boolean
    get() = !this.isDirectory && this.isLazy

/**
 * true if this node is a lazy file
 */
val TreeItem<FsNode>.isNotLazyFile: Boolean
    get() = !this.isLazyFile


/**
 * true if this node is a lazy dir
 */
val TreeItem<FsNode>.isLazyDir: Boolean
    get() = this.isDirectory && this.isLazy


/**
 * File represented by given node
 */
val TreeItem<FsNode>.file: File
    get() = this.value.file

/**
 * Replaces this node in a tree with newNode
 */
@Precondition("this.parent != null")
fun TreeItem<FsNode>.replaceWith(newNode: TreeItem<FsNode>) {
    val parentNode = parent
    this.detachFromTree()
    parentNode.insertSorted(newNode)
}

/**
 * Inserts a new file node below this node while keeping it's sort order, replaces the old version if there is one
 */
@Precondition("this.isDirectory")
@Precondition("file.exists")
fun TreeItem<FsNode>.insertSorted(file: File): TreeItem<FsNode> {
    check(file.exists()) { "attempt to insert a file that does not exist ${file.absolutePath}" }
    check(this.isDirectory) { "current node is not a directory node" }
    return if (file.isDirectory) {
        insertSorted(lazyNodeFor(file))
    } else {
        insertSorted(TreeItem<FsNode>(FsNode.FileNode(file)))
    }
}

/**
 * Inserts a new node below this node while keeping it's sort order, replaces the old version if there is one
 */
@Precondition("this.isDirectory")
fun TreeItem<FsNode>.insertSorted(node: TreeItem<FsNode>): TreeItem<FsNode> {
    val toInsert = children.binarySearch(node, value.comparator)
    if (toInsert < 0) {
        children.add(-toInsert - 1, node)
    } else {
        children[toInsert] = node
    }
    propagateSizeUp(node.value.size)
    return node
}

/**
 * Detach given node from the fs tree
 */
@Precondition("this.parent != null")
fun TreeItem<FsNode>.detachFromTree() {
    check(parent != null) { "cannot remove node with no parent" }
    parent.propagateSizeUp(-this.value.size)
    parent.children.remove(this)
}

/**
 * Change the size of very node starting from this one up to root by value
 * @param value by how much the size should change
 */
fun TreeItem<FsNode>.propagateSizeUp(value: FileSize) {
    var node: TreeItem<FsNode>? = this
    while (node != null) {
        node.value.size += value
        node = node.parent
    }
}

/**
 * Create new lazy node for a directory represented by currentFile
 */
@Invariant("currentFile.isDirectory")
fun lazyNodeFor(currentFile: File): TreeItem<FsNode> {
    check(currentFile.isDirectory) { "file $currentFile is not a directory" }
    val lazyDir = TreeItem<FsNode>(FsNode.DirectoryNode(currentFile, true))
    lazyDir.value.size = currentFile.length().toFileSize()
    val lazyFile = TreeItem<FsNode>(FsNode.FileNode(currentFile, true))
    lazyDir.children.add(lazyFile)
    return lazyDir
}