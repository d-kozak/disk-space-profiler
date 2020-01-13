package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.view.dialog.DeleteFileDialog
import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import javafx.stage.StageStyle
import tornadofx.*

class DirectoryView : View() {

    private val fileTreeViewModel: FileTreeViewModel by inject()

    override val root = vbox {
        borderpane {
            left {
                button("<=") {
                    enableWhen(fileTreeViewModel.selectedNodeParentProperty.isNotNull)
                    action {
                        fileTreeViewModel.goToParent()
                    }
                }
            }
            center {
                label(fileTreeViewModel.selectedNodeNameProperty) {
                    style {
                        padding = box(5.px)
                        fontWeight = FontWeight.BOLD
                    }
                }
            }
        }
        listview(fileTreeViewModel.selectedNodeContentProperty) {
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
                        KeyCode.ENTER -> fileTreeViewModel.entrySelected(selectedItem)
                        KeyCode.DELETE -> find<DeleteFileDialog>(mapOf(DeleteFileDialog::node to selectedItem)).openModal(stageStyle = StageStyle.UTILITY)
                    }
                }
            }

            onDoubleClick {
                fileTreeViewModel.entrySelected(selectedItem ?: return@onDoubleClick)
            }

            shortcut("alt+right") {
                fileTreeViewModel.entrySelected(selectedItem ?: return@shortcut)
            }

            shortcut("alt+left") {
                fileTreeViewModel.goToParent()
            }

            contextmenu {
                item("Refresh") {
                    action {
                        val rootItem = selectedItem ?: return@action
                        runAsync {
                            fileTreeViewModel.partialScan(rootItem, this)
                        }

                    }
                }
                item("Delete") {
                    action {
                        find<DeleteFileDialog>(mapOf(DeleteFileDialog::node to (selectedItem
                                ?: return@action))).openModal(stageStyle = StageStyle.UTILITY)
                    }
                }
            }
        }
    }
}