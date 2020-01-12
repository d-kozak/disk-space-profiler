package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.util.ProgressAdapter
import io.dkozak.profiler.scanner.SimpleDiscScanner
import io.dkozak.profiler.scanner.model.*
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

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
).setParentReferences()

class FileTreeModel : Controller() {

    private val discScanner = SimpleDiscScanner()

    val fileTreeProperty = SimpleObjectProperty(this, "fileTree", dummyFileRoot)

    fun newScan(rootDirectory: String, task: FXTask<*>) {
        discScanner.newScan(rootDirectory, ProgressAdapter(task))
        fire(MessageEvent("Scan of '$rootDirectory' finished"))
    }

    fun partialScan(selectedNode: FileTreeEntry, task: FXTask<*>) {
        discScanner.partialScan(selectedNode, fileTreeProperty.value, ProgressAdapter(task))
        fire(MessageEvent("Rescan of '${selectedNode.fullPath}' finished"))
    }
}