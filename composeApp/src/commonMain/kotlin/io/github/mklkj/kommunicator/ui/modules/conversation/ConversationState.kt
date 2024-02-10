package io.github.mklkj.kommunicator.ui.modules.conversation

import io.github.mklkj.kommunicator.Chats
import io.github.mklkj.kommunicator.data.db.entity.LocalMessage

data class ConversationState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val chat: Chats? = null,
    val messages: List<LocalMessage> = emptyList(),
)
