package io.dkozak.profiler.scanner.fs


import javafx.scene.control.TreeItem
import java.io.File


sealed class FsNode(var file: File) {
    internal lateinit var diskRoot: TreeItem<FsNode>
    var size: Long = -1

    val root: TreeItem<DiskRoot>
        get() = diskRoot as TreeItem<DiskRoot>

    companion object {
        val DEFAULT_COMPARATOR = FsNodeComparator().reversed()
    }

    open class DirectoryNode(file: File) : FsNode(file) {
        override fun toString(): String = "DirectoryNode(${file.absolutePath})"
    }

    class DiskRoot(file: File) : DirectoryNode(file) {
        var spaceAvailable = 0
        var occupiedSpace: Long = 0
    }

    class FileNode(file: File) : FsNode(file) {
        override fun toString(): String = "FileNode(${file.absolutePath})"
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


