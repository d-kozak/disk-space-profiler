package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.event.MessageEvent
import io.dkozak.profiler.scanner.util.BackgroundThread
import io.dkozak.profiler.client.util.ProgressAdapter
import io.dkozak.profiler.client.util.onUiThread
import io.dkozak.profiler.scanner.ScanConfig
import io.dkozak.profiler.scanner.SimpleDiscScanner
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.removeSelfFromTree
import io.dkozak.profiler.scanner.fs.replaceWith
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem
import mu.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger { }

/**
 * Maintains fsTree displayed in the app.
 * Updates it using DiskScanner.
 */
class FileTreeModel : Controller() {

    /**
     * Root of the file tree displayed in the app
     */
    val rootProperty = SimpleObjectProperty<TreeItem<FsNode>>(this, "fileTree", null)

    private val discScanner = SimpleDiscScanner()

    /**
     * Execute new scan.
     * @param rootDirectory from where to start
     * @param scanConfig configuration
     * @param fxTask current task
     */
    @BackgroundThread
    fun newScan(rootDirectory: String, scanConfig: ScanConfig, task: FXTask<*>) {
        val (root, time) = discScanner.newScan(rootDirectory, scanConfig, ProgressAdapter(task))
        fire(MessageEvent("Scan of '$rootDirectory' finished, it took ${time} ms"))
        logger.info { "new fstree $root" }
        onUiThread {
            rootProperty.set(root)
        }
    }

    /**
     * Rescan the disk starting from specified node.
     * @param selectedNode from where to start
     * @task current task
     */
    @BackgroundThread
    fun rescanFrom(selectedNode: TreeItem<FsNode>, task: FXTask<*>): TreeItem<FsNode> {
        val parent = selectedNode.parent
        val (newTree, time) = discScanner.partialScan(selectedNode, ScanConfig(), ProgressAdapter(task))
        onUiThread {
            if (parent != null) {
                selectedNode.replaceWith(newTree)
            } else {
                rootProperty.set(newTree)
            }
            fire(MessageEvent("Rescan of '${selectedNode.value.file.absolutePath}' finished, it took ${time} ms"))
        }
        return newTree
    }

    /**
     * Remove specified file or directory.
     * @param node to remove
     */
    fun removeNode(node: TreeItem<FsNode>): Boolean {
        if (!node.value.file.deleteRecursively()) {
            logger.warn { "failed to delete file ${node.value.file.absolutePath}" }
            return false
        }
        node.removeSelfFromTree()
        fire(MessageEvent("${if (node.value is FsNode.DirectoryNode) "Directory" else "File"} ${node.value.file.name} deleted"))
        return true
    }
}


