package io.github.mklkj.kommunicator.ui.modules.chats

import io.github.mklkj.kommunicator.data.db.entity.LocalChat

data class ChatsState(
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val isLoggedIn: Boolean = true,
    val chats: List<LocalChat> = emptyList(),
    val userAvatarUrl: String? = null,
)
