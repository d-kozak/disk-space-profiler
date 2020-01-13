package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.util.ProgressMonitor
import javafx.scene.control.TreeItem
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger { }

class SimpleDiscScanner : DiscScanner {

    override fun newScan(root: String, config: ScanConfig, monitor: ProgressMonitor): TreeItem<FsNode> {
        logger.info { "Executing newScan with $config" }
        val rootFile = File(root)
        check(rootFile.exists()) { "Given root file $this does not exist" }
        check(rootFile.isDirectory) { "Given root file $this is not a directory" }
        return FsCrawler(TreeItem(FsNode.DiskRoot(rootFile)), config, monitor).crawl()
    }

    override fun partialScan(startNode: TreeItem<FsNode>, config: ScanConfig, monitor: ProgressMonitor): TreeItem<FsNode> {
        logger.info { "Executing reScan with $config" }
        val crawler = FsCrawler(startNode.value.diskRoot, config, monitor)
        return crawler.recursiveScan(startNode.value.file)
    }

}
