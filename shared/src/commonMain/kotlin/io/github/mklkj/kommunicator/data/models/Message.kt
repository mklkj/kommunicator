package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class Message(
    val id: UUID,
    val participantId: UUID,
    val createdAt: Instant,
    val content: String,
)
