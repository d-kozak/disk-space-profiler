package io.dkozak.profiler.client.viewmodel

import io.dkozak.profiler.client.model.FileTreeModel
import io.dkozak.profiler.scanner.model.DirectoryEntry
import io.dkozak.profiler.scanner.model.FileTreeEntry
import io.dkozak.profiler.scanner.model.RootEntry
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.*


class FileTreeViewModel : ViewModel() {
    private val fileTreeModel: FileTreeModel by inject()

    val fileTreeProperty = fileTreeModel.fileTreeProperty

    val selectedNodeContentProperty = FXCollections.observableArrayList<FileTreeEntry>()
    val selectedNodeNameProperty = SimpleStringProperty(this, "selectedNodeName", "")

    val parentDirectoryProperty = SimpleObjectProperty<FileTreeEntry>(this, "parrentDirectory", null)

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
                parentDirectoryProperty.set(entry.parent)
            }
            is RootEntry -> {
                selectedNodeContentProperty.clear()
                selectedNodeContentProperty.addAll(entry.files)
                selectedNodeNameProperty.set("/")
                parentDirectoryProperty.set(entry.parent)
            }
            else -> {
                System.err.println("not supported yet")
            }
        }
    }

    fun goToParent() {
        val parent = parentDirectoryProperty.get() ?: return
        entrySelected(parent)
    }
}