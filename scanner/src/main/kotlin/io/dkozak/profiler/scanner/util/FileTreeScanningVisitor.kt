package io.dkozak.profiler.scanner.util

import io.dkozak.profiler.scanner.dto.ScanStatistics
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
internal fun scanSubtree(file: File): Pair<FileSize, ScanStatistics> {
    val visitor = FileTreeScanningVisitor()
    Files.walkFileTree(file.toPath(), visitor)
    return visitor.buildResult()
}

private val logger = KotlinLogging.logger { }

/**
 * FileVisitor which traverses the whole file subtree
 * and calculates it's size and the number of directories and files
 */
internal class FileTreeScanningVisitor : FileVisitor<Path> {

    /**
     * when the analysis started (you should use the visitor immediatelly after creating it, otherwise it will not be a valid start time)
     */
    val start = System.currentTimeMillis()

    /**
     * Total sum of all files
     */
    var size: FileSize = 0.bytes
    /**
     * Count of directories
     */
    var directoryCount: Long = 0L
    /**
     * Count of files
     */
    var fileCount: Long = 0L

    fun buildResult() = this.size to ScanStatistics(this.fileCount, this.directoryCount, System.currentTimeMillis() - this.start)


    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes?): FileVisitResult {
        return FileVisitResult.CONTINUE
    }

    override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
        directoryCount++
        size += dir.toFile().length().toFileSize()
        return FileVisitResult.CONTINUE
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
        fileCount++
        size += file.toFile().length().toFileSize()
        return FileVisitResult.CONTINUE
    }

    override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
        logger.warn { "Failed for $file" }
        exc.printStackTrace()
        return FileVisitResult.CONTINUE
    }


}