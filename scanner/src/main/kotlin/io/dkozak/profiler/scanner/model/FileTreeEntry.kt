package io.dkozak.profiler.scanner.model

sealed class FileTreeEntry {
    val fullPath: String
        get() = "/"
}

data class RootEntry(val files: List<FileTreeEntry>) : FileTreeEntry() {
    constructor(vararg files: FileTreeEntry) : this(files.toList())
}

data class DirectoryEntry(val name: String, val files: List<FileTreeEntry>) : FileTreeEntry() {
    constructor(name: String, vararg files: FileTreeEntry) : this(name, files.toList())
}

data class FileEntry(val name: String) : FileTreeEntry()