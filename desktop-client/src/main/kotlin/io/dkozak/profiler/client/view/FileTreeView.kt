package io.dkozak.profiler.client.view


import io.dkozak.profiler.client.view.dialog.DeleteFileDialog
import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.FsNodeByNameComparator
import io.dkozak.profiler.scanner.fs.FsNodeBySizeComparator
import io.dkozak.profiler.scanner.fs.isLazy
import javafx.scene.input.KeyCode
import javafx.stage.StageStyle
import mu.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger { }

/**
 * Displays the whole scanned file tree in a treeview.
 */
class FileTreeView : View() {

    private val fileTreeViewModel: FileTreeViewModel by inject()

    override val root = treeview<FsNode> {
        root = fileTreeViewModel.fileTreeProperty.value
        cellFormat {
            graphic = find<FileTreeNodeView>(mapOf(FileTreeNodeView::node to treeItem)).root
            onDoubleClick {
                if (treeItem.value !is FsNode.LazyNode)
                    fileTreeViewModel.openDirectory(treeItem)
            }
        }

        addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED) { event ->
            if (!event.isMetaDown && this.selectionModel.selectedItems.size == 1) {
                val node = this.selectionModel.selectedItems[0]
                when (event.code) {
                    KeyCode.ENTER -> {
                        node.expandedProperty().set(true)
                        fileTreeViewModel.openDirectory(node)
                    }
                    KeyCode.DELETE -> find<DeleteFileDialog>(mapOf(DeleteFileDialog::fileToDelete to node)).openModal(stageStyle = StageStyle.UTILITY)
                }
            }
        }

        contextmenu {
            item("Refresh") {
                action {
                    val node = selectionModel.selectedItems.firstOrNull() ?: return@action
                    runAsync {
                        fileTreeViewModel.rescanFrom(if (node.isLazy) node.parent else node, this)
                    }
                }
            }
            menu("Sort") {
                item("by name") {
                    action {
                        val item = selectionModel.selectedItems.firstOrNull()?.parent ?: return@action
                        item.children.sortWith(FsNodeByNameComparator)
                        item.value.comparator = FsNodeByNameComparator
                    }
                }
                item("by size") {
                    action {
                        val item = selectionModel.selectedItems.firstOrNull()?.parent ?: return@action
                        item.children.sortWith(FsNodeBySizeComparator)
                        item.value.comparator = FsNodeBySizeComparator
                    }
                }
            }
            item("Delete") {
                action {
                    val node = selectionModel.selectedItems.firstOrNull() ?: return@action
                    if (node.value is FsNode.DiskRoot) {
                        logger.warn { "Attempt to delete $selectedValue, this type is not supported" }
                    } else {
                        find<DeleteFileDialog>(mapOf(DeleteFileDialog::fileToDelete to node)).openModal(stageStyle = StageStyle.UTILITY)
                    }
                }
            }
        }
    }

    init {
        fileTreeViewModel.fileTreeProperty.onChange {
            val fileTree = it ?: return@onChange
            if (fileTree.children.isNotEmpty())
                fileTree.isExpanded = true
            root.rootProperty().set(fileTree)
            root.refresh()
        }
    }

}
