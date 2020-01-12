package io.dkozak.profiler.scanner.model

fun String.asFileTreeEntry() = RootEntry()

sealed class FileTreeEntry {
    var parent: FileTreeEntry? = null
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


fun FileTreeEntry.setParentReferences(parent: FileTreeEntry? = null): FileTreeEntry {
    this.parent = parent
    when (this) {
        is DirectoryEntry -> files.forEach { it.setParentReferences(this) }
        is RootEntry -> files.forEach { it.setParentReferences(this) }
    }
    return this
}