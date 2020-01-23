package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.file
import io.dkozak.profiler.scanner.fs.lazyNodeFor
import io.dkozak.profiler.scanner.util.Precondition
import javafx.scene.control.TreeItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import mu.KotlinLogging
import java.io.File


private val logger = KotlinLogging.logger { }

fun CoroutineScope.crawlFileTree(config: ScanConfig): Triple<TreeItem<FsNode>, MutableList<TreeItem<FsNode>>, ScanStats> =
        FsCrawler(this, config).start()


/**
 * Crawls the fs tree in depth-first traversal.
 * Creates intermediate representation up to specified depth,
 * then checks deeper subtrees using FileTreeScanningVisitor
 */
private class FsCrawler(
        private val scope: CoroutineScope,
        private val config: ScanConfig
) {

    /**
     * Count of files encountered
     */
    var fileCount = 0L
    /**
     * Count of directories encountered
     */
    var directoryCount = 0L

    val lazyDirectories = mutableListOf<TreeItem<FsNode>>()

    fun start(): Triple<TreeItem<FsNode>, MutableList<TreeItem<FsNode>>, ScanStats> {
        val start = System.currentTimeMillis()
        val tree = recursiveScan(config.startNode.file)
        return Triple(tree, lazyDirectories, ScanStats(fileCount, directoryCount, System.currentTimeMillis() - start))
    }


    /**
     * Crawl the fs tree starting from given file
     * @param file to be crawled
     * @param currentDepth depth of depth-first search
     */
    @Precondition("currentNode.file.exists")
    private fun recursiveScan(currentFile: File, currentDepth: Int = 0): TreeItem<FsNode> {
        check(currentFile.exists()) { "file ${currentFile.absolutePath} does not exist" }
        if (!scope.isActive) {
            logger.info { "Cancelation detected" }
            throw CancellationException()
        }

        return if (!currentFile.isDirectory) {
            fileCount++
            TreeItem(FsNode.FileNode(currentFile))
        } else {
            directoryCount++
            if (currentDepth >= config.treeDepth) {
                lazyNodeFor(currentFile).also { lazyDirectories.add(it) }
            } else {
                val directoryNode: TreeItem<FsNode> = TreeItem(FsNode.DirectoryNode(currentFile))
                for (file in directoryNode.file.listFiles() ?: arrayOf()) {
                    try {
                        val node = recursiveScan(file, currentDepth + 1)
                        directoryNode.children.add(node)
                        directoryNode.value.size += node.value.size
                    } catch (ex: CancellationException) {
                        // just push it up
                        throw ex
                    } catch (ex: Exception) {
                        if (ex.message != null) {
                            logger.warn { ex.message }
                        }
                    }
                }
                directoryNode.also { it.children.sortWith(directoryNode.value.comparator) }
            }
        }
    }
}