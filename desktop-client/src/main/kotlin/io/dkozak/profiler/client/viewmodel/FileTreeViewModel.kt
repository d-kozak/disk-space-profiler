package io.dkozak.profiler.client.viewmodel

import io.dkozak.profiler.client.model.FileTreeModel
import io.dkozak.profiler.scanner.fs.FsNode
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.*


class FileTreeViewModel : ViewModel() {
    private val fileTreeModel: FileTreeModel by inject()

    val fileTreeProperty = fileTreeModel.fileTreeProperty

    val selectedNodeContentProperty = FXCollections.observableArrayList<FsNode>()
    val selectedNodeNameProperty = SimpleStringProperty(this, "selectedNodeName", "")

    val parentDirectoryProperty = SimpleObjectProperty<FsNode.DirectoryNode>(this, "parentDirectory", null)

    fun newScan(rootDirectory: String, task: FXTask<*>) {
        fileTreeModel.newScan(rootDirectory, task)
    }

    fun partialScan(selectedNode: FsNode, task: FXTask<*>) {
        fileTreeModel.partialScan(selectedNode, task)
    }

    fun entrySelected(node: FsNode) {
        when (node) {
            is FsNode.DirectoryNode -> {
                selectedNodeContentProperty.clear()
                selectedNodeContentProperty.addAll(node.files)
                selectedNodeNameProperty.set(node.file.name)
                parentDirectoryProperty.set(node.parent)
            }
            is FsNode.FileNode -> {
                println("not supportted yet")
            }
        }
    }

    fun goToParent() {
        val parent = parentDirectoryProperty.get() ?: return
        entrySelected(parent)
    }
}