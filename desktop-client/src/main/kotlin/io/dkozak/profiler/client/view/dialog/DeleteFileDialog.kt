package io.dkozak.profiler.client.view.dialog


import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.scanner.fs.FsNode
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.TreeItem
import tornadofx.*

/**
 * Confirmation dialog for deleting files
 */
class DeleteFileDialog : Fragment() {

    val fileToDelete: TreeItem<FsNode> by param()

    private val fileTreeViewModel: FileTreeViewModel by inject()

    override val root: Parent = borderpane {
        title = "Delete ${if (fileToDelete.value.file.isDirectory) "directory" else "file"} ${fileToDelete.value.file.name}"

        style {
            padding = box(5.px)
        }

        center {
            label("Are you sure that you want to delete ${if (fileToDelete.value.file.isDirectory) "directory" else "file"} ${fileToDelete.value.file.name}") {
                style {
                    padding = box(10.px)
                }
            }
        }
        bottom {
            hbox(4) {
                alignment = Pos.CENTER_RIGHT
                button("Cancel") {
                    action {
                        close()
                    }
                }
                button("Delete") {
                    action {
                        fileTreeViewModel.removeNode(fileToDelete)
                        close()
                    }
                }
            }
        }
    }


}