package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.util.ProgressAdapter
import io.dkozak.profiler.client.util.onUiThread
import io.dkozak.profiler.scanner.SimpleDiscScanner
import io.dkozak.profiler.scanner.fs.DiskRoot
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.FsRoot
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import java.io.File

val dummy = DiskRoot(File("."), FsNode.DirectoryNode(File("."), FsNode.FileNode(File("gradlew"))))

class FileTreeModel : Controller() {

    private val discScanner = SimpleDiscScanner()

    val fileTreeProperty = SimpleObjectProperty<FsRoot>(this, "fileTree", dummy)

    fun newScan(rootDirectory: String, task: FXTask<*>) {
        val root = discScanner.newScan(rootDirectory, ProgressAdapter(task))
        fire(MessageEvent("Scan of '$rootDirectory' finished"))
        onUiThread {
            fileTreeProperty.set(root)
        }
    }

    fun partialScan(selectedNode: FsNode, task: FXTask<*>) {
        discScanner.partialScan(selectedNode, ProgressAdapter(task))
        fire(MessageEvent("Rescan of '${selectedNode.file.absolutePath}' finished"))
    }
}