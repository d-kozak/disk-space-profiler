package io.dkozak.profiler.scanner

import org.junit.jupiter.api.Test
import java.io.File

internal class SimpleDiskScannerTest {

    private val monitor = DummyProgressMonitor()

    @Test
    fun `scan this module`() {
        val path = "."
        if (File(path).isDirectory) {
            val scanner = SimpleDiskScanner()
            val diskRoot = scanner.newScan(path, DiskScanner.ScanConfig(treeDepth = Int.MAX_VALUE), monitor)
            println(diskRoot)
        }
    }

    @Test
    fun `scan the whole project`() {
        val path = ".."
        if (File(path).isDirectory) {
            val scanner = SimpleDiskScanner()
            val diskRoot = scanner.newScan(path, DiskScanner.ScanConfig(treeDepth = Int.MAX_VALUE), monitor)
            println(diskRoot)
        }
    }

    @Test
    fun `scan the while unix file system`() {
        val path = "/"
        if (File(path).isDirectory) {
            val scanner = SimpleDiskScanner()
            val diskRoot = scanner.newScan(path, DiskScanner.ScanConfig(), monitor)
            println(diskRoot)
        }
    }
}