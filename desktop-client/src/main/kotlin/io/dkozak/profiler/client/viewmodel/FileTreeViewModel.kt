package io.dkozak.profiler.client.viewmodel

import io.dkozak.profiler.client.model.FileTreeModel
import io.dkozak.profiler.client.util.DirectoryWatchService
import io.dkozak.profiler.client.util.onUiThread
import io.dkozak.profiler.scanner.ScanConfig
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.insertSorted
import io.dkozak.profiler.scanner.fs.removeSelfFromTree
import io.dkozak.profiler.scanner.util.toFileSize
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.TreeItem
import mu.KotlinLogging
import tornadofx.*
import java.io.File

private val logger = KotlinLogging.logger { }

class FileTreeViewModel : ViewModel() {
    private val fileTreeModel: FileTreeModel by inject()
    private val watchService = DirectoryWatchService(this)

    val fileTreeProperty = fileTreeModel.fileTreeProperty

    val selectedNodeContent = FXCollections.observableArrayList<TreeItem<FsNode>>()
    val selectedNodeNameProperty = SimpleStringProperty(this, "selectedNodeName", "")
    val selectedNodeParentProperty = SimpleObjectProperty<TreeItem<FsNode>>(this, "selectedNodeParent", null)

    val selectedNodeProperty = SimpleObjectProperty<TreeItem<FsNode>>(this, "selectedNode", null)

    init {
        fileTreeProperty.onChange { node ->
            if (node != null)
                entrySelected(node)
        }
    }

    fun newScan(rootDirectory: String, scanConfig: ScanConfig, task: FXTask<*>) {
        fileTreeModel.newScan(rootDirectory, scanConfig, task)
    }

    fun partialScan(selectedNode: TreeItem<FsNode>, task: FXTask<*>) {
        val newTree = fileTreeModel.partialScan(selectedNode, task)
        onUiThread {
            entrySelected(newTree)
        }
    }

    fun entrySelected(node: TreeItem<FsNode>) {
        when (val nodeInfo = node.value) {
            is FsNode.DirectoryNode -> {
                selectedNodeContent.setAll(node.children)
                selectedNodeParentProperty.set(node.parent)
                selectedNodeNameProperty.set(node.value.file.name)
                selectedNodeProperty.set(node)
                watchService.startWatching(nodeInfo.file)
            }
            is FsNode.FileNode -> logger.warn { "native file open not supported (yet), results in nop" }
        }
    }

    fun goToParent() {
        val parent = selectedNodeParentProperty.get()
        if (parent != null)
            entrySelected(parent)
        else logger.warn { "currently selected node has no parent" }
    }

    fun removeNode(node: TreeItem<FsNode>) {
        val removingThisDirectory = selectedNodeProperty.get() == node
        val removingChild = node in selectedNodeContent
        if (fileTreeModel.removeNode(node)) {
            if (removingThisDirectory) {
                val parent = selectedNodeParentProperty.get()
                if (parent != null) {
                    entrySelected(parent)
                } else {
                    selectedNodeNameProperty.set("")
                    selectedNodeParentProperty.set(null)
                    selectedNodeContent.clear()
                }
            } else if (removingChild) {
                selectedNodeContent.remove(node)
            }
        }
    }


    fun onFileCreated(file: File) {
        logger.info { "File created ${file.absolutePath}" }
        val parent = selectedNodeProperty.get()
        if (parent == null) {
            logger.warn { "No node selected, cannot insert" }
            return
        }
        parent.insertSorted(file)
        selectedNodeContent.setAll(parent.children)
    }

    fun onFileModified(file: File) {
        logger.info { "File modified ${file.absolutePath}" }
        val correspondingNode = locateNodeFor(file) ?: return
        if (correspondingNode.value is FsNode.FileNode) {

            correspondingNode.value.size = file.length().toFileSize()
            correspondingNode.parent?.children?.invalidate()
            selectedNodeContent.invalidate()
        }
    }

    fun onFileDeleted(file: File) {
        logger.info { "File deleted ${file.absolutePath}" }
        val correspondingNode = locateNodeFor(file) ?: return
        correspondingNode.removeSelfFromTree()
        selectedNodeContent.remove(correspondingNode)
    }

    private fun locateNodeFor(file: File): TreeItem<FsNode>? {
        val result = selectedNodeContent.find { it.value.file == file }
        if (result == null) {
            logger.warn { "Could not find corresponding node for $file in $selectedNodeContent" }
        }
        return result
    }
}