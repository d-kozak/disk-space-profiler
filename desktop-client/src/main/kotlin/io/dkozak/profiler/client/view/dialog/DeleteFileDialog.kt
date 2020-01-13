package io.dkozak.profiler.client.view.dialog


import io.dkozak.profiler.client.event.FileDeletedEvent
import io.dkozak.profiler.scanner.fs.FsNode
import javafx.geometry.Pos
import javafx.scene.Parent
import mu.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger { }

class DeleteFileDialog : Fragment() {
    val node: FsNode by param()

    override val root: Parent = borderpane {
        title = "Delete ${if (node.file.isDirectory) "directory" else "file"} ${node.file.name}"
        center {
            label("Are you sure that you want to delete ${if (node.file.isDirectory) "directory" else "file"} ${node.file.name}")
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
                        if (!node.file.deleteRecursively()) {
                            logger.warn { "failed to delete file ${node.file.absolutePath}" }
                        } else {
                            fire(FileDeletedEvent(node))
                        }
                        close()
                    }
                }
            }
        }
    }


}