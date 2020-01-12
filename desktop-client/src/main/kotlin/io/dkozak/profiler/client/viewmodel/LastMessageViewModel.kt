package io.dkozak.profiler.client.viewmodel

import io.dkozak.profiler.client.model.MessageModel
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import java.time.format.DateTimeFormatter

class LastMessageViewModel : ViewModel() {
    val lastMessageProperty = SimpleObjectProperty<String>(this, "text", "")

    private val messageModel: MessageModel by inject()

    private val timeformat = DateTimeFormatter.ofPattern("HH:mm")

    init {
        messageModel.events.onChange {
            val (text, time) = it.list.lastOrNull() ?: return@onChange
            lastMessageProperty.set("${timeformat.format(time)}: $text")
        }
    }
}