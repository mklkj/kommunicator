package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class Message(
    val id: UUID,
    val isUserMessage: Boolean,
    val authorId: UUID,
    val authorName: String,
    val authorCustomName: String?,
    val createdAt: Instant,
    val content: String,
)
