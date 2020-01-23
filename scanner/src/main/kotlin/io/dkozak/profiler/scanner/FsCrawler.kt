package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.file
import io.dkozak.profiler.scanner.fs.lazyNodeFor
import io.dkozak.profiler.scanner.util.Precondition
import io.dkozak.profiler.scanner.util.scanSubtree
import javafx.scene.control.TreeItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File


private val logger = KotlinLogging.logger { }

suspend fun CoroutineScope.startCrawling(config: ScanConfig, treeUpdateChannel: SendChannel<TreeUpdate>, scanningFinishedChannel: SendChannel<AnalysisFinished>) {
    val crawler = FsCrawler(this, config, treeUpdateChannel)
    val start = System.currentTimeMillis()
    try {
        crawler.start()
        scanningFinishedChannel.send(AnalysisFinished(config.startNode, ScanStats(crawler.fileCount, crawler.directoryCount, System.currentTimeMillis() - start)))
    } catch (ex: Exception) {
        if (ex is CancellationException) throw ex
        scanningFinishedChannel.send(AnalysisFinished(config.startNode, ScanStats(crawler.fileCount, crawler.directoryCount, System.currentTimeMillis() - start), errorMessage = ex.message
                ?: "error"))
    }
}

/**
 * Crawls the fs tree in depth-first traversal.
 * Creates intermediate representation up to specified depth,
 * then checks deeper subtrees using FileTreeScanningVisitor
 */
private class FsCrawler(
        private val scope: CoroutineScope,
        private val config: ScanConfig,
        private val treeUpdateChannel: SendChannel<TreeUpdate>
) {

    /**
     * Count of files encountered
     */
    var fileCount = 0L
    /**
     * Count of directories encountered
     */
    var directoryCount = 0L

    var startTime = System.currentTimeMillis()

    suspend fun start() {
        startTime = System.currentTimeMillis()
        recursiveScan(config.startNode.parent, config.startNode.file)
    }


    /**
     * Crawl the fs tree starting from given file
     * @param file to be crawled
     * @param currentDepth depth of depth-first search
     */
    @Precondition("currentNode.file.exists")
    private suspend fun recursiveScan(parent: TreeItem<FsNode>?, currentFile: File, currentDepth: Int = 0) {
        check(currentFile.exists()) { "file ${currentFile.absolutePath} does not exist" }
        if (!scope.isActive) {
            logger.info { "Cancelation detected" }
            throw CancellationException()
        }

        if (!currentFile.isDirectory) {
            fileCount++
            val fileNode: TreeItem<FsNode> = TreeItem(FsNode.FileNode(currentFile))
            treeUpdateChannel.send(TreeUpdate.AddNodeRequest(parent, fileNode, createScanStats()))
        } else {
            directoryCount++
            if (currentDepth >= config.treeDepth) {
                val lazyDir = lazyNodeFor(currentFile)
                treeUpdateChannel.send(TreeUpdate.AddNodeRequest(parent, lazyDir, createScanStats()))
                val (subtreeSize, subtreeDirs, subtreeFiles) = withContext(scope.coroutineContext) { scanSubtree(currentFile) }
                fileCount += subtreeFiles
                directoryCount += subtreeDirs
                val newLazyDir = lazyNodeFor(currentFile).apply { this.value.size += subtreeSize }
                treeUpdateChannel.send(TreeUpdate.ReplaceNodeRequest(lazyDir, newLazyDir, createScanStats()))
            } else {
                val directoryNode: TreeItem<FsNode> = TreeItem(FsNode.DirectoryNode(currentFile))
                treeUpdateChannel.send(TreeUpdate.AddNodeRequest(parent, directoryNode, createScanStats()))
                for (file in directoryNode.file.listFiles() ?: arrayOf()) {
                    try {
                        recursiveScan(directoryNode, file, currentDepth + 1)
                    } catch (ex: CancellationException) {
                        // just push it up
                        throw ex
                    } catch (ex: Exception) {
                        if (ex.message != null) {
                            logger.warn { ex.message }
                        }
                    }

                }
            }
        }
    }


    private fun createScanStats() = ScanStats(fileCount, directoryCount, System.currentTimeMillis() - startTime)
}