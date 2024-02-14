package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.uuid.UUID

data class MessageReadEntity(
    val messageId: UUID,
    val participantId: UUID,
    val readAt: Instant,
)
