package io.dkozak.profiler.client.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import java.time.LocalDateTime

data class MessageEvent(val text: String, val time: LocalDateTime = LocalDateTime.now()) : FXEvent()

class MessageModel : Controller() {
    val messageLimit = 10

    val events: ObservableList<MessageEvent> = FXCollections.observableList(mutableListOf<MessageEvent>())

    init {
        subscribe<MessageEvent> { event ->
            events.add(event)
            if (events.size > messageLimit)
                events.remove(0, events.size - messageLimit)
        }
    }
}