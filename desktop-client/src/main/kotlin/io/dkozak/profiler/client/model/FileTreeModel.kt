package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.event.MessageEvent
import io.dkozak.profiler.client.util.ProgressAdapter
import io.dkozak.profiler.client.util.onUiThread
import io.dkozak.profiler.scanner.DiskScanner
import io.dkozak.profiler.scanner.SimpleDiskScanner
import io.dkozak.profiler.scanner.fs.*
import io.dkozak.profiler.scanner.util.BackgroundThread
import io.dkozak.profiler.scanner.util.UiThread
import javafx.beans.property.SimpleObjectProperty
import javafx.concurrent.Task
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

    private val discScanner = SimpleDiskScanner()

    /**
     * Execute new scan.
     * @param rootDirectory from where to start
     * @param scanConfig configuration
     * @param fxTask current task
     */
    @BackgroundThread
    fun newScan(rootDirectory: String, scanConfig: DiskScanner.ScanConfig, task: FXTask<*>) {
        fire(MessageEvent("Scan of '$rootDirectory' started"))
        val (root, time) = discScanner.newScan(rootDirectory, scanConfig, ProgressAdapter(task))
        fire(MessageEvent("Scan of '$rootDirectory' finished, it took ${time} ms"))
        logger.info { "new fstree $root" }
        onUiThread {
            registerExpandListeners(root)
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
        fire(MessageEvent("Scan of '${selectedNode.file.absolutePath}' started"))
        val parent = selectedNode.parent
        val (newTree, time) = discScanner.rescanFrom(selectedNode, DiskScanner.ScanConfig(), ProgressAdapter(task))
        onUiThread {
            registerExpandListeners(newTree)
            if (parent != null) {
                selectedNode.replaceWith(newTree)
            } else {
                rootProperty.set(newTree)
            }
            newTree.isExpanded = true
            fire(MessageEvent("Rescan of '${selectedNode.file.absolutePath}' finished, it took ${time} ms"))
        }
        return newTree
    }

    @UiThread
    fun rescanRequested(node: TreeItem<FsNode>): Task<TreeItem<FsNode>>? = if (!node.value.scanStarted) {
        node.value.scanStarted = true
        runAsync { rescanFrom(node, this) } ui {
            node.value.scanStarted = false
        }
    } else {
        logger.info { "scan for ${node.file.absolutePath} is already running" }
        null
    }


    /**
     * Registers expand listener for lazy node in the tree, so that rescan is triggered automatically when such node is visible
     */
    @UiThread
    private fun registerExpandListeners(node: TreeItem<FsNode>) {
        if (node.isLazyDir) {
            node.expandedProperty().onChange { if (it) rescanRequested(node) }
        } else if (node.isDirectory) {
            node.children.forEach(::registerExpandListeners)
        }
    }

    /**
     * Remove specified file or directory.
     * @param node to remove
     */
    fun removeNode(node: TreeItem<FsNode>): Boolean {
        if (!node.value.file.deleteRecursively()) {
            val errorMessage = "Failed to delete file ${node.file.absolutePath}"
            logger.warn { errorMessage }
            error("Delete failed", errorMessage)
            fire(MessageEvent(errorMessage))
            return false
        }
        node.detachFromTree()
        fire(MessageEvent("${if (node.value is FsNode.DirectoryNode) "Directory" else "File"} ${node.value.file.name} deleted"))
        return true
    }
}


