package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class Chat(
    val id: UUID,
    val avatarUrl: String,
    val isUnread: Boolean,
    val isActive: Boolean,
    val lastMessageTimestamp: LocalDateTime,
    val lastMessage: String,
    val lastMessageAuthor: String,
    val name: String,
)
