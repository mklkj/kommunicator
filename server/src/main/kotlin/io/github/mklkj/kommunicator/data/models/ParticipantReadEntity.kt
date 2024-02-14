package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.uuid.UUID

data class ParticipantReadEntity(
    val participantId: UUID,
    val readAt: Instant,
)
