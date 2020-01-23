package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.view.dialog.openDeleteFileDialog
import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.scanner.fs.FsNodeByNameComparator
import io.dkozak.profiler.scanner.fs.FsNodeBySizeComparator
import io.dkozak.profiler.scanner.fs.isLazyFile
import io.dkozak.profiler.scanner.fs.isNotLazy
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
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
                if (!event.isMetaDown && selectedItem != null) {
                    when (event.code) {
                        KeyCode.ENTER -> fileTreeViewModel.openDirectory(selectedItem)
                        KeyCode.DELETE -> openDeleteFileDialog(selectedItem)
                    }
                }
            }

            onDoubleClick {
                val item = selectedItem ?: return@onDoubleClick
                if (item.isNotLazy)
                    fileTreeViewModel.openDirectory(item)
            }

            shortcut("alt+right") {
                val item = selectedItem ?: return@shortcut
                if (item.isNotLazy)
                    fileTreeViewModel.openDirectory(item)
            }

            shortcut("alt+left") {
                fileTreeViewModel.goToParent()
            }

            contextmenu {
                item("Refresh") {
                    action {
                        val node = selectedItem ?: return@action
                        runAsync {
                            fileTreeViewModel.rescanFrom(if (node.isLazyFile) node.parent else node, this)
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
                        openDeleteFileDialog(selectedItem ?: return@action)
                    }
                }
            }
        }
    }
}