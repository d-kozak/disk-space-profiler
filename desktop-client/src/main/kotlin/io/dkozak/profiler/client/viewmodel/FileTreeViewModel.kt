package io.dkozak.profiler.client.viewmodel

import io.dkozak.profiler.client.model.FileTreeModel
import io.dkozak.profiler.scanner.model.DirectoryEntry
import io.dkozak.profiler.scanner.model.FileTreeEntry
import io.dkozak.profiler.scanner.model.RootEntry
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.*


class FileTreeViewModel : ViewModel() {
    private val fileTreeModel: FileTreeModel by inject()

    val fileTreeProperty = fileTreeModel.fileTreeProperty

    val selectedNodeContentProperty = FXCollections.observableArrayList<FileTreeEntry>()
    val selectedNodeNameProperty = SimpleStringProperty(this, "selectedNodeName", "")

    fun newScan(rootDirectory: String, task: FXTask<*>) {
        fileTreeModel.newScan(rootDirectory, task)
    }

    fun partialScan(selectedNode: FileTreeEntry, task: FXTask<*>) {
        fileTreeModel.partialScan(selectedNode, task)
    }

    fun entrySelected(entry: FileTreeEntry) {
        when (entry) {
            is DirectoryEntry -> {
                selectedNodeContentProperty.clear()
                selectedNodeContentProperty.addAll(entry.files)
                selectedNodeNameProperty.set(entry.name)
            }
            is RootEntry -> {
                selectedNodeContentProperty.clear()
                selectedNodeContentProperty.addAll(entry.files)
                selectedNodeNameProperty.set("/")
            }
            else -> {
                System.err.println("not supported yet")
            }
        }
    }

    fun goBack() {
        println("back")
    }
}