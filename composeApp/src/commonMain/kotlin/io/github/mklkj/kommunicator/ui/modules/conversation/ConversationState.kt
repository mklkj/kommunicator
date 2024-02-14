package io.github.mklkj.kommunicator.ui.modules.conversation

import io.github.mklkj.kommunicator.Chats
import io.github.mklkj.kommunicator.Participants
import io.github.mklkj.kommunicator.SelectParticipantsWithLastReadMessage
import io.github.mklkj.kommunicator.data.db.entity.LocalMessage
import io.github.mklkj.kommunicator.data.ws.ConnectionStatus

data class ConversationState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.NotConnected,
    val chat: Chats? = null,
    val messages: List<LocalMessage> = emptyList(),
    val lastReadMessages: List<SelectParticipantsWithLastReadMessage> = emptyList(),
    val typingParticipants: List<Participants> = emptyList(),
)
