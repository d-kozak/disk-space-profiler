package io.dkozak.profiler.client.model

import io.dkozak.profiler.client.util.Cleanup
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import java.time.LocalDateTime

data class MessageEvent(val text: String, val time: LocalDateTime = LocalDateTime.now()) : FXEvent()
@Cleanup("controller inherited only for dependency injection and event bus substribe, there might be a better way")
class MessageModel : Controller() {
    val events: ObservableList<MessageEvent> = FXCollections.observableList(mutableListOf(MessageEvent("Welcome :)")))

    init {
        subscribe<MessageEvent> { event ->
            events.add(event)
        }
    }
}