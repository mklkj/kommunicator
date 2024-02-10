package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
sealed class MessageEvent

@Serializable
@SerialName("message_broadcast")
data class MessageBroadcast(
    val id: UUID,
    val participantId: UUID,
    val content: String,
    val createdAt: Instant,
) : MessageEvent()

@Serializable
@SerialName("message_push")
data class MessagePush(
    val id: UUID,
    val content: String,
) : MessageEvent()

@Serializable
data object TypingPush : MessageEvent()

@Serializable
data class TypingBroadcast(
    val participantId: UUID,
) : MessageEvent()
