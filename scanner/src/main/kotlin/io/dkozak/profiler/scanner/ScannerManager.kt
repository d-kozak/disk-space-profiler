package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.dto.*
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
 * Starts a ScannerManager coroutine
 */
fun CoroutineScope.startScannerManagerAsync(requestChannel: ReceiveChannel<ScanRequest>, treeUpdateRequestChannel: SendChannel<TreeUpdateRequest>, finishChannel: SendChannel<ScanResult>) {
    val manager = ScannerManager(this, requestChannel, treeUpdateRequestChannel, finishChannel)
    manager.startAsync()
}

private val logger = KotlinLogging.logger { }

/**
 * Scanner manager, keeps track of all running scans and maintains their execution
 */
private class ScannerManager(
        /**
         * scope in which new coroutine should be created
         */
        private val scope: CoroutineScope,
        /**
         * channel for accepting new requests
         */
        private val requestChannel: ReceiveChannel<ScanRequest>,
        /**
         * channel for sending tree updates
         */
        private val treeUpdateRequestChannel: SendChannel<TreeUpdateRequest>,
        /**
         * channel for sending finish signals
         */
        private val finishChannel: SendChannel<ScanResult>
) {

    /**
     * channel used internally to notify the
     */
    private val scanningFinishedChannel = Channel<ScanResult>(BUFFERED)

    /**
     * safety mechanism to ensure that only one instance is ever created
     * it is initially null and it holds a refecence to the manager coroutine after first exectuion of startAsync
     */
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
                        logger.info { request }
                        when (request) {
                            is ScanRequest.StartScan -> {
                                if (shouldScan(request.config.startNode)) {
                                    stopSubScans(request.config.startNode)
                                    runningScans[request.config.startNode.file.absolutePath] = scope.startScanAsync(request.config, treeUpdateRequestChannel, scanningFinishedChannel)
                                }
                            }
                            is ScanRequest.CancelScans -> cancelAllScans()
                        }
                    }
                    scanningFinishedChannel.onReceiveOrNull { info ->
                        logger.info { info }
                        if (info != null) {
                            if (runningScans.remove(info.startNode.file.absolutePath) == null) {
                                logger.warn { "could not remove corresponding job for $info" }
                            }
                            finishChannel.send(info.also { it.anyAnalysisRunning = runningScans.isNotEmpty() })
                        }
                    }
                }
                logger.info { "Remains ${runningScans.size} active scanner jobs" }
            }
            scanningFinishedChannel.close()
        }
    }

    /**
     * cancels all running scans
     */
    private fun cancelAllScans() {
        for (job in runningScans.values) {
            job.cancel()
        }
        runningScans.clear()
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

/**
 * starts a new coroutine and scans specified fs tree using it
 */
fun CoroutineScope.startScanAsync(scanConfig: ScanConfig, treeUpdateRequestChannel: SendChannel<TreeUpdateRequest>, scanningFinishedChannel: SendChannel<ScanResult>) = launch {
    try {
        val (fsTree, scanStats) = coroutineScope {
            logger.info { "Executing scan with config: $scanConfig" }
            val (fsTree, lazyDirs, stats) = walkFileTree(scanConfig)
            treeUpdateRequestChannel.send(TreeUpdateRequest.ReplaceNode(scanConfig.startNode, fsTree, stats))

            val subtreeStats = lazyDirs.map {
                async {
                    val (subtreeSize, stats) = scanSubtree(it.file)
                    val newNode = lazyNodeFor(it.file).also { it.value.size += subtreeSize }
                    treeUpdateRequestChannel.send(TreeUpdateRequest.ReplaceNode(it, newNode, stats))
                    stats
                }
            }.awaitAll().fold(ScanStatistics(0, 0, 0), ScanStatistics::plus)
            fsTree to subtreeStats + stats
        }
        scanningFinishedChannel.send(ScanResult.Success(fsTree, scanStats))
    } catch (ex: Exception) {
        if (ex is CancellationException) throw ex
        scanningFinishedChannel.send(ScanResult.Failure(scanConfig.startNode, ex.message ?: "error"))
    }

}


