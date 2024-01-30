package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class ChatCreateRequest(
    val customName: String?,
    val participants: List<UUID>,
)

@Serializable
data class ChatCreateResponse(
    val chatId: UUID,
)
