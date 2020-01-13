package io.dkozak.profiler.client.event

import tornadofx.*
import java.time.LocalDateTime

data class MessageEvent(val text: String, val time: LocalDateTime = LocalDateTime.now()) : FXEvent()