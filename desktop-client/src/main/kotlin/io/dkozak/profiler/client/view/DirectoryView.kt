package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.view.dialog.DeleteFileDialog
import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.FsNodeByNameComparator
import io.dkozak.profiler.scanner.fs.FsNodeBySizeComparator
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import javafx.stage.StageStyle
import tornadofx.*

/**
 * Displays the currently selected directory with it's content.
 */
class DirectoryView : View() {

    private val fileTreeViewModel: FileTreeViewModel by inject()

    override val root = vbox {
        borderpane {
            left {
                button {
                    graphic = imageview("back.png")
                    enableWhen(fileTreeViewModel.directoryParentProperty.isNotNull)
                    action {
                        fileTreeViewModel.goToParent()
                    }
                }
            }
            center {
                label(fileTreeViewModel.directoryNameProperty) {
                    style {
                        padding = box(5.px)
                        fontWeight = FontWeight.BOLD
                    }
                }
            }
        }
        listview(fileTreeViewModel.directoryContent) {
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }

            cellFormat {
                graphic = find<DirectoryNodeView>(mapOf(DirectoryNodeView::node to it)).root
            }

            addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED) { event ->
                val selectedItem = this.selectedItem
                if ((event.code == KeyCode.ENTER || event.code == KeyCode.DELETE) && !event.isMetaDown && selectedItem != null) {
                    when (event.code) {
                        KeyCode.ENTER -> fileTreeViewModel.openDirectory(selectedItem)
                        KeyCode.DELETE -> find<DeleteFileDialog>(mapOf(DeleteFileDialog::fileToDelete to selectedItem)).openModal(stageStyle = StageStyle.UTILITY)
                    }
                }
            }

            onDoubleClick {
                fileTreeViewModel.openDirectory(selectedItem ?: return@onDoubleClick)
            }

            shortcut("alt+right") {
                fileTreeViewModel.openDirectory(selectedItem ?: return@shortcut)
            }

            shortcut("alt+left") {
                fileTreeViewModel.goToParent()
            }

            contextmenu {
                item("Refresh") {
                    action {
                        var node = selectedItem ?: return@action
                        if (node.value is FsNode.LazyNode)
                            node = node.parent ?: node
                        runAsync {
                            fileTreeViewModel.rescanFrom(node, this)
                        }

                    }
                }
                menu("Sort") {
                    item("by name") {
                        action {
                            val item = selectedItem?.parent ?: return@action
                            item.children.sortWith(FsNodeByNameComparator)
                            item.value.comparator = FsNodeByNameComparator
                            fileTreeViewModel.directoryContent.setAll(item.children)
                        }
                    }
                    item("by size") {
                        action {
                            val item = selectedItem?.parent ?: return@action
                            item.children.sortWith(FsNodeBySizeComparator)
                            item.value.comparator = FsNodeBySizeComparator
                            fileTreeViewModel.directoryContent.setAll(item.children)
                        }
                    }
                }
                item("Delete") {
                    action {
                        find<DeleteFileDialog>(mapOf(DeleteFileDialog::fileToDelete to (selectedItem
                                ?: return@action))).openModal(stageStyle = StageStyle.UTILITY)
                    }
                }
            }
        }
    }
}