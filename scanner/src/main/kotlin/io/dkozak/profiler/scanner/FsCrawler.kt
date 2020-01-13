package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.util.FileSize
import io.dkozak.profiler.scanner.util.ProgressMonitor
import io.dkozak.profiler.scanner.util.calcSize
import javafx.scene.control.TreeItem
import java.io.File


class FsCrawler(val diskRoot: TreeItem<FsNode>, private val config: ScanConfig, private val monitor: ProgressMonitor) {

    val errorMessages = mutableListOf<String>()

    var fileCount = 0
    var directoryCount = 0

    fun crawl(): TreeItem<FsNode> {
        monitor.message("scanning: ${diskRoot.value.file.name}")
        val node = recursiveScan(diskRoot.value.file)
        check(node.value is FsNode.DirectoryNode) { "node resulting from the scan is not a directory node" }
        node.value.root.value.occupiedSpace = node.value.size
        return diskRoot.apply {
            this.value.diskRoot = node.value.diskRoot
            this.value.size = node.value.size
            this.children.addAll(node.children)
        }
    }

    fun recursiveScan(currentFile: File, currentDepth: Int = 0): TreeItem<FsNode> {
        monitor.message("ScanInfo: ${fileCount} files, ${directoryCount} directories, depth $currentDepth")
        check(currentFile.exists()) { "file ${currentFile.absolutePath} does not exist" }
        if (!currentFile.isDirectory) {
            fileCount++
            return processSingleFile(currentFile)
        }
        directoryCount++
        if (currentDepth == config.treeDepth) {
            val lazyDir = TreeItem<FsNode>(FsNode.DirectoryNode(currentFile))
            lazyDir.value.diskRoot = diskRoot
            val (subtreeSize, subtreeFileCount, subtreeDirectoryCount) = calcSize(currentFile)
            lazyDir.value.size = subtreeSize
            fileCount += subtreeFileCount
            directoryCount += subtreeDirectoryCount
            val lazyNode: TreeItem<FsNode> = TreeItem(FsNode.LazyNode(currentFile))
            lazyNode.value.diskRoot = diskRoot
            lazyDir.children.add(lazyNode)
            return lazyDir
        }
        return processDirectory(currentFile, currentDepth + 1)
    }

    private fun processSingleFile(currentFile: File): TreeItem<FsNode> {
        return TreeItem(FsNode.FileNode(currentFile).apply {
            size = FileSize(currentFile.length())
            this.diskRoot = this@FsCrawler.diskRoot
        })

    }

    private fun processDirectory(currentFile: File, currentDepth: Int): TreeItem<FsNode> {
        val files = currentFile.listFiles()
                ?: throw IllegalStateException("current dir $currentFile returned null for listFiles")
        val nodeInfo = FsNode.DirectoryNode(currentFile).apply {
            this.diskRoot = this@FsCrawler.diskRoot
        }

        val directoryNode: TreeItem<FsNode> = TreeItem(nodeInfo)

        for (file in files) {
            try {
                val node = recursiveScan(file, currentDepth)
                directoryNode.value.size += node.value.size
                directoryNode.children.add(node)
            } catch (ex: Exception) {
                if (ex.message != null) {
                    println(ex.message)
                    errorMessages.add(ex.message!!)
                }
            }

        }
        directoryNode.children.sortWith(directoryNode.value.comparator)
        return directoryNode
    }


}