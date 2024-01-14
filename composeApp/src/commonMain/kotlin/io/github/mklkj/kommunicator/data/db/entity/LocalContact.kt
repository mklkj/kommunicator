package io.github.mklkj.kommunicator.data.db.entity

import kotlinx.uuid.UUID

data class LocalContact(
    val id: UUID,
    val userId: UUID,
    val contactUserId: UUID,
    val avatarUrl: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val isActive: Boolean,
)
