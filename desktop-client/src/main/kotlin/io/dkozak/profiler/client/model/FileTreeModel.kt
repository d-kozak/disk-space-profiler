package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.event.MessageEvent
import io.dkozak.profiler.client.util.ProgressAdapter
import io.dkozak.profiler.client.util.onUiThread
import io.dkozak.profiler.scanner.ScanConfig
import io.dkozak.profiler.scanner.SimpleDiscScanner
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.insertSorted
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem
import mu.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger { }

class FileTreeModel : Controller() {

    private val discScanner = SimpleDiscScanner()

    val fileTreeProperty = SimpleObjectProperty<TreeItem<FsNode>>(this, "fileTree", null)

    fun newScan(rootDirectory: String, scanConfig: ScanConfig, task: FXTask<*>) {
        val (root, time) = discScanner.newScan(rootDirectory, scanConfig, ProgressAdapter(task))
        fire(MessageEvent("Scan of '$rootDirectory' finished, it took ${time} ms"))
        logger.info { "new fstree $root" }
        onUiThread {
            fileTreeProperty.set(root)
        }
    }

    fun partialScan(selectedNode: TreeItem<FsNode>, task: FXTask<*>): TreeItem<FsNode> {
        val parent = selectedNode.parent
        val (newTree, time) = discScanner.partialScan(selectedNode, ScanConfig(), ProgressAdapter(task))
        onUiThread {
            if (parent != null) {
                selectedNode.removeFromParent()
                parent.insertSorted(newTree)
            } else {
                fileTreeProperty.set(newTree)
            }
            fire(MessageEvent("Rescan of '${selectedNode.value.file.absolutePath}' finished, it took ${time} ms"))
        }
        return newTree
    }

    fun removeNode(node: TreeItem<FsNode>): Boolean {
        if (!node.value.file.deleteRecursively()) {
            logger.warn { "failed to delete file ${node.value.file.absolutePath}" }
            return false
        }
        node.removeFromParent()
        fire(MessageEvent("${if (node.value is FsNode.DirectoryNode) "Directory" else "File"} ${node.value.file.name} deleted"))
        return true
    }
}