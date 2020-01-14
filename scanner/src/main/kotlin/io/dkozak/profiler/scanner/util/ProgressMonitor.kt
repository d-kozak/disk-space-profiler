package io.dkozak.profiler.scanner.util

/**
 * Component used from DiskScanner to notify it's client about the state of the scan process.
 */
interface ProgressMonitor {
    /**
     * Send a message
     */
    fun message(text: String)

    /**
     * Advance progress
     */
    @Cleanup("this is not used (?yet?)")
    fun progress(number: Long, max: Long)
}