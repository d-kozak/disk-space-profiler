package io.dkozak.profiler.client.viewmodel

import io.dkozak.profiler.client.model.MessageModel
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import java.time.format.DateTimeFormatter

/**
 * Contains last received message, if any
 */
class LastMessageViewModel : ViewModel() {
    /**
     * last message text
     */
    val lastMessageProperty = SimpleObjectProperty<String>(this, "text", "")

    private val messageModel: MessageModel by inject()

    private val timeformat = DateTimeFormatter.ofPattern("HH:mm")

    init {
        messageModel.lastMessages.onChange {
            val (text, time) = it.list.lastOrNull() ?: return@onChange
            lastMessageProperty.set("${timeformat.format(time)}: $text")
        }
    }
}