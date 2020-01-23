package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.lazyNodeFor
import io.dkozak.profiler.scanner.util.*
import javafx.scene.control.TreeItem
import mu.KotlinLogging
import java.io.File


private val logger = KotlinLogging.logger { }

/**
 * Crawls the fs tree in depth-first traversal.
 * Creates intermediate representation up to specified depth,
 * then checks deeper subtrees using FileTreeScanningVisitor
 */
internal class FsCrawler(
        /**
         * Node from which the analysis should be started
         */
        val startNode: TreeItem<FsNode>,
        private val config: DiskScanner.ScanConfig,
        private val monitor: ProgressMonitor) {

    /**
     * Count of files encountered
     */
    var fileCount = 0
    /**
     * Count of directories encountered
     */
    var directoryCount = 0

    /**
     * current thread reference, it is expensive to ask about it every time
     */
    private val currentThread: Thread = Thread.currentThread()

    /**
     * Crawl the fs tree starting from the diskRoot
     * @return fs tree
     */
    fun crawl(): TreeItem<FsNode> {
        monitor.message("scanning: ${startNode.value.file.name}")
        val node = recursiveScan(startNode.value.file)
        check(node.value is FsNode.DirectoryNode) { "node resulting from the scan is not a directory node" }
        return startNode.apply {
            this.value.size = node.value.size
            this.children.addAll(node.children)
        }
    }


    /**
     * Crawl the fs tree starting from given file
     * @param file to be crawled
     * @param currentDepth depth of depth-first search
     */
    @Precondition("currentFile.exists")
    fun recursiveScan(currentFile: File, currentDepth: Int = 0): TreeItem<FsNode> {
        if (currentThread.isInterrupted) {
            logger.info { "Cancelation detected" }
            throw InterruptedException()
        }

        monitor.message("ScanInfo: ${fileCount} files, ${directoryCount} directories, depth $currentDepth")
        check(currentFile.exists()) { "file ${currentFile.absolutePath} does not exist" }
        if (!currentFile.isDirectory) {
            fileCount++
            return processSingleFile(currentFile)
        }
        directoryCount++
        if (currentDepth >= config.treeDepth) {
            val lazyDir = lazyNodeFor(currentFile)
            val (subtreeSize, subtreeDirs, subtreeFiles) = scanSubtree(currentFile)
            lazyDir.value.size = subtreeSize
            directoryCount += subtreeDirs
            fileCount += subtreeFiles
            return lazyDir
        }
        return processDirectory(currentFile, currentDepth + 1)
    }

    private fun processSingleFile(currentFile: File): TreeItem<FsNode> {
        return TreeItem(FsNode.FileNode(currentFile).apply {
            size = FileSize(currentFile.length())
        })

    }

    private fun processDirectory(currentFile: File, currentDepth: Int): TreeItem<FsNode> {
        val files = currentFile.listFiles()
                ?: throw IllegalStateException("current dir $currentFile returned null for listFiles")
        val nodeInfo = FsNode.DirectoryNode(currentFile)

        val directoryNode: TreeItem<FsNode> = TreeItem(nodeInfo)
        directoryNode.value.size = currentFile.length().toFileSize()

        for (file in files) {
            try {
                val node = recursiveScan(file, currentDepth)
                directoryNode.value.size += node.value.size
                directoryNode.children.add(node)
            } catch (ex: InterruptedException) {
                // just push it up
                throw ex
            } catch (ex: Exception) {
                if (ex.message != null) {
                    logger.warn { ex.message }
                }
            }

        }
        directoryNode.children.sortWith(directoryNode.value.comparator)
        return directoryNode
    }
}