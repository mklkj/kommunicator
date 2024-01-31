package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class ChatDetails(
    val id: UUID,
    val avatarUrl: String,
    val name: String?,
    val participants: List<ChatParticipant>,
    val messages: List<Message>,
)
