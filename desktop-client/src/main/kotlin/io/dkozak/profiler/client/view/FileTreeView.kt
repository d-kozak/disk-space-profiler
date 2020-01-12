package io.dkozak.profiler.client.view


import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.scanner.fs.DiskRoot
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.WindowsRoot
import javafx.scene.control.TreeItem
import javafx.scene.input.KeyCode
import tornadofx.*

class FileTreeView : View() {

    private val fileTreeViewModel: FileTreeViewModel by inject()

    override val root = treeview<Any> {
        root = TreeItem(fileTreeViewModel.fileTreeProperty.value)
        cellFormat {
            text = when (it) {
                is WindowsRoot -> "windows"
                is DiskRoot -> it.file.name
                is FsNode.DirectoryNode -> it.file.name
                is FsNode.FileNode -> it.file.name
                else -> "unknown $it"
            }
            onDoubleClick {
                itemSelected(treeItem.value)
            }
        }

        populate { parent ->
            when (val node = parent.value) {
                is WindowsRoot -> node.disks
                is DiskRoot -> node.node.files
                is FsNode.DirectoryNode -> node.files
                else -> null
            }
        }

        addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED) { event ->
            if ((event.code == KeyCode.ENTER) && !event.isMetaDown && this.selectionModel.selectedItems.size == 1) {
                val node = this.selectionModel.selectedItems[0]
                node.expandedProperty().set(true)
                itemSelected(node.value)
            }
        }

        contextmenu {
            item("Open") {
                action {
                    itemSelected(selectedValue ?: return@action)
                }
            }
            item("Refresh") {
                action {
                    val node = selectedValue ?: return@action
                    if (node is FsNode)
                        runAsync {
                            fileTreeViewModel.partialScan(node, this)
                        }
                    else {
                        println("invalid node $node")
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
            root.refresh()
        }
    }

    private fun itemSelected(item: Any) {
        if (item is FsNode)
            fileTreeViewModel.entrySelected(item)
        else if (item is DiskRoot)
            fileTreeViewModel.entrySelected(item.node)
    }
}