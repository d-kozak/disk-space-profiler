package io.dkozak.profiler.client.event

import io.dkozak.profiler.scanner.fs.FsNode
import tornadofx.*
import java.time.LocalDateTime

data class MessageEvent(val text: String, val time: LocalDateTime = LocalDateTime.now()) : FXEvent()

data class FileDeletedEvent(val node: FsNode) : FXEvent()