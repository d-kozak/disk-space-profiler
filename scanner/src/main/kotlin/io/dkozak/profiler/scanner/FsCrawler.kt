package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.DiskRoot
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.util.ProgressMonitor
import java.io.File

fun String.crawl(progressMonitor: ProgressMonitor): DiskRoot {
    val rootFile = File(this)
    check(rootFile.exists()) { "Given root file $this does not exist" }
    check(rootFile.isDirectory) { "Given root file $this is not a directory" }
    return FsCrawler(DiskRoot(rootFile, FsNode.DirectoryNode(rootFile)), progressMonitor).crawl()
}

class FsCrawler(val diskRoot: DiskRoot, private val monitor: ProgressMonitor) {

    val errorMessages = mutableListOf<String>()

    fun crawl(): DiskRoot {
        monitor.message("scanning: ${diskRoot.file.name}")
        val node = recursiveScan(diskRoot.file, null)
        check(node is FsNode.DirectoryNode) { "node resulting from the scan is not a directory node" }
        return diskRoot.apply { this.node = node }
    }

    fun recursiveScan(currentFile: File, parentNode: FsNode.DirectoryNode? = null): FsNode {
        monitor.message("File: ${currentFile.name}")
        check(currentFile.exists()) { "file ${currentFile.absolutePath} does not exist" }
        if (!currentFile.isDirectory) {
            return processSingleFile(currentFile, parentNode)
        }
        return processDirectory(currentFile, parentNode)
    }

    private fun processSingleFile(currentFile: File, parentNode: FsNode.DirectoryNode? = null) =
            FsNode.FileNode(currentFile).apply {
                size = currentFile.length()
                this.disk = diskRoot
                this.parent = parentNode
            }


    private fun processDirectory(currentFile: File, parentNode: FsNode.DirectoryNode? = null): FsNode.DirectoryNode {
        val files = currentFile.listFiles()
                ?: throw IllegalStateException("current dir $currentFile returned null for listFiles")
        val fileNodes = mutableListOf<FsNode>()
        val directoryNode = FsNode.DirectoryNode(currentFile, fileNodes).apply {
            this.disk = diskRoot
            this.parent = parentNode
        }

        for (file in files) {
            try {
                val node = recursiveScan(file, directoryNode)
                directoryNode.size += node.size
                fileNodes.add(node)
            } catch (ex: Exception) {
                if (ex.message != null) {
                    println(ex.message)
                    errorMessages.add(ex.message!!)
                }
            }

        }
        return directoryNode
    }


}