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
data class TypingPush(
    val isStop: Boolean,
) : MessageEvent()

@Serializable
data class TypingBroadcast(
    val participantId: UUID,
    val isStop: Boolean,
) : MessageEvent()

@Serializable
data class ReadPush(
    val messageId: UUID,
    val readAt: Instant,
) : MessageEvent()

@Serializable
data class ReadBroadcast(
    val messageId: UUID,
    val participantId: UUID,
    val readAt: Instant,
) : MessageEvent()
