package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.uuid.UUID

data class ChatEntity(
    val id: UUID,
    val customName: String?,
)

data class ChatSummaryEntity(
    val id: UUID,
    val customName: String?,
    val lastMessage: ChatSummaryLastMessageEntity,
    val participants: List<ChatSummaryParticipant>,
)

data class ChatSummaryLastMessageEntity(
    val messageId: UUID,
    val createdAt: Instant,
    val content: String,
    val authorId: UUID,
    val authorFirstName: String,
    val authorLastName: String,
    val authorCustomName: String?,
)

data class ChatSummaryParticipant(
    val id: UUID,
    val userId: UUID,
    val email: String,
    val customName: String?,
    val firstName: String,
    val lastName: String,
    val readAt: Instant?,
)

data class ChatParticipantEntity(
    val id: UUID,
    val customName: String?,
    val userId: UUID,
    val email: String,
    val username: String,
    val userFirstName: String,
    val userLastName: String,
    val userAvatarUrl: String,
    val readAt: Instant?,
)
