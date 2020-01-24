package io.dkozak.profiler.client.event

import io.dkozak.profiler.scanner.fs.FsNode
import javafx.scene.control.TreeItem
import tornadofx.*
import java.time.LocalDateTime

/**
 * Fired when any component wants to display a message to the user.
 */
data class MessageEvent(val text: String, val time: LocalDateTime = LocalDateTime.now()) : FXEvent()

/**
 * Send from the FileTreeModel when a new directory is loaded
 */
data class DirectoryLoadedEvent(val node: TreeItem<FsNode>) : FXEvent()