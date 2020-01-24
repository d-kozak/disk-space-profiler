package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.event.DirectoryLoadedEvent
import io.dkozak.profiler.client.event.MessageEvent
import io.dkozak.profiler.client.util.onUiThread
import io.dkozak.profiler.scanner.dto.*
import io.dkozak.profiler.scanner.fs.*
import io.dkozak.profiler.scanner.startScannerManagerAsync
import io.dkozak.profiler.scanner.util.UiThread
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.selects.select
import mu.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger { }

/**
 * Maintains fsTree displayed in the app.
 * Updates it using DiskScanner.
 */
class FileTreeModel : Controller(), CoroutineScope by CoroutineScope(Dispatchers.Default) {

    /**
     * Root of the file tree displayed in the app
     */
    val rootProperty = SimpleObjectProperty<TreeItem<FsNode>>(this, "fileTree", null)


    val anyAnalysisRunningProperty = SimpleBooleanProperty(this, "anyAnalysisRunning", false)

    private val requestChannel = Channel<ScanRequest>(BUFFERED)
    private val treeUpdateChannel = Channel<TreeUpdateRequest>(BUFFERED)
    private val finishChannel = Channel<ScanResult>(BUFFERED)

    init {
        beforeShutdown {
            this.cancel()
        }

        startScannerManagerAsync(requestChannel, treeUpdateChannel, finishChannel)
        launch {
            while (true) {
                select<Unit> {
                    treeUpdateChannel.onReceiveOrNull { update ->
                        if (update != null) {
                            logger.info { update }
                            onUiThread {
                                when (update) {
                                    is TreeUpdateRequest.AddChild -> {
                                        registerExpandListeners(update.newChild)
                                        if (update.parent != null) {
                                            update.parent!!.insertSorted(update.newChild)
                                        } else {
                                            rootProperty.set(update.newChild)
                                        }
                                        progressMessage(update.newChild.file.absolutePath, update.stats)
                                    }
                                    is TreeUpdateRequest.ReplaceNode -> {
                                        registerExpandListeners(update.newNode)
                                        if (update.oldNode.parent != null)
                                            update.oldNode.replaceWith(update.newNode)
                                        else rootProperty.set(update.newNode)
                                        progressMessage(update.newNode.file.absolutePath, update.stats)
                                    }
                                }
                            }

                        }
                    }
                    finishChannel.onReceiveOrNull { info ->
                        logger.info { info }
                        when (info) {
                            is ScanResult.Success -> {
                                if (info.startNode.isDirectory)
                                    fire(DirectoryLoadedEvent(info.startNode))
                                fire(MessageEvent("Analysis of ${info.startNode.file.absolutePath} finished, found ${info.stats.files} files and ${info.stats.directories} directories, it took ${info.stats.time} ms."))
                            }
                            is ScanResult.Failure -> {
                                fire(MessageEvent("Analysis of ${info.startNode.file.absolutePath} failed: ${info.message}."))
                            }
                        }
                        anyAnalysisRunningProperty.set(info?.anyAnalysisRunning ?: false)

                    }
                }
            }
        }
    }


    /**
     * Execute new scan.
     * @param scanConfig configuration
     */
    @UiThread
    fun newScan(scanConfig: ScanConfig) {
        fire(MessageEvent("Scan of '${scanConfig.startNode.file.absolutePath}' started"))
        runBlocking { requestChannel.send(ScanRequest.StartScan(scanConfig)) }
        anyAnalysisRunningProperty.set(true)
    }

    /**
     * Rescan the disk starting from specified node.
     * @param selectedNode from where to start
     * @task current task
     */
    @UiThread
    fun rescanFrom(selectedNode: TreeItem<FsNode>) {
        fire(MessageEvent("Scan of '${selectedNode.file.absolutePath}' started"))
        runBlocking { requestChannel.send(ScanRequest.StartScan(ScanConfig(startNode = selectedNode))) }
        anyAnalysisRunningProperty.set(true)
    }


    /**
     * Registers expand listener for lazy node in the tree, so that rescan is triggered automatically when such node is visible
     */
    @UiThread
    private fun registerExpandListeners(node: TreeItem<FsNode>) {
        if (node.isLazyDir) {
            node.expandedProperty().onChange { if (it) rescanFrom(node) }
        } else if (node.isDirectory) {
            node.children.forEach(::registerExpandListeners)
        }
    }

    /**
     * Remove specified file or directory.
     * @param node to remove
     */
    fun removeNode(node: TreeItem<FsNode>): Boolean {
        if (!node.value.file.deleteRecursively()) {
            val errorMessage = "Failed to delete file ${node.file.absolutePath}"
            logger.warn { errorMessage }
            error("Delete failed", errorMessage)
            fire(MessageEvent(errorMessage))
            return false
        }
        node.detachFromTree()
        fire(MessageEvent("${if (node.value is FsNode.DirectoryNode) "Directory" else "File"} ${node.value.file.name} deleted"))
        return true
    }

    private fun progressMessage(path: String, stats: ScanStatistics) {
        fire(MessageEvent("$path: ${stats.files} files,  ${stats.directories} directories, took ${stats.time} ms"))
    }

    /**
     * cancels all running scans
     */
    fun cancelScans(): Unit = runBlocking {
        requestChannel.send(ScanRequest.CancelScans)
        anyAnalysisRunningProperty.set(false)
    }

}


