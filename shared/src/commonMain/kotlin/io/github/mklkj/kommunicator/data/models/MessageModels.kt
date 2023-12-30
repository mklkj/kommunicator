package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class MessageRequest(
    val id: UUID,
    val content: String,
)
