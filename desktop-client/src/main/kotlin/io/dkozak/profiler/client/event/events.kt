package io.dkozak.profiler.client.event

import tornadofx.*
import java.time.LocalDateTime

/**
 * Fired when any component wants to display a message to the user.
 */
data class MessageEvent(val text: String, val time: LocalDateTime = LocalDateTime.now()) : FXEvent()