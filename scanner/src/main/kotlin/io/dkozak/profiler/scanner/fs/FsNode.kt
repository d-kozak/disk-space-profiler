package io.dkozak.profiler.scanner.fs


import io.dkozak.profiler.scanner.util.Cleanup
import io.dkozak.profiler.scanner.util.FileSize
import io.dkozak.profiler.scanner.util.Invariant
import io.dkozak.profiler.scanner.util.toFileSize
import javafx.scene.control.TreeItem
import java.io.File


/**
 * Node of the file system tree
 */
sealed class FsNode(var file: File) {
    /**
     * Comparator used to sort children of current elements
     */
    var comparator: Comparator<TreeItem<FsNode>> = DEFAULT_COMPARATOR

    /**
     * Root of the tree - every node can access it for convenience
     */
    internal lateinit var diskRoot: TreeItem<FsNode>

    /**
     * Total size of the subtree starting at this node
     */
    var size = file.length().toFileSize()

    /**
     * True if this node's subtree is currently being scanned
     */
    var scanStarted = false

    /**
     * Root of the tree - every node can access it for convenience
     */
    @Cleanup("diskroot vs root - could not find to way to make the type system happy")
    val root: TreeItem<DiskRoot>
        get() = diskRoot as TreeItem<DiskRoot>

    /**
     * How much space of it's disk is given file taking
     */
    @Invariant("0.0 <= spaceTaken <= 1.0")
    val spaceTaken: Double
        get() = this.size.relativeTo(this.file.totalSpace.toFileSize())
                .coerceAtLeast(0.0)
                .coerceAtMost(1.0)

    /**
     * Size of the disk on which corresponding file is located
     */
    val totalSpace: FileSize
        get() = this.file.totalSpace.toFileSize()

    companion object {
        /**
         * Default comparator for nodes
         */
        val DEFAULT_COMPARATOR = FsNodeBySizeComparator
    }


    open class DirectoryNode(file: File) : FsNode(file) {
        override fun toString(): String = "DirectoryNode(${file.absolutePath})"
    }

    /**
     * Root of the tree
     */
    @Cleanup("currently, there is no distinction between DiskRoot and a simple DirectoryNode")
    class DiskRoot(file: File) : DirectoryNode(file) {
        override fun toString(): String = "DiskRoot(${file.absolutePath})"
    }

    class FileNode(file: File) : FsNode(file) {
        override fun toString(): String = "FileNode(${file.absolutePath})"
    }

    /**
     * Lazy node in the file tree
     */
    class LazyNode(file: File) : FsNode(file) {
        override fun toString(): String = "LazyNode(${file.absolutePath})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FsNode) return false

        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        return file.absolutePath.hashCode()
    }
}


