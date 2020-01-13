package io.dkozak.profiler.client.viewmodel

import io.dkozak.profiler.client.model.FileTreeModel
import io.dkozak.profiler.scanner.fs.FsNode
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.TreeItem
import mu.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger { }

class FileTreeViewModel : ViewModel() {
    private val fileTreeModel: FileTreeModel by inject()

    val fileTreeProperty = fileTreeModel.fileTreeProperty

    val selectedNodeContentProperty = FXCollections.observableArrayList<TreeItem<FsNode>>()
    val selectedNodeNameProperty = SimpleStringProperty(this, "selectedNodeName", "")
    val selectedNodeParentProperty = SimpleObjectProperty<TreeItem<FsNode>>(this, "selectedNodeParent", null)

    val selectedNodeProperty = SimpleObjectProperty<TreeItem<FsNode>>(this, "selectedNode", null)

    init {
        fileTreeProperty.onChange { node ->
            if (node != null)
                entrySelected(node)
        }
    }

    fun newScan(rootDirectory: String, task: FXTask<*>) {
        fileTreeModel.newScan(rootDirectory, task)
    }

    fun partialScan(selectedNode: TreeItem<FsNode>, task: FXTask<*>) {
        fileTreeModel.partialScan(selectedNode, task)
    }

    fun entrySelected(node: TreeItem<FsNode>) {
        when (val nodeInfo = node.value) {
            is FsNode.DirectoryNode -> {
                selectedNodeContentProperty.setAll(node.children)
                selectedNodeParentProperty.set(node.parent)
                selectedNodeNameProperty.set(node.value.file.name)
                selectedNodeProperty.set(node)
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
        val removingChild = node in selectedNodeContentProperty
        if (fileTreeModel.removeNode(node)) {
            if (removingThisDirectory) {
                val parent = selectedNodeParentProperty.get()
                if (parent != null) {
                    entrySelected(parent)
                } else {
                    selectedNodeNameProperty.set("")
                    selectedNodeParentProperty.set(null)
                    selectedNodeContentProperty.clear()
                }
            } else if (removingChild) {
                selectedNodeContentProperty.remove(node)
            }
        }
    }
}