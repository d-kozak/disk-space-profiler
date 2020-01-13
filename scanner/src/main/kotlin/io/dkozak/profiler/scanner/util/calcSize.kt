package io.dkozak.profiler.scanner.util

import java.io.File

fun calcSize(file: File): Triple<FileSize, Int, Int> {
    if (!file.isDirectory) return Triple(FileSize(file.length()), 1, 0)
    var size = FileSize(0)
    var fileCount = 0
    var directoryCount = 1
    for (child in file.listFiles() ?: arrayOf()) {
        val (childSize, childFileFileCount, childDirectoryCount) = calcSize(child)
        size += childSize
        fileCount += childFileFileCount
        directoryCount += childDirectoryCount
    }
    return Triple(size, fileCount, directoryCount)
}