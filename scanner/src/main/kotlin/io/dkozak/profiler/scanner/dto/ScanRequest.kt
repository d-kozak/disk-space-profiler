package io.dkozak.profiler.scanner.dto

/**
 * Requests that can be send to the ScannerManager
 */
sealed class ScanRequest {
    /**
     * To start a new scan.
     *
     * Note that the scan might not have to be started if any of the currently running scans would overwrite it's results.
     */
    data class StartScan(val config: ScanConfig) : ScanRequest()

    /**
     * Request to cancel all running scans
     * (there's no support for more granular cancels (yet)
     */
    object CancelScans : ScanRequest() {
        override fun toString(): String {
            return "CancelScanRequest"
        }
    }
}