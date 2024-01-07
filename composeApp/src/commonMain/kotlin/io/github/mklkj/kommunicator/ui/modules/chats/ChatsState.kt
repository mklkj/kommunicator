package io.github.mklkj.kommunicator.ui.modules.chats

import io.github.mklkj.kommunicator.data.models.Chat

data class ChatsState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = true,
    val chats: List<Chat> = emptyList(),
    val userAvatarUrl: String? = null,
)
