package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.util.ProgressMonitor
import javafx.scene.control.TreeItem
import java.io.File

fun String.crawl(progressMonitor: ProgressMonitor): TreeItem<FsNode> {
    val rootFile = File(this)
    check(rootFile.exists()) { "Given root file $this does not exist" }
    check(rootFile.isDirectory) { "Given root file $this is not a directory" }
    return FsCrawler(TreeItem(FsNode.DiskRoot(rootFile)), progressMonitor).crawl()
}

class FsCrawler(val diskRoot: TreeItem<FsNode>, private val monitor: ProgressMonitor) {

    val errorMessages = mutableListOf<String>()

    fun crawl(): TreeItem<FsNode> {
        monitor.message("scanning: ${diskRoot.value.file.name}")
        val node = recursiveScan(diskRoot.value.file)
        check(node.value is FsNode.DirectoryNode) { "node resulting from the scan is not a directory node" }
        return diskRoot.apply { this.children.addAll(node.children) }
    }

    fun recursiveScan(currentFile: File): TreeItem<FsNode> {
        monitor.message("File: ${currentFile.name}")
        check(currentFile.exists()) { "file ${currentFile.absolutePath} does not exist" }
        if (!currentFile.isDirectory) {
            return processSingleFile(currentFile)
        }
        return processDirectory(currentFile)
    }

    private fun processSingleFile(currentFile: File): TreeItem<FsNode> =
            TreeItem(FsNode.FileNode(currentFile).apply {
                size = currentFile.length()
                this.diskRoot = this@FsCrawler.diskRoot
            })


    private fun processDirectory(currentFile: File): TreeItem<FsNode> {
        val files = currentFile.listFiles()
                ?: throw IllegalStateException("current dir $currentFile returned null for listFiles")
        val nodeInfo = FsNode.DirectoryNode(currentFile).apply {
            this.diskRoot = this@FsCrawler.diskRoot
        }

        val directoryNode: TreeItem<FsNode> = TreeItem(nodeInfo)

        for (file in files) {
            try {
                val node = recursiveScan(file)
                directoryNode.value.size += node.value.size
                directoryNode.children.add(node)
            } catch (ex: Exception) {
                if (ex.message != null) {
                    println(ex.message)
                    errorMessages.add(ex.message!!)
                }
            }

        }
        directoryNode.children.sortWith(FsNode.DEFAULT_COMPARATOR)
        return directoryNode
    }


}