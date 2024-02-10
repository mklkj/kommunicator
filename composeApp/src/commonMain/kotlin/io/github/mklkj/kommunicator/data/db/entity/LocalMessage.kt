package io.github.mklkj.kommunicator.data.db.entity

import kotlinx.datetime.Instant
import kotlinx.uuid.UUID

data class LocalMessage(
    val id: UUID,
    val chatId: UUID,
    val userId: UUID,
    val authorId: UUID,
    val isUserMessage: Boolean,
    val participantName: String,
    val createdAt: Instant,
    val content: String,
)
