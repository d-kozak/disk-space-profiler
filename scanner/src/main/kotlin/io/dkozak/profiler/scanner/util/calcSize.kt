package io.dkozak.profiler.scanner.util

import java.io.File

fun calcSize(file: File): FileSize {
    if (!file.isDirectory) return FileSize(file.length())
    var size = FileSize(0)
    for (child in file.listFiles() ?: arrayOf()) {
        size += calcSize(child)
    }
    return size
}