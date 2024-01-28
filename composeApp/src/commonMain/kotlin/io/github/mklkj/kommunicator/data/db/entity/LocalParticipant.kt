package io.github.mklkj.kommunicator.data.db.entity

import kotlinx.uuid.UUID

data class LocalParticipant(
    val id: UUID,
    val chatId: UUID,
    val userId: UUID,
    val customName: String?,
)
