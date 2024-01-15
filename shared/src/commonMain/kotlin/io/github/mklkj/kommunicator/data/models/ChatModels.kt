package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class ChatCreateRequest(
    val chatId: UUID,
    val customName: String?,
    val participants: List<UUID>,
)
