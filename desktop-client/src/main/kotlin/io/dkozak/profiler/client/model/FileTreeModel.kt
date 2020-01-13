package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.event.FileDeletedEvent
import io.dkozak.profiler.client.event.MessageEvent
import io.dkozak.profiler.client.util.ProgressAdapter
import io.dkozak.profiler.client.util.onUiThread
import io.dkozak.profiler.scanner.SimpleDiscScanner
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.FsRoot
import javafx.beans.property.SimpleObjectProperty
import mu.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger { }

class FileTreeModel : Controller() {

    private val discScanner = SimpleDiscScanner()

    val fileTreeProperty = SimpleObjectProperty<FsRoot>(this, "fileTree", null)

    init {
        subscribe<FileDeletedEvent> { event ->
            val parent = event.node.parent
            val currentRoot = fileTreeProperty.get() ?: return@subscribe
            if (parent != null) {
                if (!parent.files.remove(event.node)) {
                    logger.warn { "could not remove node $event.node, internal representation will be outdated now" }
                } else {
                    fileTreeProperty.set(currentRoot)
                }
            }
        }
    }

    fun newScan(rootDirectory: String, task: FXTask<*>) {
        val root = discScanner.newScan(rootDirectory, ProgressAdapter(task))
        fire(MessageEvent("Scan of '$rootDirectory' finished"))
        logger.info { "new fstree $root" }
        onUiThread {
            fileTreeProperty.set(root)
        }
    }

    fun partialScan(selectedNode: FsNode, task: FXTask<*>) {
        discScanner.partialScan(selectedNode, ProgressAdapter(task))
        fire(MessageEvent("Rescan of '${selectedNode.file.absolutePath}' finished"))
    }
}