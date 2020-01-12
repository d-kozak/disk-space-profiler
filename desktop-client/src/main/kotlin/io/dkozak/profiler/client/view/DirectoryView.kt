package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.model.FileTreeModel
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import tornadofx.*

class DirectoryView : View() {

    private val fileTreeModel: FileTreeModel by inject()

    private val dummyFiles = listOf("File 1", "File 2", "Dir 1", "File 3", "Dir 2").asObservable()

    override val root = vbox {
        alignment = Pos.CENTER
        label("dir1") {
            style {
                padding = box(5.px)
                fontWeight = FontWeight.BOLD
            }
        }
        listview(dummyFiles) {
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
            contextmenu {
                item("Open")
                item("Refresh") {
                    action {
                        val rootItem = selectedItem ?: return@action
                        runAsync {
                            fileTreeModel.scan(rootItem, this)
                        }

                    }
                }
                item("Delete")
            }
        }
    }
}