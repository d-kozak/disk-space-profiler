package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.fs.file
import io.dkozak.profiler.scanner.fs.lazyNodeFor
import io.dkozak.profiler.scanner.util.scanSubtree
import javafx.scene.control.TreeItem
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import mu.KotlinLogging


/**
 * Configuration options for scanning
 */
data class ScanConfig(
        /**
         * Depth into which internal representation should be created, anything deeper is just scanned and summed up.
         */
        var treeDepth: Int = DEFAULT_TREE_DEPTH,
        var startNode: TreeItem<FsNode>
) {
    companion object {
        const val DEFAULT_TREE_DEPTH = 2
    }
}

data class AnalysisFinished(
        val root: TreeItem<FsNode>,
        val stats: ScanStats,
        var anyAnalysisRunning: Boolean = false,
        var errorMessage: String? = null
)

sealed class TreeUpdate {
    data class AddNodeRequest(
            val parent: TreeItem<FsNode>?,
            val newChild: TreeItem<FsNode>,
            val stats: ScanStats
    ) : TreeUpdate()

    data class ReplaceNodeRequest(
            val oldNode: TreeItem<FsNode>,
            val newNode: TreeItem<FsNode>,
            val stats: ScanStats
    ) : TreeUpdate()
}


data class ScanStats(
        val files: Long,
        val directories: Long,
        val time: Long
) {
    operator fun plus(other: ScanStats) = ScanStats(files + other.files, directories + other.directories, time + other.time)
}

fun CoroutineScope.startScannerManagerAsync(requestChannel: ReceiveChannel<ScanConfig>, treeUpdateChannel: SendChannel<TreeUpdate>, finishChannel: SendChannel<AnalysisFinished>) {
    val manager = ScannerManager(this, requestChannel, treeUpdateChannel, finishChannel)
    manager.startAsync()
}

private val logger = KotlinLogging.logger { }

private class ScannerManager(
        private val scope: CoroutineScope,
        private val requestChannel: ReceiveChannel<ScanConfig>,
        private val treeUpdateChannel: SendChannel<TreeUpdate>,
        private val finishChannel: SendChannel<AnalysisFinished>
) {

    private val scanningFinishedChannel = Channel<AnalysisFinished>(BUFFERED)

    private var job: Job? = null

    /**
     * Scan info is kept here instead of on the fs tree to make sure that it is confined to this coroutine only
     * and also because iterating over the fs tree from background thread is not thread-safe
     * instead, we can iterate over the running tasks, there should never be that many of them anyway
     */
    private val runningScans = mutableMapOf<String, Job>()


    fun startAsync() {
        check(job == null) { "only once instance of scanner manager should ever be executed" }
        job = scope.launch {
            while (!requestChannel.isClosedForReceive) {
                select<Unit> {
                    requestChannel.onReceiveOrNull { request ->
                        if (request != null) {
                            logger.info { request }
                            if (shouldScan(request.startNode)) {
                                stopSubScans(request.startNode)
                                runningScans[request.startNode.file.absolutePath] = scope.startScanAsync(request, treeUpdateChannel, scanningFinishedChannel)
                            }
                        }
                    }
                    scanningFinishedChannel.onReceiveOrNull { info ->
                        if (info != null) {
                            logger.info { info }
                            if (runningScans.remove(info.root.file.absolutePath) == null) {
                                logger.warn { "could not remove corresponding job for $info" }
                            }
                            finishChannel.send(info.copy(anyAnalysisRunning = runningScans.isNotEmpty()))
                        }
                    }
                }
                logger.info { "Remains ${runningScans.size} active scanner jobs" }
            }
            scanningFinishedChannel.close()
        }
    }

    /**
     * Scan should be executed if and only if no scan of the predecessor(including the node itself) of this node is being scanned (it would be redundant)
     */
    private fun shouldScan(startNode: TreeItem<FsNode>): Boolean {
        for (path in runningScans.keys) {
            if (startNode.file.absolutePath.startsWith(path)) {
                logger.info { "Found predecessor $path with active scan, scan for ${startNode.value.file.absolutePath} will not be executed" }
                return false
            }
        }
        return true
    }

    /**
     * Stops scans of all successors of current node, their result will be overwritten anyway
     */
    private fun stopSubScans(startNode: TreeItem<FsNode>) {
        runningScans.keys.filter { it.startsWith(startNode.file.absolutePath) }
                .forEach { runningScans.remove(it)?.cancel() }
    }
}


fun CoroutineScope.startScanAsync(scanConfig: ScanConfig, treeUpdateChannel: SendChannel<TreeUpdate>, scanningFinishedChannel: SendChannel<AnalysisFinished>) = launch {
    val start = System.currentTimeMillis()
    try {
        val scanStats = coroutineScope {
            logger.info { "Executing scan with config: $scanConfig" }
            val (fsTree, lazyDirs, stats) = crawlFileTree(scanConfig)
            treeUpdateChannel.send(TreeUpdate.ReplaceNodeRequest(scanConfig.startNode, fsTree, stats))

            val subtreeStats = lazyDirs.map {
                async {
                    val (subtreeSize, stats) = scanSubtree(it.file)
                    val newNode = lazyNodeFor(it.file).also { it.value.size += subtreeSize }
                    treeUpdateChannel.send(TreeUpdate.ReplaceNodeRequest(it, newNode, stats))
                    stats
                }
            }.awaitAll().fold(ScanStats(0, 0, 0), ScanStats::plus)
            subtreeStats + stats
        }
        scanningFinishedChannel.send(AnalysisFinished(scanConfig.startNode, scanStats))
    } catch (ex: Exception) {
        if (ex is CancellationException) throw ex
        scanningFinishedChannel.send(AnalysisFinished(scanConfig.startNode, ScanStats(0, 0, System.currentTimeMillis() - start), errorMessage = ex.message
                ?: "error"))
    }

}


