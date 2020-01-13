package io.dkozak.profiler.client.viewmodel

import io.dkozak.profiler.client.model.FileTreeModel
import io.dkozak.profiler.scanner.fs.DiskRoot
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.WindowsRoot
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import mu.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger { }

class FileTreeViewModel : ViewModel() {
    private val fileTreeModel: FileTreeModel by inject()

    val fileTreeProperty = fileTreeModel.fileTreeProperty

    val selectedNodeContentProperty = FXCollections.observableArrayList<FsNode>()
    val selectedNodeNameProperty = SimpleStringProperty(this, "selectedNodeName", "")

    val parentDirectoryProperty = SimpleObjectProperty<FsNode.DirectoryNode>(this, "parentDirectory", null)

    init {
        fileTreeProperty.onChange { node ->
            when (node) {
                is WindowsRoot -> {
                    if (node.disks.isNotEmpty()) {
                        entrySelected(node.disks.first().node)
                    }
                }
                is DiskRoot -> entrySelected(node.node)
            }
        }
    }

    fun newScan(rootDirectory: String, task: FXTask<*>) {
        fileTreeModel.newScan(rootDirectory, task)
    }

    fun partialScan(selectedNode: FsNode, task: FXTask<*>) {
        fileTreeModel.partialScan(selectedNode, task)
    }

    fun entrySelected(node: FsNode) {
        when (node) {
            is FsNode.DirectoryNode -> {
                selectedNodeContentProperty.setAll(node.files)
                selectedNodeNameProperty.set(node.file.name)
                parentDirectoryProperty.set(node.parent)
            }
            is FsNode.FileNode -> logger.warn { "native file open not supported (yet), results in nop" }
        }
    }

    fun goToParent() {
        val parent = parentDirectoryProperty.get() ?: return
        entrySelected(parent)
    }
}