package io.github.mklkj.kommunicator.ui.modules.chats

import io.github.mklkj.kommunicator.data.models.Chat

data class ChatsState(
    val chats: List<Chat> = emptyList(),
)
