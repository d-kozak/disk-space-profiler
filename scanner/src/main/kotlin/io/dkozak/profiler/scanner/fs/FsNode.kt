package io.dkozak.profiler.scanner.fs

import java.io.File

sealed class FsNode(var file: File) {
    var parent: DirectoryNode? = null
    lateinit var disk: DiskRoot
    var size: Long = -1

    class DirectoryNode(file: File, val files: List<FsNode>) : FsNode(file) {
        constructor(file: File, vararg files: FsNode) : this(file, files.toList())

        override fun toString(): String = "DirectoryNode(file=$file,files=$files)"

    }

    class FileNode(file: File) : FsNode(file) {
        override fun toString(): String = "FileNode(file=$file)"
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


