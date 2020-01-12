package io.dkozak.profiler.scanner

import org.junit.jupiter.api.Test

internal class SimpleDiscScannerTest {

    private val monitor = DummyProgressMonitor()

    @Test
    fun `scan this module`() {
        val scanner = SimpleDiscScanner()
        val diskRoot = scanner.newScan(".", monitor)
        println(diskRoot)
    }

    @Test
    fun `scan the whole project`() {
        val scanner = SimpleDiscScanner()
        val diskRoot = scanner.newScan("..", monitor)
        println(diskRoot)
    }

    @Test
    fun `scan the while unit file system`() {
        val scanner = SimpleDiscScanner()
        val diskRoot = scanner.newScan("/", monitor)
        println(diskRoot)
    }
}