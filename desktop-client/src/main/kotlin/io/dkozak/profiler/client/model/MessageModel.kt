package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.event.MessageEvent
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*

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