package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val avatarUrl: String,
    val isUnread: Boolean,
    val lastMessageTimestamp: LocalDateTime,
    val lastMessage: String,
    val lastMessageAuthor: String,
    val name: String,
)
