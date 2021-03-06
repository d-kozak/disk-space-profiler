package io.dkozak.profiler.client.viewmodel

import io.dkozak.profiler.client.event.DirectoryLoadedEvent
import io.dkozak.profiler.client.model.FileTreeModel
import io.dkozak.profiler.client.util.DirectoryWatchService
import io.dkozak.profiler.scanner.dto.ScanConfig
import io.dkozak.profiler.scanner.fs.*
import io.dkozak.profiler.scanner.util.toFileSize
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.TreeItem
import kotlinx.coroutines.cancel
import mu.KotlinLogging
import tornadofx.*
import java.io.File

private val logger = KotlinLogging.logger { }

/**
 * Cotains all properties for FileTreView and DirectoryView
 * @see FileTreeView
 * @see DirectoryView
 */
class FileTreeViewModel : ViewModel() {
    private val fileTreeModel: FileTreeModel by inject()
    private val watchService = DirectoryWatchService(this)

    /**
     * Root of the file tree
     */
    val fileTreeProperty = fileTreeModel.rootProperty

    /**
     * currently selected directory
     */
    val directoryProperty = SimpleObjectProperty<TreeItem<FsNode>>(this, "selectedNode", null)

    /**
     * Three properties dependent on currently selected directory, exposed for data binding.
     */
    val directoryContent = FXCollections.observableArrayList<TreeItem<FsNode>>()
    val directoryNameProperty = SimpleStringProperty(this, "selectedNodeName", "")
    val directoryParentProperty = SimpleObjectProperty<TreeItem<FsNode>>(this, "selectedNodeParent", null)


    /**
     * directory that should be displayed once it is available, or null if not waiting for any
     */
    private var wantedDirectory: TreeItem<FsNode>? = null

    init {
        fileTreeProperty.onChange { node ->
            if (node != null)
                openDirectory(node)
        }

        subscribe<DirectoryLoadedEvent> {
            if (wantedDirectory?.value == it.node.value) {
                openDirectory(it.node)
                wantedDirectory = null
            }
        }

        beforeShutdown {
            watchService.cancel()
        }
    }

    /**
     * Execute new scan.
     * @param scanConfig configuration
     */
    fun newScan(scanConfig: ScanConfig) {
        fileTreeModel.newScan(scanConfig)
    }

    /**
     * Rescan the disk starting from specified node.
     * @param selectedNode from where to start
     */
    fun rescanFrom(selectedNode: TreeItem<FsNode>) {
        fileTreeModel.rescanFrom(selectedNode)
    }

    /**
     * Open currently selected directory
     */
    fun openDirectory(node: TreeItem<FsNode>) {
        if (!node.isDirectory) {
            logger.warn { "node $node is not a directory" }
            return
        }
        if (node.isLazy) {
            wantedDirectory = node
            fileTreeModel.rescanFrom(node)
        }

        directoryContent.setAll(node.children)
        directoryParentProperty.set(node.parent)
        directoryNameProperty.set(node.value.file.name)
        directoryProperty.set(node)
        watchService.startWatching(node.file)
    }

    /**
     * Go to the parent directory
     */
    fun goToParent() {
        val parent = directoryParentProperty.get()
        if (parent != null)
            openDirectory(parent)
        else logger.warn { "currently selected node has no parent" }
    }

    /**
     * Remove specified file or directory.
     * @param node to remove
     */
    fun removeNode(node: TreeItem<FsNode>) {
        val removingThisDirectory = directoryProperty.get() == node
        val removingChild = node in directoryContent
        if (fileTreeModel.removeNode(node)) {
            if (removingThisDirectory) {
                val parent = directoryParentProperty.get()
                if (parent != null) {
                    openDirectory(parent)
                } else {
                    directoryNameProperty.set("")
                    directoryParentProperty.set(null)
                    directoryContent.clear()
                }
            } else if (removingChild) {
                directoryContent.remove(node)
            }
        }
    }

    /**
     * Callback from DirectoryWatchService executed when a new file is created
     */
    fun onFileCreated(file: File) {
        logger.info { "File created ${file.absolutePath}" }
        val parent = directoryProperty.get()
        if (parent == null) {
            logger.warn { "No node selected, cannot insert" }
            return
        }
        parent.insertSorted(file)
        directoryContent.setAll(parent.children)
    }

    /**
     * Callback from DirectoryWatchService executed when a file is modified
     */
    fun onFileModified(file: File) {
        logger.info { "File modified ${file.absolutePath}" }
        val correspondingNode = locateNodeFor(file) ?: return
        if (correspondingNode.value is FsNode.FileNode) {
            correspondingNode.parent?.children?.invalidate()
            directoryContent.invalidate()
            correspondingNode.propagateSizeUp(file.length().toFileSize() - correspondingNode.value.size)
        }
    }

    /**
     * Callback from DirectoryWatchService executed when a file is deleted
     */
    fun onFileDeleted(file: File) {
        logger.info { "File deleted ${file.absolutePath}" }
        val correspondingNode = locateNodeFor(file) ?: return
        correspondingNode.detachFromTree()
        directoryContent.remove(correspondingNode)
    }

    private fun locateNodeFor(file: File): TreeItem<FsNode>? {
        val result = directoryContent.find { it.value.file == file }
        if (result == null) {
            logger.warn { "Could not find corresponding node for $file in $directoryContent" }
        }
        return result
    }
}