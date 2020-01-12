package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.scanner.model.DirectoryEntry
import io.dkozak.profiler.scanner.model.FileEntry
import io.dkozak.profiler.scanner.model.FileTreeEntry
import io.dkozak.profiler.scanner.model.RootEntry
import javafx.scene.control.TreeItem
import tornadofx.*

class FileTreeView : View() {

    private val fileTreeViewModel: FileTreeViewModel by inject()

    override val root = treeview<FileTreeEntry> {
        root = TreeItem(fileTreeViewModel.fileTreeProperty.value)
        cellFormat {
            text = when (it) {
                is RootEntry -> "/"
                is DirectoryEntry -> it.name
                is FileEntry -> it.name
            }
            onDoubleClick {
                fileTreeViewModel.entrySelected(treeItem.value)
            }
        }

        populate { parent ->
            when (val node = parent.value) {
                is RootEntry -> node.files
                is DirectoryEntry -> node.files
                is FileEntry -> null
            }
        }

        contextmenu {
            item("Open") {
                action {
                    val node = selectedValue ?: return@action
                    fileTreeViewModel.entrySelected(node)
                }
            }
            item("Refresh") {
                action {
                    val node = selectedValue ?: return@action
                    runAsync {
                        fileTreeViewModel.partialScan(node, this)
                    }
                }
            }
            item("Delete")
        }
    }

    init {
        fileTreeViewModel.fileTreeProperty.onChange {
            val fileTree = it ?: return@onChange
            root.rootProperty().set(TreeItem(fileTree))
        }
    }
}