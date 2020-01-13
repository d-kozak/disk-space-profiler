package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.util.ProgressMonitor
import javafx.scene.control.TreeItem

class SimpleDiscScanner : DiscScanner {

    override fun newScan(root: String, monitor: ProgressMonitor): TreeItem<FsNode> = root.crawl(monitor)

    override fun partialScan(startNode: TreeItem<FsNode>, monitor: ProgressMonitor): TreeItem<FsNode> {
        val crawler = FsCrawler(startNode.value.diskRoot, monitor)
        return crawler.recursiveScan(startNode.value.file)
    }

}
