package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.uuid.UUID

data class MessageEntity(
    val id: UUID,
    val chatId: UUID,
    val userId: UUID,
    val timestamp: Instant,
    val content: String,
    val author: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
)
