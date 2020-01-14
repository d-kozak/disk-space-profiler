package io.dkozak.profiler.client.util

import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey


private val logger = KotlinLogging.logger { }

/**
 * Watches a gives directory for changes,
 * executes callbacks back to the FileTreeViewModel.
 * @see FileTreeViewModel
 */
class DirectoryWatchService(val fileTreeViewModel: FileTreeViewModel) {

    private var lastTask: Deferred<Unit>? = null

    fun startWatching(directory: File) {
        check(directory.isDirectory) { "file ${directory.absolutePath} is not a directory" }
        lastTask?.cancel()
        lastTask = GlobalScope.async { watchDirectory(directory) }
    }

    private fun CoroutineScope.watchDirectory(directory: File) {
        val watchService = FileSystems.getDefault().newWatchService()
        val path = directory.toPath()

        path.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY)

        var key: WatchKey
        logger.info { "Watching directory ${directory.absolutePath}" }
        while (watchService.take().also { key = it } != null) {
            for (event in key.pollEvents()) {
                if (!isActive) {
                    logger.info { "Terminating for directory ${directory.absolutePath}" }
                    return
                }
                logger.info { "Event kind:${event.kind()}. File affected: ${event.context()}" }
                val path = event.context() as? Path
                if (path == null) {
                    logger.warn { "Could not get context for event" }
                    continue
                }
                val file = directory.toPath().resolve(path).toFile()
                logger.debug { "AbsolutePath ${file.absolutePath}" }
                onUiThread {
                    when (event.kind()) {
                        StandardWatchEventKinds.ENTRY_CREATE -> fileTreeViewModel.onFileCreated(file)
                        StandardWatchEventKinds.ENTRY_DELETE -> fileTreeViewModel.onFileDeleted(file)
                        StandardWatchEventKinds.ENTRY_MODIFY -> fileTreeViewModel.onFileModified(file)

                    }
                }
            }
            key.reset()
        }

    }

}