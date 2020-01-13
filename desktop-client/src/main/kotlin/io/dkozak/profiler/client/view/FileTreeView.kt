package io.dkozak.profiler.client.view


import io.dkozak.profiler.client.view.dialog.DeleteFileDialog
import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.scanner.fs.FsNode
import javafx.scene.input.KeyCode
import javafx.stage.StageStyle
import mu.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger { }

class FileTreeView : View() {

    private val fileTreeViewModel: FileTreeViewModel by inject()

    override val root = treeview<FsNode> {
        root = fileTreeViewModel.fileTreeProperty.value
        cellFormat {
            text = when (it) {
                is FsNode.DirectoryNode -> it.file.name
                is FsNode.FileNode -> it.file.name
                else -> "unknown $it"
            }
            onDoubleClick {
                fileTreeViewModel.entrySelected(treeItem)
            }
        }

        addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED) { event ->
            if ((event.code == KeyCode.ENTER) && !event.isMetaDown && this.selectionModel.selectedItems.size == 1) {
                val node = this.selectionModel.selectedItems[0]
                node.expandedProperty().set(true)
                fileTreeViewModel.entrySelected(node)
            }
        }

        contextmenu {
            item("Refresh") {
                action {
                    val node = selectionModel.selectedItems.firstOrNull() ?: return@action
                    runAsync {
                        fileTreeViewModel.partialScan(node, this)
                    }
                }
            }
            item("Delete") {
                action {
                    val node = selectionModel.selectedItems.firstOrNull() ?: return@action
                    if (node.value is FsNode.DiskRoot) {
                        logger.warn { "Attempt to delete $selectedValue, this type is not supported" }
                    } else {
                        find<DeleteFileDialog>(mapOf(DeleteFileDialog::node to node)).openModal(stageStyle = StageStyle.UTILITY)
                    }
                }
            }
        }
    }

    init {
        fileTreeViewModel.fileTreeProperty.onChange {
            val fileTree = it ?: return@onChange
            root.rootProperty().set(fileTree)
            root.refresh()
        }
    }

}