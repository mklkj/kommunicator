package io.github.mklkj.kommunicator.ui.modules.conversation

import io.github.mklkj.kommunicator.data.models.ChatDetails

data class ConversationState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val details: ChatDetails? = null,
)
