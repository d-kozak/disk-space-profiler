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
)

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

    private val runningScans = mutableMapOf<String, Job>()
    private val scanningFinishedChannel = Channel<AnalysisFinished>(BUFFERED)

    private var job: Job? = null

    fun startAsync() {
        check(job == null) { "only once instance of scanner manager should ever be executed" }
        job = scope.launch {
            while (!requestChannel.isClosedForReceive) {
                select<Unit> {
                    requestChannel.onReceiveOrNull { request ->
                        if (request != null) {
                            logger.info { request }
                            // todo cancel subscans
                            runningScans[request.startNode.file.absolutePath] = scope.startScanAsync(request, treeUpdateChannel, scanningFinishedChannel)
                        }
                    }
                    scanningFinishedChannel.onReceiveOrNull { info ->
                        if (info != null) {
                            logger.info { info }
                            val job = runningScans.remove(info.root.file.absolutePath)
                            if (job != null) {
                                logger.info { "Waiting for job to cancel" }
                                job.cancelAndJoin()
                                logger.info { "Job finished" }
                            } else {
                                logger.warn { "scanner for ${info.root.file.absolutePath} not found" }
                            }
                            finishChannel.send(info.copy(anyAnalysisRunning = runningScans.isNotEmpty()))
                        }
                    }
                }
            }
            scanningFinishedChannel.close()
        }
    }
}


fun CoroutineScope.startScanAsync(scanConfig: ScanConfig, treeUpdateChannel: SendChannel<TreeUpdate>, scanningFinishedChannel: SendChannel<AnalysisFinished>) = async {
    val start = System.currentTimeMillis()
    coroutineScope {
        logger.info { "Executing scan with config: $scanConfig" }
        val (fsTree, lazyDirs, stats) = crawlFileTree(scanConfig)
        treeUpdateChannel.send(TreeUpdate.ReplaceNodeRequest(scanConfig.startNode, fsTree, stats))

        lazyDirs.map {
            async {
                val (subtreeSize, stats) = scanSubtree(it.file)
                val newNode = lazyNodeFor(it.file).also { it.value.size += subtreeSize }
                treeUpdateChannel.send(TreeUpdate.ReplaceNodeRequest(it, newNode, stats))
            }
        }
    }
    scanningFinishedChannel.send(AnalysisFinished(scanConfig.startNode, ScanStats(0, 0, System.currentTimeMillis() - start)))
}


