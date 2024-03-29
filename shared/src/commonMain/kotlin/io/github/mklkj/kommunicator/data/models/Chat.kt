package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class Chat(
    val id: UUID,
    val avatarUrl: String,
    val participants: List<ChatParticipant>,
    val lastMessage: Message?,
    val customName: String?,
)

@Serializable
data class ChatParticipant(
    val id: UUID,
    val userId: UUID,
    val customName: String?,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String,
    val readAt: Instant?,
)
