package io.dkozak.profiler.client.view

import io.dkozak.profiler.client.viewmodel.runScan
import io.dkozak.profiler.scanner.model.DirectoryEntry
import io.dkozak.profiler.scanner.model.FileEntry
import io.dkozak.profiler.scanner.model.FileTreeEntry
import io.dkozak.profiler.scanner.model.RootEntry
import javafx.scene.control.TreeItem
import tornadofx.*

class FileTreeView : View() {

    private val dummyFileRoot: FileTreeEntry = RootEntry(
            FileEntry("one.txt"),
            DirectoryEntry("dir1",
                    FileEntry("img.jpg"),
                    DirectoryEntry("inner dir",
                            FileEntry("secret.txt")
                    )
            ),
            DirectoryEntry("dir2",
                    FileEntry("img2.png")
            ),
            FileEntry("foo.dat")
    )

    override val root = treeview<FileTreeEntry> {
        root = TreeItem(dummyFileRoot)
        cellFormat {
            text = when (it) {
                is RootEntry -> "/"
                is DirectoryEntry -> it.name
                is FileEntry -> it.name
            }
        }

        populate { parent ->
            when (val node = parent.value) {
                is RootEntry -> node.files
                is DirectoryEntry -> node.files
                is FileEntry -> null
            }
        }

        contextmenu {
            item("Open")
            item("Refresh") {
                action {
                    val rootItem = selectedValue ?: return@action
                    runAsync {
                        runScan(rootItem.fullPath)
                    }
                }
            }
            item("Delete")
        }
    }

}