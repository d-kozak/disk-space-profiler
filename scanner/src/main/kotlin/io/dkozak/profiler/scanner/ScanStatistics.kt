package io.dkozak.profiler.scanner

import io.dkozak.profiler.scanner.fs.FsNode
import javafx.scene.control.TreeItem

data class ScanStatistics(
        val root: TreeItem<FsNode>,
        val time: Long
)