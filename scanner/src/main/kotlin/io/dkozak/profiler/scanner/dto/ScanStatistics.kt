package io.dkozak.profiler.scanner.dto

/**
 * Statistics about a scan.
 *
 * It can be either a partial scan ( for example of one lazy dir), or a complete scan.
 */
data class ScanStatistics(
        /**
         * Number of files encountered
         */
        val files: Long,
        /**
         * Number of directories encountered
         */
        val directories: Long,
        /**
         * How long the analysis took [ms]
         */
        val time: Long
) {
    /**
     * Aggregates two scan statictics together by summing up the details
     */
    operator fun plus(other: ScanStatistics) = ScanStatistics(files + other.files, directories + other.directories, time + other.time)
}