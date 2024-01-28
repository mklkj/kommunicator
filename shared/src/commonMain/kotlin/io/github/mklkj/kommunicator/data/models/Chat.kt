package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class Chat(
    val id: UUID,
    val avatarUrl: String,
    val isUnread: Boolean,
    val isActive: Boolean,
    val participants: List<ChatParticipant>,
    val lastMessageTimestamp: Instant?,
    val lastMessage: String?,
    val lastMessageAuthor: String?,
    val name: String?,
)

@Serializable
data class ChatParticipant(
    val userId: UUID,
    val customName: String?,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String,
)
