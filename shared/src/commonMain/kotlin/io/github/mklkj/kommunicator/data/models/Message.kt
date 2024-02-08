package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class Message(
    val id: UUID,
    val isUserMessage: Boolean,
    val participantId: UUID,
    val participantFirstName: String,
    val participantLastName: String,
    val participantCustomName: String?,
    val createdAt: Instant,
    val content: String,
)
