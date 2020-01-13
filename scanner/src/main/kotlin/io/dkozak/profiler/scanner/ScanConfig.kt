package io.dkozak.profiler.scanner


data class ScanConfig(var treeDepth: Int = DEFAULT_TREE_DEPTH) {
    companion object {
        val DEFAULT_TREE_DEPTH = 2
    }
}