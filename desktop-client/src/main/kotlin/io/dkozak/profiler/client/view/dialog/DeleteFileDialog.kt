package io.dkozak.profiler.client.view.dialog


import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import io.dkozak.profiler.scanner.fs.FsNode
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.TreeItem
import tornadofx.*

class DeleteFileDialog : Fragment() {
    val node: TreeItem<FsNode> by param()

    val fileTreeViewModel: FileTreeViewModel by inject()

    override val root: Parent = borderpane {
        title = "Delete ${if (node.value.file.isDirectory) "directory" else "file"} ${node.value.file.name}"
        center {
            label("Are you sure that you want to delete ${if (node.value.file.isDirectory) "directory" else "file"} ${node.value.file.name}")
        }
        bottom {
            hbox {
                alignment = Pos.CENTER_RIGHT
                button("Cancel") {
                    action {
                        close()
                    }
                }
                button("Delete") {
                    action {
                        fileTreeViewModel.removeNode(node)
                        close()
                    }
                }
            }
        }
    }


}