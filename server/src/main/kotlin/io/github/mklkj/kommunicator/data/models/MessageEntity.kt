package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.uuid.UUID

data class MessageEntity(
    val id: UUID,
    val chatId: UUID,
    val userId: UUID,
    val timestamp: Instant,
    val content: String,
)

data class MessageWithUserEntity(
    val id: UUID,
    val chatId: UUID,
    val userId: UUID,
    val timestamp: Instant,
    val content: String,
    val authorUsername: String,
    val authorFirstName: String,
    val authorLastName: String,
    val authorAvatarUrl: String,
)
