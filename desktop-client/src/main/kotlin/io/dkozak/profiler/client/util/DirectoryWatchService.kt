package io.dkozak.profiler.client.util

import io.dkozak.profiler.client.viewmodel.FileTreeViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import java.util.concurrent.TimeUnit


private val logger = KotlinLogging.logger { }

/**
 * Watches a gives directory for changes,
 * executes callbacks back to the FileTreeViewModel.
 * @see FileTreeViewModel
 */
class DirectoryWatchService(private val fileTreeViewModel: FileTreeViewModel) : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val watchService = FileSystems.getDefault().newWatchService()

    private val channel = Channel<File>(Channel.CONFLATED)

    init {
        runWatcherAsync()
    }

    fun startWatching(directory: File) {
        check(directory.isDirectory) { "file ${directory.absolutePath} is not a directory" }
        runBlocking { channel.send(directory) }
    }

    private fun runWatcherAsync() = async {

        channelLoop@ for (directory in channel) {
            val path = directory.toPath()

            var key: WatchKey = path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY)

            logger.info { "Watching directory ${directory.absolutePath}" }


            watcherLoop@ while (true) {
                if (!isActive) {
                    logger.info { "Terminating" }
                    key.cancel()
                    return@async
                }

                if (!channel.isEmpty) {
                    logger.info { "Stopped watching ${directory.absolutePath}" }
                    key.cancel()
                    continue@channelLoop
                }

                try {
                    var maybeKey = watchService.poll(100, TimeUnit.MILLISECONDS)
                    while (maybeKey != null) {
                        key = maybeKey
                        for (event in key.pollEvents()) {
                            if (!isActive || !channel.isEmpty) {
                                continue@watcherLoop
                            }
                            logger.info { "Event kind:${event.kind()}. File affected: ${event.context()}" }
                            val newFilePath = event.context() as? Path
                            if (newFilePath == null) {
                                logger.warn { "Could not get context for event" }
                                continue
                            }
                            val file = directory.toPath().resolve(newFilePath).toFile()
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
                        maybeKey = watchService.poll(100, TimeUnit.MILLISECONDS)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }
}