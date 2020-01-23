package io.dkozak.profiler.scanner.fs


import io.dkozak.profiler.scanner.util.FileSize
import io.dkozak.profiler.scanner.util.Invariant
import io.dkozak.profiler.scanner.util.toFileSize
import javafx.scene.control.TreeItem
import java.io.File


/**
 * Node of the file system tree
 */
sealed class FsNode(var file: File, var isLazy: Boolean = false) {
    /**
     * Comparator used to sort children of current elements
     */
    var comparator: Comparator<TreeItem<FsNode>> = DEFAULT_COMPARATOR

    /**
     * Total size of the subtree starting at this node
     */
    var size = file.length().toFileSize()

    /**
     * True if this node's subtree is currently being scanned
     */
    var scanStarted = false

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

    /**
     * Used space of the disk on which corresponding file is located
     */
    val usedSpace: FileSize
        get() = totalSpace - this.file.freeSpace.toFileSize()

    companion object {
        /**
         * Default comparator for nodes
         */
        val DEFAULT_COMPARATOR = FsNodeBySizeComparator
    }


    class DirectoryNode(file: File, isLazy: Boolean = false) : FsNode(file, isLazy) {
        override fun toString(): String = "DirectoryNode(${file.absolutePath})"
    }

    class FileNode(file: File, isLazy: Boolean = false) : FsNode(file, isLazy) {
        override fun toString(): String = "FileNode(${file.absolutePath})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FsNode) return false

        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }
}


