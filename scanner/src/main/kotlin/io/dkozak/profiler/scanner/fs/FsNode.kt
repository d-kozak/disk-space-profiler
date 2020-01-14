package io.dkozak.profiler.scanner.fs


import io.dkozak.profiler.scanner.util.FileSize
import io.dkozak.profiler.scanner.util.toFileSize
import javafx.scene.control.TreeItem
import java.io.File


sealed class FsNode(var file: File) {
    var comparator: Comparator<TreeItem<FsNode>> = DEFAULT_COMPARATOR

    internal lateinit var diskRoot: TreeItem<FsNode>
    var size = file.length().toFileSize()

    val root: TreeItem<DiskRoot>
        get() = diskRoot as TreeItem<DiskRoot>

    val spaceTaken: Double
        get() = this.size.relativeTo(this.file.totalSpace.toFileSize())
                .coerceAtLeast(0.0)
                .coerceAtMost(1.0)

    val totalSpace: FileSize
        get() = this.file.totalSpace.toFileSize()

    companion object {
        val DEFAULT_COMPARATOR = FsNodeBySizeComparator
    }

    open class DirectoryNode(file: File) : FsNode(file) {
        override fun toString(): String = "DirectoryNode(${file.absolutePath})"
    }

    class DiskRoot(file: File) : DirectoryNode(file) {
        var spaceAvailable = FileSize(0)
        var occupiedSpace = FileSize(0)
    }

    class FileNode(file: File) : FsNode(file) {
        override fun toString(): String = "FileNode(${file.absolutePath})"
    }

    class LazyNode(file: File) : FsNode(file) {
        override fun toString(): String = "LazyNode(${file.absolutePath})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FsNode) return false

        if (file.absolutePath != other.file.absolutePath) return false

        return true
    }

    override fun hashCode(): Int {
        return file.absolutePath.hashCode()
    }
}


