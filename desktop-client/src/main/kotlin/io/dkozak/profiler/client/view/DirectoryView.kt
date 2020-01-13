package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.scanner.fs.FsNode
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import tornadofx.*

class DirectoryView : View() {

    private val fileTreeViewModel: FileTreeViewModel by inject()

    override val root = vbox {
        borderpane {
            left {
                button("<=") {
                    enableWhen(fileTreeViewModel.parentDirectoryProperty.isNotNull)
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
                graphic = hbox {
                    when (it) {
                        is FsNode.DirectoryNode -> {
                            label("dir ${it.file.name}")
                        }
                        is FsNode.FileNode -> {
                            label("file ${it.file.name}")
                        }
                    }
                }

            }

            addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED) { event ->
                val selectedItem = this.selectedItem
                if ((event.code == KeyCode.ENTER) && !event.isMetaDown && selectedItem != null) {
                    fileTreeViewModel.entrySelected(selectedItem)
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
                item("Delete")
            }
        }
    }
}