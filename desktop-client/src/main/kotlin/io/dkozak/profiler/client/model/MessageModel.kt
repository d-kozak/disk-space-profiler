package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.event.MessageEvent
import io.dkozak.profiler.scanner.util.Invariant
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.Controller

/**
 * Holds last messages
 */
class MessageModel : Controller() {
    /**
     * amount of messages to be kept
     */
    val messageLimit = 10

    /**
     * last messages that were received
     */
    @Invariant("lastMessages.size <= messageLimit")
    val lastMessages: ObservableList<MessageEvent> = FXCollections.observableList(mutableListOf<MessageEvent>())

    init {
        subscribe<MessageEvent> { event ->
            lastMessages.add(event)
            if (lastMessages.size > messageLimit)
                lastMessages.remove(0, lastMessages.size - messageLimit)
        }
    }
}