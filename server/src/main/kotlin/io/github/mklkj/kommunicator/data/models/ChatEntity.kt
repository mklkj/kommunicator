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
    val lastMessageCreatedAt: Instant?,
    val lastMessageContent: String?,
    val lastMessageAuthorId: UUID?,
    val lastMessageAuthorFirstName: String?,
    val lastMessageAuthorLastName: String?,
    val participants: List<ChatSummaryParticipant>,
)

data class ChatSummaryParticipant(
    val userId: UUID,
    val email: String,
    val customName: String?,
    val firstName: String,
    val lastName: String,
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
)
