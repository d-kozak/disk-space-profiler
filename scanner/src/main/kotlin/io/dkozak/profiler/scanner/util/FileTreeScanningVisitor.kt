package io.dkozak.profiler.scanner.util

import mu.KotlinLogging
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes


/**
 * Scan subtree starting at given file
 * @return scanResult
 */
internal fun scanSubtree(file: File): FileTreeScanningVisitor.Result {
    val visitor = FileTreeScanningVisitor()
    Files.walkFileTree(file.toPath(), visitor)
    return visitor.result
}

private val logger = KotlinLogging.logger { }

/**
 * FileVisitor which traverses the whole file subtree
 * and calculates it's size and the number of directories and files
 */
internal class FileTreeScanningVisitor : FileVisitor<Path> {
    /**
     * Result of the scan
     */
    data class Result(
            /**
             * Total sum of all files
             */
            var size: FileSize = 0.bytes,
            /**
             * Count of directories
             */
            var directoryCount: Int = 0,
            /**
             * Count of files
             */
            var fileCount: Int = 0)

    var result = Result()

    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes?): FileVisitResult {
        return FileVisitResult.CONTINUE
    }

    override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
        result.directoryCount++
        result.size += dir.toFile().length().toFileSize()
        return FileVisitResult.CONTINUE
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
        result.fileCount++
        result.size += file.toFile().length().toFileSize()
        return FileVisitResult.CONTINUE
    }

    override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
        logger.warn { "Failed for $file" }
        exc.printStackTrace()
        return FileVisitResult.CONTINUE
    }


}