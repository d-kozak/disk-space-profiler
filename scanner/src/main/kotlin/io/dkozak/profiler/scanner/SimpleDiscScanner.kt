package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.DiskRoot
import io.dkozak.profiler.scanner.fs.FsNode
import io.dkozak.profiler.scanner.util.ProgressMonitor

class SimpleDiscScanner : DiscScanner {

    override fun newScan(root: String, monitor: ProgressMonitor): DiskRoot = root.crawl(monitor)

    override fun partialScan(startNode: FsNode, monitor: ProgressMonitor): FsNode {
        val crawler = FsCrawler(startNode.disk, monitor)
        return crawler.recursiveScan(startNode.file, startNode.parent)
    }

}
