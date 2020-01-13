package io.dkozak.profiler.client.view


import io.dkozak.profiler.client.view.dialog.DeleteFileDialog
import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.FsNodeByNameComparator
import io.dkozak.profiler.scanner.fs.FsNodeBySizeComparator
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
            graphic = find<FileTreeNodeView>(mapOf(FileTreeNodeView::node to treeItem)).root
            onDoubleClick {
                if (treeItem.value !is FsNode.LazyNode)
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
                    var node = selectionModel.selectedItems.firstOrNull() ?: return@action
                    if (node.value is FsNode.LazyNode)
                        node = node.parent ?: node
                    runAsync {
                        fileTreeViewModel.partialScan(node, this)
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
                        find<DeleteFileDialog>(mapOf(DeleteFileDialog::node to node)).openModal(stageStyle = StageStyle.UTILITY)
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
