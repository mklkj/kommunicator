package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class Message(
    val id: UUID,
    val isUserMessage: Boolean,
    val authorName: String,
    val content: String,
)
