package io.github.mklkj.kommunicator.data.db.entity

import kotlinx.uuid.UUID

data class LocalChat(
    val id: UUID,
    val avatarUrl: String,
    val isUnread: Boolean,
    val isActive: Boolean,
    val participants: List<LocalParticipant>,
    val lastMessage: LocalMessage?,
    val customName: String?,
)
