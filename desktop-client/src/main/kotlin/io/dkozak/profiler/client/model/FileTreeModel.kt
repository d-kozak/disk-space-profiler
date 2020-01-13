package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.event.MessageEvent
import io.dkozak.profiler.client.util.ProgressAdapter
import io.dkozak.profiler.client.util.onUiThread
import io.dkozak.profiler.scanner.SimpleDiscScanner
import io.dkozak.profiler.scanner.fs.FsNode
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem
import mu.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger { }

class FileTreeModel : Controller() {

    private val discScanner = SimpleDiscScanner()

    val fileTreeProperty = SimpleObjectProperty<TreeItem<FsNode>>(this, "fileTree", null)

    fun newScan(rootDirectory: String, task: FXTask<*>) {
        val root = discScanner.newScan(rootDirectory, ProgressAdapter(task))
        fire(MessageEvent("Scan of '$rootDirectory' finished"))
        logger.info { "new fstree $root" }
        onUiThread {
            fileTreeProperty.set(root)
        }
    }

    fun partialScan(selectedNode: TreeItem<FsNode>, task: FXTask<*>) {
        TODO("not finished")
//        discScanner.partialScan(selectedNode, ProgressAdapter(task))
//        fire(MessageEvent("Rescan of '${selectedNode.file.absolutePath}' finished"))
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