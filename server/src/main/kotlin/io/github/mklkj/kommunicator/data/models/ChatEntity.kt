package io.github.mklkj.kommunicator.data.models

import kotlinx.uuid.UUID

data class ChatEntity(
    val id: UUID,
    val customName: String?,
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
